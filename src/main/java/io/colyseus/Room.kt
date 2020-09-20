package io.colyseus

import com.fasterxml.jackson.databind.ObjectMapper
import io.colyseus.serializer.SchemaSerializer
import io.colyseus.serializer.schema.*
import io.colyseus.serializer.schema.Iterator
import org.java_websocket.framing.CloseFrame
import org.msgpack.jackson.dataformat.MessagePackFactory
import java.net.URI
import java.net.URISyntaxException
import java.nio.ByteBuffer

class Room<T : Schema> internal constructor(schema: Class<T>, var name: String) {

    public var onLeave: ((code: Int) -> Unit)? = null
    public var onError: ((code: Int, message: String?) -> Unit)? = null
    public var onJoin: (() -> Unit)? = null
    public var onStateChange: ((state: T, isFirstState: Boolean) -> Unit)? = null
    public val onMessageHandlers = hashMapOf<String, MessageHandler<*>>()

    class MessageHandler<out K> constructor(
            val type: Class<out K>,
            val handler: ((message: Any) -> Unit)?,
    )

    var id: String? = null
    var sessionId: String? = null
    private var connection: Connection? = null

    private val msgpackMapper = ObjectMapper(MessagePackFactory())
    private var serializer = SchemaSerializer(schema)
    public val state: T = serializer.state as T

    @Throws(URISyntaxException::class)
    fun connect(endpoint: String, httpHeaders: Map<String, String>? = null) {
//        System.out.println("Room is connecting to " + endpoint)
        connection = Connection(URI(endpoint), httpHeaders)
        connection?.onError = { e -> onError?.invoke(-1, e.message) }
        connection?.onClose = { code, reason, _ ->
            if (code == CloseFrame.PROTOCOL_ERROR &&
                    reason != null &&
                    reason.startsWith("Invalid status code received: 401")) {
                onError?.invoke(-1, reason)
            }
            onLeave?.invoke(code)
        }
        connection?.onMessage = { buf: ByteBuffer ->
            val length = buf.remaining()
            val bytes = ByteArray(length)
            buf[bytes, 0, length]
            this.parseMessage(bytes)
        }
        connection?.connect()
    }

    private fun parseMessage(bytes: ByteArray) {
        try {
            val code = bytes[0].toInt() and 0xFF
            when (code) {
                Protocol.JOIN_ROOM -> {
                    var offset = 1
                    val serializerId = String(bytes, offset + 1, bytes[offset].toInt() and 0xFF)
                    offset += serializerId.length + 1
                    if (serializerId == "fossil-delta") {
                        throw Error("fossil-delta is not supported")
                    }
                    if (bytes.size > offset) {
                        serializer.handshake(bytes, offset)
                    }
                    onJoin?.invoke()
                    connection?._send(Protocol.JOIN_ROOM)
                }
                Protocol.ERROR -> {
                    val it = Iterator(1)
                    val errorCode: Int = Decoder.decodeNumber(bytes, it).toInt()
                    val errorMessage = Decoder.decodeString(bytes, it)
                    onError?.invoke(errorCode, errorMessage)
                }
                Protocol.ROOM_DATA_SCHEMA -> {
                    val messageType: Class<*> = Context.instance.get(bytes[1].toInt() and 0xFF) as Class<*>
                    val message = messageType.getConstructor().newInstance() as Schema
                    message.decode(bytes, Iterator(2))
                    val messageHandler = onMessageHandlers["s" + messageType.typeName]
                    if (messageHandler != null) {
                        messageHandler.handler?.invoke(message)
                    } else {
                        println("No handler for type " + messageType.typeName)
                    }
                }
                Protocol.LEAVE_ROOM -> leave()
                Protocol.ROOM_STATE -> setState(bytes, 1)
                Protocol.ROOM_STATE_PATCH -> patch(bytes, 1)
                Protocol.ROOM_DATA -> {
                    val messageHandler: MessageHandler<*>?
                    val type: Any
                    val it = Iterator(1)
                    if (Decoder.numberCheck(bytes, it)) {
                        type = Decoder.decodeNumber(bytes, it).toInt()
                        messageHandler = onMessageHandlers["i$type"]
                    } else {
                        type = Decoder.decodeString(bytes, it)
                        messageHandler = onMessageHandlers[type.toString()]
                    }
                    if (messageHandler != null) {
                        if (bytes.size > it.offset) {
                            messageHandler.handler?.invoke(
                                    msgpackMapper.readValue(bytes[it.offset..bytes.size], messageHandler.type)!!
                            )
                        }
                    } else {
                        println("No handler for type $type" + if (type is Int) " (${Context.instance.get(type)?.simpleName})" else "")
                    }
                }
            }
        } catch (e: Exception) {
            leave(false)
            e.printStackTrace()
            onError?.invoke(-1, e.message)
        }
    }

    // Disconnect from the room.
    @JvmOverloads
    fun leave(consented: Boolean = true) {
        if (connection != null) {
            if (consented) {
                connection!!._send(Protocol.LEAVE_ROOM)
            } else {
                connection!!.close()
            }
        } else {
            onLeave?.invoke(4000) // "consented" code
        }
    }

    // Send a message by number type, without payload
    public fun send(type: Int) {
        connection?.send(byteArrayOfInts(Protocol.ROOM_DATA, type))
    }

    // Send a message by number type with payload
    public fun send(type: Int, message: Any) {
        val initialBytes: ByteArray = byteArrayOfInts(Protocol.ROOM_DATA, type)
        val encodedMessage: ByteArray = msgpackMapper.writeValueAsBytes(message)
        connection?.send(initialBytes + encodedMessage)
    }

    // Send a message by string type, without payload
    public fun send(type: String) {
        val encodedType: ByteArray = type.toByteArray()
        val initialBytes: ByteArray = Encoder.getInitialBytesFromEncodedType(encodedType)
        connection?.send(initialBytes + encodedType)
    }

    // Send a message by string type with payload
    public fun send(type: String, message: Any) {
        val encodedMessage: ByteArray = msgpackMapper.writeValueAsBytes(message)
        val encodedType: ByteArray = type.toByteArray()
        val initialBytes: ByteArray = Encoder.getInitialBytesFromEncodedType(encodedType)
        connection?.send(initialBytes + encodedType + encodedMessage)
    }

    public inline fun <reified MessageType> onMessage(
            type: String,
            noinline handler: ((message: MessageType) -> Unit)?,
    ) {
        onMessageHandlers[type] = MessageHandler(
                MessageType::class.java,
                handler as ((Any) -> Unit)?
        )
    }

    public inline fun <reified MessageType> onMessage(
            type: Int,
            noinline handler: ((message: MessageType) -> Unit)?,
    ) {
        onMessageHandlers["i$type"] = MessageHandler(
                MessageType::class.java,
                handler as ((Any) -> Unit)?
        )
    }

    public inline fun <reified MessageType> onMessage(
            noinline handler: ((message: MessageType) -> Unit)?,
    ) {
        onMessageHandlers["s" + MessageType::class.java.name] = MessageHandler(
                MessageType::class.java,
                handler as ((Any) -> Unit)?
        )
    }

    public fun hasJoined(): Boolean {
        return sessionId != null
    }

    @Throws(Exception::class)
    private fun setState(encodedState: ByteArray, offset: Int = 0) {
        serializer.setState(encodedState, offset)
        onStateChange?.invoke(serializer.state as T, true)
    }

    @Throws(Exception::class)
    private fun patch(delta: ByteArray, offset: Int = 0) {
        serializer.patch(delta, offset)
        onStateChange?.invoke(serializer.state as T, false)
    }
}

package io.colyseus

import com.fasterxml.jackson.databind.ObjectMapper
import org.java_websocket.client.WebSocketClient
import org.java_websocket.drafts.Draft_6455
import org.java_websocket.handshake.ServerHandshake
import org.msgpack.jackson.dataformat.MessagePackFactory
import java.net.URI
import java.nio.ByteBuffer
import java.util.*

class Connection internal constructor(
        uri: URI,
        httpHeaders: Map<String, String>? = null,
) : WebSocketClient(uri, Draft_6455(), httpHeaders, CONNECT_TIMEOUT) {

    companion object {
        private const val CONNECT_TIMEOUT = 10000
    }

    var onError: ((e: Exception) -> Unit)? = null
    var onClose: ((code: Int, reason: String?, remote: Boolean) -> Unit)? = null
    var onOpen: (() -> Unit)? = null
    var onMessage: ((bytes: ByteBuffer) -> Unit)? = null

    private val _enqueuedCalls = LinkedList<Any>()
    private val msgpackMapper: ObjectMapper = ObjectMapper(MessagePackFactory())


    fun send(data: Any) {
        if (isOpen) {
            try {
                val d = msgpackMapper.writeValueAsBytes(data)
//                println("sending... " + Arrays.toString(d))
                send(d)
            } catch (e: Exception) {
                onError(e)
            }
        } else {
            // WebSocket not connected.
            // Enqueue data to be sent when readyState == OPEN
            _enqueuedCalls.push(data)
        }
    }

    override fun onOpen(handshakedata: ServerHandshake) {
        if (_enqueuedCalls.size > 0) {
            for (objects in _enqueuedCalls) {
                this@Connection.send(objects)
            }
            _enqueuedCalls.clear()
        }
        onOpen?.invoke()
    }

    override fun onMessage(message: String) {
    }

    override fun onClose(code: Int, reason: String, remote: Boolean) {
        onClose?.invoke(code, reason, remote)
    }

    override fun onError(ex: Exception) {
        onError?.invoke(ex)
    }

    override fun onMessage(buf: ByteBuffer) {
//        println("received: $buf")
        onMessage?.invoke(buf)
    }
}
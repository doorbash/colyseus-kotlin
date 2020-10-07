package io.colyseus

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import io.colyseus.async.ColyseusAsync
import io.colyseus.serializer.schema.Schema
import io.colyseus.util.Http.request
import io.colyseus.util.callbacks.Function1Void
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.util.*
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class Client(private val endpoint: String) {

    private val objectMapper = ObjectMapper()

    data class AvailableRoom(
            var roomId: String? = null,
            var clients: Int = 0,
            var maxClients: Int = 0,
            var metadata: Any? = null,
    )

    public suspend fun <T : Schema> joinOrCreate(
            schema: Class<T>,
            roomName: String,
            options: LinkedHashMap<String, Any>? = null,
            httpHeaders: MutableMap<String, String>? = null,
            wsHeaders: Map<String, String>? = null,
    ): Room<T> {
        return withContext(ColyseusAsync.coroutineContext) {
            createMatchMakeRequest(
                    schema,
                    "joinOrCreate",
                    roomName,
                    options,
                    httpHeaders,
                    wsHeaders,
            )
        }
    }

    @JvmOverloads
    public fun <T : Schema> joinOrCreate(
            schema: Class<T>,
            roomName: String,
            options: LinkedHashMap<String, Any>? = null,
            httpHeaders: MutableMap<String, String>? = null,
            wsHeaders: Map<String, String>? = null,
            callback: Function1Void<Room<T>>,
            onError: Function1Void<Exception>,
    ) {
        ColyseusAsync.launch {
            try {
                callback(createMatchMakeRequest(
                        schema,
                        "joinOrCreate",
                        roomName,
                        options,
                        httpHeaders,
                        wsHeaders,
                ))
            } catch (e: Exception) {
                onError.invoke(e)
            }
        }
    }

    public suspend fun <T : Schema> create(
            schema: Class<T>,
            roomName: String,
            options: LinkedHashMap<String, Any>? = null,
            httpHeaders: MutableMap<String, String>? = null,
            wsHeaders: Map<String, String>? = null,
    ): Room<T> {
        return withContext(ColyseusAsync.coroutineContext) {
            createMatchMakeRequest(
                    schema,
                    "create",
                    roomName,
                    options,
                    httpHeaders,
                    wsHeaders,
            )
        }
    }

    @JvmOverloads
    public fun <T : Schema> create(
            schema: Class<T>,
            roomName: String,
            options: LinkedHashMap<String, Any>? = null,
            httpHeaders: MutableMap<String, String>? = null,
            wsHeaders: Map<String, String>? = null,
            callback: Function1Void<Room<T>>,
            onError: Function1Void<Exception>,
    ) {
        ColyseusAsync.launch {
            try {
                callback(createMatchMakeRequest(
                        schema,
                        "create",
                        roomName,
                        options,
                        httpHeaders,
                        wsHeaders,
                ))
            } catch (e: Exception) {
                onError.invoke(e)
            }
        }
    }

    public suspend fun <T : Schema> join(
            schema: Class<T>,
            roomName: String,
            options: LinkedHashMap<String, Any>? = null,
            httpHeaders: MutableMap<String, String>? = null,
            wsHeaders: Map<String, String>? = null,
    ): Room<T> {
        return withContext(ColyseusAsync.coroutineContext) {
            createMatchMakeRequest(
                    schema,
                    "join",
                    roomName,
                    options,
                    httpHeaders,
                    wsHeaders,
            )
        }
    }

    @JvmOverloads
    public fun <T : Schema> join(
            schema: Class<T>,
            roomName: String,
            options: LinkedHashMap<String, Any>? = null,
            httpHeaders: MutableMap<String, String>? = null,
            wsHeaders: Map<String, String>? = null,
            callback: Function1Void<Room<T>>,
            onError: Function1Void<Exception>,
    ) {
        ColyseusAsync.launch {
            try {
                callback(createMatchMakeRequest(
                        schema,
                        "join",
                        roomName,
                        options,
                        httpHeaders,
                        wsHeaders,
                ))
            } catch (e: Exception) {
                onError.invoke(e)
            }
        }
    }

    public suspend fun <T : Schema> joinById(
            schema: Class<T>,
            roomId: String,
            options: LinkedHashMap<String, Any>? = null,
            httpHeaders: MutableMap<String, String>? = null,
            wsHeaders: Map<String, String>? = null,
    ): Room<T> {
        return withContext(ColyseusAsync.coroutineContext) {
            createMatchMakeRequest(
                    schema,
                    "joinById",
                    roomId,
                    options,
                    httpHeaders,
                    wsHeaders,
            )
        }
    }

    @JvmOverloads
    public fun <T : Schema> joinById(
            schema: Class<T>,
            roomId: String,
            options: LinkedHashMap<String, Any>? = null,
            httpHeaders: MutableMap<String, String>? = null,
            wsHeaders: Map<String, String>? = null,
            callback: Function1Void<Room<T>>,
            onError: Function1Void<Exception>,
    ) {
        ColyseusAsync.launch {
            try {
                callback(createMatchMakeRequest(
                        schema,
                        "joinById",
                        roomId,
                        options,
                        httpHeaders,
                        wsHeaders,
                ))
            } catch (e: Exception) {
                onError.invoke(e)
            }
        }
    }

    public suspend fun <T : Schema> reconnect(
            schema: Class<T>,
            roomId: String,
            sessionId: String,
            httpHeaders: MutableMap<String, String>? = null,
            wsHeaders: Map<String, String>? = null,
    ): Room<T> {
        val options = LinkedHashMap<String, Any>()
        options["sessionId"] = sessionId
        return withContext(ColyseusAsync.coroutineContext) {
            createMatchMakeRequest(
                    schema,
                    "joinById",
                    roomId,
                    options,
                    httpHeaders,
                    wsHeaders
            )
        }
    }

    @JvmOverloads
    public fun <T : Schema> reconnect(
            schema: Class<T>,
            roomId: String,
            sessionId: String,
            httpHeaders: MutableMap<String, String>? = null,
            wsHeaders: Map<String, String>? = null,
            callback: Function1Void<Room<T>>,
            onError: Function1Void<Exception>,
    ) {
        ColyseusAsync.launch {
            try {
                val options = LinkedHashMap<String, Any>()
                options["sessionId"] = sessionId
                callback(createMatchMakeRequest(
                        schema,
                        "joinById",
                        roomId,
                        options,
                        httpHeaders,
                        wsHeaders
                ))
            } catch (e: Exception) {
                onError.invoke(e)
            }
        }
    }

    suspend fun getAvailableRooms(roomName: String): List<AvailableRoom> {
        return withContext(ColyseusAsync.coroutineContext) {
            getAvailableRoomsSync(roomName)
        }
    }

    fun getAvailableRooms(roomName: String, callback: Function1Void<List<AvailableRoom>>) {
        ColyseusAsync.launch {
            callback(getAvailableRoomsSync(roomName))
        }
    }

    private fun getAvailableRoomsSync(roomName: String): List<AvailableRoom> {
        val url = endpoint.replace("ws", "http") + "/matchmake/" + roomName
        val httpHeaders = LinkedHashMap<String, String>()
        httpHeaders["Accept"] = "application/json"
        val response: String = request(url = url, method = "GET", httpHeaders = httpHeaders)
        val data: List<AvailableRoom> =
                objectMapper.readValue(response, ArrayList::class.java) as List<AvailableRoom>
        return data
    }

    private suspend fun <T : Schema> createMatchMakeRequest(
            schema: Class<T>,
            method: String,
            roomName: String,
            options: LinkedHashMap<String, Any>? = null,
            httpHeaders: MutableMap<String, String>? = null,
            wsHeaders: Map<String, String>? = null,
    ): Room<T> {
        return suspendCoroutine { cont: Continuation<Room<T>> ->
            var headers: MutableMap<String, String>? = httpHeaders
            try {
                val url = endpoint.replace("ws", "http") +
                        "/matchmake/" +
                        method +
                        "/" +
                        URLEncoder.encode(roomName, "UTF-8")
                val body = if (options != null) {
                    objectMapper.writeValueAsString(
                            objectMapper.convertValue(options, JsonNode::class.java)
                    )
                } else "{}"
                if (headers == null) headers = LinkedHashMap()
                headers["Accept"] = "application/json"
                headers["Content-Type"] = "application/json"
                val res = request(url = url, method = "POST", httpHeaders = headers, body = body)
                val response = objectMapper.readValue(res, JsonNode::class.java)
                if (response.has("error")) {
                    throw MatchMakeException(response["error"].asText(), response["code"].asInt())
                }
                val room = Room(schema, roomName)
                val roomId = response["room"]["roomId"].asText()
                room.id = roomId
                val sessionId = response["sessionId"].asText()
                room.sessionId = sessionId
                room.onError = { code, message ->
                    cont.resumeWithException(Exception(message))
                }
                room.onJoin = {
                    room.onError = null
                    room.onJoin = null
                    cont.resume(room)
                }
                val wsOptions = LinkedHashMap<String, Any?>()
                wsOptions["sessionId"] = room.sessionId
                val wsUrl = buildEndpoint(response["room"], wsOptions)
                //            System.out.println("ws url is " + wsUrl)
                room.connect(wsUrl, wsHeaders)
            } catch (e: Exception) {
                cont.resumeWithException(e)
            }
        }
    }

    class MatchMakeException(message: String?, var code: Int) : Exception(message) {
        companion object {
            // MatchMaking Error Codes
            const val ERR_MATCHMAKE_NO_HANDLER = 4210
            const val ERR_MATCHMAKE_INVALID_CRITERIA = 4211
            const val ERR_MATCHMAKE_INVALID_ROOM_ID = 4212
            const val ERR_MATCHMAKE_UNHANDLED = 4213 // generic exception during onCreate/onJoin
            const val ERR_MATCHMAKE_EXPIRED = 4214 // generic exception during onCreate/onJoin
        }
    }

    @Throws(UnsupportedEncodingException::class)
    private fun buildEndpoint(room: JsonNode, options: LinkedHashMap<String, Any?>): String {
        val charset = "UTF-8"
        val params = StringBuilder()
        for ((i, name) in options.keys.withIndex()) {
            if (i > 0) params.append("&")
            params.append(URLEncoder.encode(name, charset)).append("=").append(URLEncoder.encode(options[name].toString(), charset))
        }
        return "$endpoint/${room["processId"].asText()}/${room["roomId"].asText()}?$params"
    }
}
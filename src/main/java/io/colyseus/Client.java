package io.colyseus;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.colyseus.serializer.schema.Schema;
import org.msgpack.jackson.dataformat.MessagePackFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;

public class Client {

    /**
     * Unique identifier for the client.
     */
    private String id;
    private LinkedHashMap<String, String> httpHeaders;
    private int connectTimeout;

    /**
     * An interface for listening to client events
     */
    public interface Listener {

        /**
         * This event is triggered when the connection is accepted by the server.
         *
         * @param id ColyseusId provided by the server
         */
        void onOpen(String id);


        /**
         * This event is triggered when an unhandled message comes to client from server
         *
         * @param message The message from server
         */
        void onMessage(Object message);

        /**
         * This event is triggered when the connection is closed.
         *
         * @param code   The codes can be looked up here: {@link org.java_websocket.framing.CloseFrame}
         * @param reason Additional information
         * @param remote Whether or not the closing of the connection was initiated by the remote host
         */
        void onClose(int code, String reason, boolean remote);

        /**
         * This event is triggered when some error occurs in the server.
         */
        void onError(Exception e);
    }

//    public static interface GetAvailableRoomsCallback {
//        void onCallback(List<AvailableRoom> availableRooms, String error);
//    }

//    public static interface AvailableRoomsRequestListener {
//        void callback(List<AvailableRoom> availableRooms);
//    }

    private Connection connection;
    public LinkedHashMap<String, Room> rooms = new LinkedHashMap<>();
    public LinkedHashMap<Integer, Room> connectingRooms = new LinkedHashMap<>();
    private int requestId = 0;
    private String hostname;
    //    private LinkedHashMap<Integer, AvailableRoomsRequestListener> availableRoomsRequests = new LinkedHashMap<>();
    private Listener listener;
    private ObjectMapper msgpackMapper;
    private ObjectMapper defaultMapper;

    public Client(String url) {
        this(url, null, null, null, 0, null);
    }

    public Client(String url, Listener listener) {
        this(url, null, null, null, 0, listener);
    }

    public Client(String url, String id) {
        this(url, id, null, null, 0, null);
    }

    public Client(String url, String id, Listener listener) {
        this(url, id, null, null, 0, listener);
    }

    public Client(String url, String id, LinkedHashMap<String, Object> options, LinkedHashMap<String, String> httpHeaders, int connectTimeout, Listener listener) {
        this.hostname = url;
        this.id = id;
        this.httpHeaders = httpHeaders == null ? new LinkedHashMap<String, String>() : httpHeaders;
        this.connectTimeout = connectTimeout;
        this.listener = listener;
        this.defaultMapper = new ObjectMapper();
        this.msgpackMapper = new ObjectMapper(new MessagePackFactory());
        this.connect(options == null ? new LinkedHashMap<String, Object>() : options, connectTimeout);
    }

    public String getId() {
        return this.id;
    }

    /**
     * Joins room
     *
     * @param roomName can be either a room name or a roomId
     */
    public Room join(String roomName, Class<? extends Schema> type) {
        return this.createRoomRequest(type, roomName, null);
    }

    public Room join(String roomName, LinkedHashMap<String, Object> options, Class<? extends Schema> type) {
        return this.createRoomRequest(type, roomName, options);
    }

    /**
     * Reconnects the client into a room he was previously connected with.
     */
    public Room rejoin(String roomName, String sessionId, Class<? extends Schema> type) {
        LinkedHashMap<String, Object> options = new LinkedHashMap<>();
        options.put("sessionId", sessionId);
        return this.join(roomName, options, type);
    }

    private Room createRoomRequest(Class<?> type, final String roomName, LinkedHashMap<String, Object> options) {
//        System.out.println("createRoomRequest(" + roomName + "," + options + "," + reuseRoomInstance + "," + retryTimes + "," + retryCount);
        if (options == null) options = new LinkedHashMap<>();
        options.put("requestId", ++this.requestId);

//        if (Auth.HasToken)
//        {
//            options.Add("token", Auth.Token);
//        }

        final Room room = createRoom(type, roomName, options);

//        final LinkedHashMap<String, Object> finalOptions = options;
//        room.addListener(new Room.Listener() {
//            @Override
//            public void onLeave() {
//                rooms.remove(room.getId());
//                connectingRooms.remove(finalOptions.get("requestId"));
//            }
//        });

        this.connectingRooms.put((int) options.get("requestId"), room);

        this.connection.send(Protocol.JOIN_REQUEST, roomName, options);

        return room;
    }

    private Room createRoom(Class<?> type, String roomName, LinkedHashMap<String, Object> options) {
        return new Room(type, this, roomName, options);
    }

    /**
     * List all available rooms to connect with the provided roomName. Locked rooms won't be listed.
     */
//    public void getAvailableRooms(String roomName, final GetAvailableRoomsCallback callback) {
//        // reject this promise after 10 seconds.
//
//        ++this.requestId;
//
//        final int requestIdFinal = this.requestId;
//
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    Thread.sleep(10000);
//                    if (availableRoomsRequests.containsKey(requestIdFinal)) {
//                        availableRoomsRequests.remove(requestIdFinal);
//                        callback.onCallback(null, "timeout");
//                    }
//                } catch (Exception ignored) {
//
//                }
//            }
//        });
//
//        // send the request to the server.
//        this.connection.send(Protocol.ROOM_LIST, requestIdFinal, roomName);
//
//        availableRoomsRequests.put(requestIdFinal, new AvailableRoomsRequestListener() {
//            @Override
//            public void callback(List<AvailableRoom> availableRooms) {
//                availableRoomsRequests.remove(requestIdFinal);
//                callback.onCallback(availableRooms, null);
//            }
//        });
//    }

    /**
     * Close connection with the server.
     */
    public void close() {
        this.connection.close();
    }

    private void connect(LinkedHashMap<String, Object> options, int connectTimeout) {
        URI uri;
        try {
            uri = new URI(buildEndpoint("", options));
        } catch (URISyntaxException | UnsupportedEncodingException | JsonProcessingException e) {
            if (Client.this.listener != null)
                Client.this.listener.onError(e);
            return;
        }
        this.connection = new Connection(uri, connectTimeout, httpHeaders, new Connection.Listener() {
            @Override
            public void onError(Exception e) {
                if (Client.this.listener != null)
                    Client.this.listener.onError(e);
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                if (Client.this.listener != null)
                    Client.this.listener.onClose(code, reason, remote);
            }

            @Override
            public void onOpen() {
                if (Client.this.id != null && Client.this.listener != null)
                    Client.this.listener.onOpen(Client.this.id);
            }

            @Override
            public void onMessage(ByteBuffer bytes) {
                Client.this.onMessageCallback(bytes);
            }
        });
    }

    private String buildEndpoint(String path, LinkedHashMap<String, Object> options) throws UnsupportedEncodingException, JsonProcessingException {
        // append colyseusid to connection string.
        String charset = "UTF-8";
        StringBuilder sb = new StringBuilder();
        for (String name : options.keySet()) {
            sb.append("&");
            sb.append(URLEncoder.encode(name, charset));
            sb.append("=");
            sb.append(URLEncoder.encode(defaultMapper.writeValueAsString(options.get(name)), charset));
        }
        return this.hostname + "/" + path + "?colyseusid=" + URLEncoder.encode(this.id == null ? "" : this.id, charset) + sb.toString();
    }

    int previousCode;

    private void onMessageCallback(ByteBuffer buf) {
//        System.out.println("Client.onMessageCallback()");

        try {
            if (previousCode == 0) {
                int code = buf.get();
                if (code == Protocol.USER_ID) {
                    byte[] bytes = new byte[buf.get()];
                    buf.get(bytes);
                    this.id = new String(bytes, StandardCharsets.UTF_8);
                    // storage.set_item("colyseusid", self.id)
                    System.out.println("colyseus id : " + this.id);
                    if (Client.this.listener != null) {
                        Client.this.listener.onOpen(this.id);
                    }
                } else if (code == Protocol.JOIN_REQUEST) {
                    int requestId = buf.get();
                    Room room = this.connectingRooms.get(requestId);
                    if (room == null) {
                        System.out.println("client left room before receiving session id.");
                        return;
                    }
                    byte[] bytes = new byte[buf.get()];
                    buf.get(bytes, 0, bytes.length);
                    room.setId(new String(bytes, StandardCharsets.UTF_8));
                    String processPath = "";
                    if (buf.hasRemaining()) {
                        bytes = new byte[buf.get()];
                        buf.get(bytes, 0, bytes.length);
                        processPath = new String(bytes, StandardCharsets.UTF_8);
                    }
                    connectingRooms.remove(requestId);
                    this.rooms.put(room.getId(), room);
                    room.connect(buildEndpoint(processPath + "/" + room.getId(), room.getOptions()), httpHeaders, this.connectTimeout);
                } else if (code == Protocol.JOIN_ERROR) {
                    int length = buf.get();
                    byte[] bytes = new byte[length];
                    buf.get(bytes);
                    String message = new String(bytes, StandardCharsets.UTF_8);
                    if (this.listener != null) this.listener.onError(new Exception(message));
                } else if (code == Protocol.ROOM_LIST) {
                    previousCode = code;
                }
            } else {
                previousCode = 0;
            }
        } catch (Exception e) {
            if (this.listener != null) this.listener.onError(e);
        }

//        try {
////            if (val.getValueType() == ValueType.ARRAY) {
////                ArrayValue arrayValue = val.asArrayValue();
////                Value codeValue = arrayValue.get(0);
//                if (val.getValueType() == ValueType.INTEGER) {
//                    int code = val.asIntegerValue().asInt();
//                    switch (code) {
//                        case Protocol.USER_ID: {
////                            System.out.println("Protocol: USER_ID");
//                            this.id = unpacker.unpackString();
////                            System.out.println("colyseus id : " + this.id);
//                            if (Client.this.listener != null) {
//                                Client.this.listener.onOpen(this.id);
//                            }
//                        }
//                        break;
//                        case Protocol.JOIN_ROOM: {
////                            System.out.println("Protocol: JOIN_ROOM");
//                            int requestId = unpacker.unpackInt();
////                            System.out.println("requestId: " + requestId);
//                            Room room = this.connectingRooms.get(requestId);
//                            if (room == null) {
//                                System.out.println("client left room before receiving session id.");
//                                return;
//                            }
//                            room.setId(unpacker.unpackString());
////                            System.out.println("room.id: " + room.getId());
//                            this.rooms.put(room.getId(), room);
//                            room.connect(buildEndpoint(room.getId(), room.getOptions()), httpHeaders, this.connectTimeout);
//                            connectingRooms.remove(requestId);
//                        }
//                        break;
//                        case Protocol.JOIN_ERROR: {
////                            System.out.println("Protocol: JOIN_ERROR");
//                            String error = unpacker.unpackString();
//                            System.err.println("colyseus: server error: " + error);
//                            // general error
//                            if (this.listener != null)
//                                this.listener.onError(new Exception(error));
//                        }
//                        break;
//                        case Protocol.ROOM_LIST: {
//////                            System.out.println("Protocol: ROOM_LIST");
////                            int id = arrayValue.get(1).asIntegerValue().asInt();
//////                            System.out.println("id: " + id);
////                            ArrayValue roomsArrayValue = arrayValue.get(2).asArrayValue();
////                            List<AvailableRoom> availableRooms = new ArrayList<>();
////                            for (int i = 0; i < roomsArrayValue.size(); i++) {
////                                MapValue roomMapValue = roomsArrayValue.get(i).asMapValue();
////                                AvailableRoom room = new AvailableRoom();
////                                for (Map.Entry<Value, Value> entry : roomMapValue.entrySet()) {
////                                    switch (entry.getKey().asStringValue().asString()) {
////                                        case "clients":
////                                            room.clients = entry.getValue().asIntegerValue().asInt();
////                                            break;
////                                        case "maxClients":
////                                            room.maxClients = entry.getValue().asIntegerValue().asInt();
////                                            break;
////                                        case "roomId":
////                                            room.roomId = entry.getValue().asStringValue().asString();
////                                            break;
////                                        case "metadata":
////                                            room.metadata = entry.getValue();
////                                            break;
////                                    }
////                                }
////                                availableRooms.add(room);
////                            }
////                            if (this.availableRoomsRequests.containsKey(id)) {
////                                this.availableRoomsRequests.get(id).callback(availableRooms);
////                            } else {
////                                System.out.println("receiving ROOM_LIST after timeout:" + roomsArrayValue);
////                            }
//                        }
//                        break;
//                        default: {
//                            // message is array, first element is integer but it is not a Protocol code
//                            dispatchOnMessage(bytes);
//                        }
//                    }
//                } else {
//                    // message is array but first element is not integer
//                    dispatchOnMessage(bytes);
//                }
////            } else {
////                // message is not an array
////                dispatchOnMessage(bytes);
////            }
//        } catch (Exception e) {
//            if (this.listener != null) this.listener.onError(e);
//        }
    }

    private void dispatchOnMessage(byte[] bytes) throws IOException {
        if (Client.this.listener != null)
            Client.this.listener.onMessage(msgpackMapper.readValue(bytes, new TypeReference<Object>() {
            }));
    }

    void onRoomLeave(String roomId) {
        this.rooms.remove(roomId);
    }
}
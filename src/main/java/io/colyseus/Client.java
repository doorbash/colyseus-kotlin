package io.colyseus;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;
import org.msgpack.jackson.dataformat.MessagePackFactory;
import org.msgpack.value.ArrayValue;
import org.msgpack.value.MapValue;
import org.msgpack.value.Value;
import org.msgpack.value.ValueType;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Client {

    /**
     * Unique identifier for the client.
     */
    private String id;
    private LinkedHashMap<String, String> httpHeaders;
    private int connectTimeout;
    private LinkedHashMap<String, Object> options;

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
         *
         * @param e error exception object
         */
        void onError(Exception e);
    }

    public static interface GetAvailableRoomsCallback {
        void onCallback(List<AvailableRoom> availableRooms, String error);
    }

    public static interface AvailableRoomsRequestListener {
        void callback(List<AvailableRoom> availableRooms);
    }

    public static class AvailableRoom {

        public int clients;
        public int maxClients;
        public String roomId;
        public Value metadata;

        @Override
        public String toString() {
            return "{" +
                    "clients:" + clients + ", " +
                    "maxClients:" + maxClients + ", " +
                    "roomId:" + roomId + ", " +
                    "metadata:" + metadata + ", " +
                    "}";
        }
    }

    private Connection connection;
    public LinkedHashMap<String, Room> rooms = new LinkedHashMap<>();
    public LinkedHashMap<Integer, Room> connectingRooms = new LinkedHashMap<>();
    private int requestId = 0;
    private String hostname;
    private LinkedHashMap<Integer, AvailableRoomsRequestListener> availableRoomsRequests = new LinkedHashMap<>();
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
        this.options = options == null ? new LinkedHashMap<String, Object>() : options;
        this.defaultMapper = new ObjectMapper();
        this.msgpackMapper = new ObjectMapper(new MessagePackFactory());
    }

    public String getId() {
        return this.id;
    }

    /**
     * Joins room
     *
     * @param roomName can be either a room name or a roomId
     * @return joined Room object
     */
    public Room join(String roomName) {
        return this.createRoomRequest(roomName, null);
    }

    public Room join(String roomName, LinkedHashMap<String, Object> options) {
        return this.createRoomRequest(roomName, options);
    }

    /**
     * Reconnects the client into a room he was previously connected with.
     *
     * @param roomName room name
     * @param sessionId  session id
     * @return rejoined Room object
     */
    public Room rejoin(String roomName, String sessionId) {
        LinkedHashMap<String, Object> options = new LinkedHashMap<>();
        options.put("sessionId ", sessionId);
        return this.join(roomName, options);
    }

    private Room createRoomRequest(final String roomName, LinkedHashMap<String, Object> options) {
//        System.out.println("createRoomRequest(" + roomName + "," + options + "," + reuseRoomInstance + "," + retryTimes + "," + retryCount);
        if (options == null) options = new LinkedHashMap<>();
        options.put("requestId", ++this.requestId);

        final Room room = createRoom(roomName, options);

        final LinkedHashMap<String, Object> finalOptions = options;
        room.addListener(new Room.Listener() {
            @Override
            public void onLeave() {
                rooms.remove(room.getId());
                connectingRooms.remove(finalOptions.get("requestId"));
            }
        });

        this.connectingRooms.put((int) options.get("requestId"), room);

        this.connection.send(Protocol.JOIN_ROOM, roomName, options);

        return room;
    }

    private Room createRoom(String roomName, LinkedHashMap<String, Object> options) {
        return new Room(roomName, options);
    }

    /**
     * List all available rooms to connect with the provided roomName. Locked rooms won't be listed.
     *
     * @param roomName room name
     * @param callback room callback
     */
    public void getAvailableRooms(String roomName, final GetAvailableRoomsCallback callback) {
        // reject this promise after 10 seconds.

        ++this.requestId;

        final int requestIdFinal = this.requestId;

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(10000);
                    if (availableRoomsRequests.containsKey(requestIdFinal)) {
                        availableRoomsRequests.remove(requestIdFinal);
                        callback.onCallback(null, "timeout");
                    }
                } catch (Exception ignored) {

                }
            }
        });

        // send the request to the server.
        this.connection.send(Protocol.ROOM_LIST, requestIdFinal, roomName);

        availableRoomsRequests.put(requestIdFinal, new AvailableRoomsRequestListener() {
            @Override
            public void callback(List<AvailableRoom> availableRooms) {
                availableRoomsRequests.remove(requestIdFinal);
                callback.onCallback(availableRooms, null);
            }
        });
    }

    /**
     * Close connection with the server.
     */
    public void close() {
        if(this.connection != null) {
            this.connection.close();
        }
    }

    public void close(boolean leaveRooms) {
        if(leaveRooms){
            for (Map.Entry<String, Room> entry : this.rooms.entrySet()){
               entry.getValue().leave();
            }
            this.rooms.clear();
            this.connectingRooms.clear();
        }
        close();
    }

    public void connect(){
        connect(options, connectTimeout);
    }

    private void connect(LinkedHashMap<String, Object> options, int connectTimeout) {
        if(connection != null){
            connection.setListener(null);
            connection.close();
        }
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
            public void onMessage(byte[] bytes) {
                Client.this.onMessageCallback(bytes);
            }
        });
    }

    private String buildEndpoint(String path, LinkedHashMap<String, Object> options) throws UnsupportedEncodingException, JsonProcessingException {
        // append colyseusid to connection string.
        StringBuilder sb = new StringBuilder();
        for (String name : options.keySet()) {
            sb.append("&");
            sb.append(URLEncoder.encode(name, StandardCharsets.UTF_8.name()));
            sb.append("=");
            sb.append(URLEncoder.encode(defaultMapper.writeValueAsString(options.get(name)), StandardCharsets.UTF_8.name()));
        }
        return this.hostname + "/" + path + "?colyseusid=" + URLEncoder.encode(this.id == null ? "" : this.id, StandardCharsets.UTF_8.name()) + sb.toString();
    }

    private void onMessageCallback(byte[] bytes) {
//        System.out.println("Client.onMessageCallback()");
        try {
            MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(bytes);
            Value val = unpacker.unpackValue();
            if (val.getValueType() == ValueType.ARRAY) {
                ArrayValue arrayValue = val.asArrayValue();
                Value codeValue = arrayValue.get(0);
                if (codeValue.getValueType() == ValueType.INTEGER) {
                    int code = codeValue.asIntegerValue().asInt();
                    switch (code) {
                        case Protocol.USER_ID: {
//                            System.out.println("Protocol: USER_ID");
                            this.id = arrayValue.get(1).asStringValue().asString();
//                            System.out.println("colyseus id : " + this.id);
                            if (Client.this.listener != null) {
                                Client.this.listener.onOpen(this.id);
                            }
                        }
                        break;
                        case Protocol.JOIN_ROOM: {
//                            System.out.println("Protocol: JOIN_ROOM");
                            int requestId = arrayValue.get(2).asIntegerValue().asInt();
//                            System.out.println("requestId: " + requestId);
                            Room room = this.connectingRooms.get(requestId);
                            if (room == null) {
                                System.out.println("client left room before receiving session id.");
                                return;
                            }
                            room.setId(arrayValue.get(1).asStringValue().asString());
//                            System.out.println("room.id: " + room.getId());
                            this.rooms.put(room.getId(), room);
                            room.connect(buildEndpoint(room.getId(), room.getOptions()), httpHeaders, this.connectTimeout);
                            connectingRooms.remove(requestId);
                        }
                        break;
                        case Protocol.JOIN_ERROR: {
//                            System.out.println("Protocol: JOIN_ERROR");
                            System.err.println("colyseus: server error: " + arrayValue.get(2).toString());
                            // general error
                            if (this.listener != null)
                                this.listener.onError(new Exception(arrayValue.get(2).toString()));
                        }
                        break;
                        case Protocol.ROOM_LIST: {
//                            System.out.println("Protocol: ROOM_LIST");
                            int id = arrayValue.get(1).asIntegerValue().asInt();
//                            System.out.println("id: " + id);
                            ArrayValue roomsArrayValue = arrayValue.get(2).asArrayValue();
                            List<AvailableRoom> availableRooms = new ArrayList<>();
                            for (int i = 0; i < roomsArrayValue.size(); i++) {
                                MapValue roomMapValue = roomsArrayValue.get(i).asMapValue();
                                AvailableRoom room = new AvailableRoom();
                                for (Map.Entry<Value, Value> entry : roomMapValue.entrySet()) {
                                    switch (entry.getKey().asStringValue().asString()) {
                                        case "clients":
                                            room.clients = entry.getValue().asIntegerValue().asInt();
                                            break;
                                        case "maxClients":
                                            room.maxClients = entry.getValue().asIntegerValue().asInt();
                                            break;
                                        case "roomId":
                                            room.roomId = entry.getValue().asStringValue().asString();
                                            break;
                                        case "metadata":
                                            room.metadata = entry.getValue();
                                            break;
                                    }
                                }
                                availableRooms.add(room);
                            }
                            if (this.availableRoomsRequests.containsKey(id)) {
                                this.availableRoomsRequests.get(id).callback(availableRooms);
                            } else {
                                System.out.println("receiving ROOM_LIST after timeout:" + roomsArrayValue);
                            }
                        }
                        break;
                        default: {
                            // message is array, first element is integer but it is not a Protocol code
                            dispatchOnMessage(bytes);
                        }
                    }
                } else {
                    // message is array but first element is not integer
                    dispatchOnMessage(bytes);
                }
            } else {
                // message is not an array
                dispatchOnMessage(bytes);
            }
        } catch (Exception e) {
            if (this.listener != null) this.listener.onError(e);
        }
    }

    private void dispatchOnMessage(byte[] bytes) throws IOException {
        if (Client.this.listener != null)
            Client.this.listener.onMessage(msgpackMapper.readValue(bytes, new TypeReference<Object>() {
            }));
    }

    public void setListener(Listener listener){
        this.listener = listener;
    }
}
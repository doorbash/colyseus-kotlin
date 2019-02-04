package io.colyseus;

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
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Client {

    private String id;
    private Map<String, String> httpHeaders;

    public interface Listener {
        void onOpen();

        void onMessage(Object message);

        void onClose(int code, String reason, boolean remote);

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

    // signals
    private Connection connection;
    private LinkedHashMap<String, Room> rooms = new LinkedHashMap<>();
    private LinkedHashMap<Integer, Room> connectingRooms = new LinkedHashMap<>();
    private int requestId = 0;
    private String hostname;
    private LinkedHashMap<Integer, AvailableRoomsRequestListener> roomsAvailableRequests = new LinkedHashMap<>();
    private Listener listener;
    private ObjectMapper objectMapper;

    public Client(String url, Listener listener) throws URISyntaxException {
        this(url, null, null, listener);
    }

    public Client(String url, Map<String, String> httpHeaders, Listener listener) throws URISyntaxException {
        this(url, null, httpHeaders, listener);
    }

    public Client(String url, LinkedHashMap<String, Object> options, Listener listener) throws URISyntaxException {
        this(url, options, null, listener);
    }

    public Client(String url, LinkedHashMap<String, Object> options, Map<String, String> httpHeaders, Listener listener) throws URISyntaxException {
        this.hostname = url;
        this.httpHeaders = httpHeaders == null ? new LinkedHashMap<>() : httpHeaders;
        this.listener = listener;
        this.objectMapper = new ObjectMapper(new MessagePackFactory());
        this.connect(null, options == null ? new LinkedHashMap<>() : options);
    }

    public Room join(String roomName) {
        return this.createRoomRequest(roomName, null, null, 0, 0);
    }

    public Room join(String roomName, LinkedHashMap<String, Object> options) {
        return this.createRoomRequest(roomName, options, null, 0, 0);
    }

    public Room join(String roomName, LinkedHashMap<String, Object> options, int retryTimes) {
        return this.createRoomRequest(roomName, options, null, retryTimes, 0);
    }

    public Room rejoin(String roomName, String sessionId) {
        LinkedHashMap<String, Object> options = new LinkedHashMap<>();
        options.put("sessionId ", sessionId);
        return this.join(roomName, options);
    }

    private Room createRoomRequest(String roomName, LinkedHashMap<String, Object> options, Room reuseRoomInstance, int retryTimes, final int retryCount) {
        System.out.println("createRoomRequest(" + roomName + "," + options + "," + reuseRoomInstance + "," + retryTimes + "," + retryCount);
        if (options == null) options = new LinkedHashMap<>();
        options.put("requestId", ++this.requestId);

        if (retryTimes > 0) options.put("retryTimes", retryTimes); // ?

        Room room = reuseRoomInstance == null ? this.createRoom(roomName, options) : reuseRoomInstance;

        LinkedHashMap<String, Object> finalOptions = options;
        room.addListener(new Room.RoomListener(true) {
            @Override
            public void onLeave() {
                rooms.remove(room.getId());
                connectingRooms.remove(finalOptions.get("requestId"));
            }
        });

        if (retryTimes > 0) {
            room.addListener(new Room.RoomListener(true) {
                @Override
                public void onError(Exception e) {
                    System.out.println("////////////////////////////////////////////");
                    if (!room.hasJoined() && retryCount <= retryTimes) {
                        createRoomRequest(roomName, finalOptions, room, retryTimes, retryCount + 1);
                    }
                }
            });
        }

        this.connectingRooms.put((int) options.get("requestId"), room);

        this.connection.send(Protocol.JOIN_ROOM, roomName, options);

        return room;
    }

    private Room createRoom(String roomName, LinkedHashMap<String, Object> options) {
        return new Room(roomName, options);
    }

    public void getAvailableRooms(String roomName, GetAvailableRoomsCallback callback) {
        // reject this promise after 10 seconds.
        int requestId = ++this.requestId;

        new Thread(() -> {
            try {
                Thread.sleep(10000);
                if (roomsAvailableRequests.containsKey(requestId)) {
                    roomsAvailableRequests.remove(requestId);
                    callback.onCallback(null, "timeout");
                }
            } catch (Exception e) {
                System.err.println(e);
            }
        }).start();

        // send the request to the server.
        this.connection.send(Protocol.ROOM_LIST, requestId, roomName);

        roomsAvailableRequests.put(requestId, new AvailableRoomsRequestListener() {
            @Override
            public void callback(List<AvailableRoom> roomsAvailable) {
                roomsAvailableRequests.remove(requestId);
                callback.onCallback(roomsAvailable, null);
            }
        });
    }

    public void close() {
        this.connection.close();
    }

    private void connect(String colyseusid, LinkedHashMap<String, Object> options) throws URISyntaxException {

        if (colyseusid != null) this.id = colyseusid;

        this.connection = new Connection(buildEndpoint("", options), 10000, httpHeaders, new Connection.Listener() {
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
                    Client.this.listener.onOpen();
            }

            @Override
            public void onMessage(byte[] bytes) {
                if (Client.this.listener != null)
                    Client.this.onMessageCallback(bytes);
            }
        });
    }

    private String buildEndpoint(String path, LinkedHashMap<String, Object> options) {
        // append colyseusid to connection string.
        List<String> params = new ArrayList<>();
        for (String name : options.keySet()) {
            params.add(name + "=" + options.get(name).toString());
        }
        StringBuilder sb = new StringBuilder();
        for (String s : params) {
            sb.append("&");
            sb.append(s);
        }
        return this.hostname + "/" + path + "?colyseusid=" + (this.id == null ? "" : this.id) + sb.toString();
    }

    private void onMessageCallback(byte[] bytes) {
        System.out.println("Client.onMessageCallback()");
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
                            //TODO: store user id
                            System.out.println("Protocol: USER_ID");
                            this.id = arrayValue.get(1).asStringValue().asString();
                            System.out.println("colyseus id : " + this.id);
                            if (Client.this.listener != null) Client.this.listener.onOpen();
                        }
                        break;
                        case Protocol.JOIN_ROOM: {
                            System.out.println("Protocol: JOIN_ROOM");
                            int requestId = arrayValue.get(2).asIntegerValue().asInt();
                            System.out.println("requestId: " + requestId);
                            Room room = this.connectingRooms.get(requestId);
                            if (room == null) {
                                System.out.println("client left room before receiving session id.");
                                return;
                            }
                            room.setId(arrayValue.get(1).asStringValue().asString());
                            System.out.println("room.id: " + room.getId());
                            this.rooms.put(room.getId(), room);
                            room.connect(buildEndpoint(room.getId(), room.getOptions()), httpHeaders);
                            connectingRooms.remove(requestId);
                        }
                        break;
                        case Protocol.JOIN_ERROR: {
                            System.out.println("Protocol: JOIN_ERROR");
                            System.err.println("colyseus.js: server error: + " + arrayValue.get(2).toString());
                            // general error
                            if (this.listener != null)
                                this.listener.onError(new Exception(arrayValue.get(2).toString()));
                        }
                        break;
                        case Protocol.ROOM_LIST: {
                            System.out.println("Protocol: ROOM_LIST");
                            int id = arrayValue.get(1).asIntegerValue().asInt();
                            System.out.println("id: " + id);
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
                            if (this.roomsAvailableRequests.containsKey(id)) {
                                this.roomsAvailableRequests.get(id).callback(availableRooms);
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
            e.printStackTrace();
        }
    }

    private void dispatchOnMessage(byte[] bytes) throws IOException {
        if (Client.this.listener != null)
            Client.this.listener.onMessage(objectMapper.readValue(bytes, new TypeReference<Object>() {
            }));
    }
}
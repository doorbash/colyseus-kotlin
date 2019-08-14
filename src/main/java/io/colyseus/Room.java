package io.colyseus;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.colyseus.serializer.SchemaSerializer;
import io.colyseus.serializer.schema.Schema;

import org.java_websocket.framing.CloseFrame;
import org.msgpack.jackson.dataformat.MessagePackFactory;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Room<T extends Schema> {

    public abstract static class Listener<T extends Schema> {

        protected Listener() {

        }

        /**
         * This event is triggered when the client leave the room.
         */
        protected void onLeave() {

        }

        /**
         * This event is triggered when some error occurs in the room handler.
         */
        protected void onError(Exception e) {

        }

        /**
         * This event is triggered when the server sends a message directly to the client.
         */
        protected void onMessage(Object message) {

        }

        /**
         * This event is triggered when the client successfuly joins the room.
         */
        protected void onJoin() {

        }

        /**
         * This event is triggered when the server updates its state.
         */
        protected void onStateChange(T state, boolean isFirstState) {

        }
    }

    public T state;

    private Class<Schema> stateType;

    private LinkedHashMap<String, Object> options;

    /**
     * The unique identifier of the room.
     */
    private String id;

    /**
     * Unique session identifier.
     */
    private String sessionId;

    /**
     * Name of the room handler. Ex: "battle".
     */

    private String name;


    private Client client;
    private List<Listener> listeners = new ArrayList<>();
    private Connection connection;
    private byte[] _previousState;
    private ObjectMapper msgpackMapper;
    private SchemaSerializer<Schema> serializer;
    private int previousCode;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    String getId() {
        return id;
    }

    void setId(String id) {
        this.id = id;
    }

    LinkedHashMap<String, Object> getOptions() {
        return options;
    }

    public void setOptions(LinkedHashMap<String, Object> options) {
        this.options = options;
    }

    public void setConnection(LinkedHashMap<String, Object> options) {
        this.options = options;
    }

//    public LinkedHashMap<String, Object> getState() {
//        return state;
//    }

    Room(Class<Schema> type, Client client, String roomName, LinkedHashMap<String, Object> options) {
//        super(new LinkedHashMap<String, Object>());
//        System.out.println("Room created: name: " + roomName + ", options: " + options);
        this.stateType = type;
        this.client = client;
        this.name = roomName;
        this.options = options;
        this.msgpackMapper = new ObjectMapper(new MessagePackFactory());
        try {
            serializer = new SchemaSerializer<>(stateType);
            state = (T) serializer.state;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addListener(Listener listener) {
        this.listeners.add(listener);
    }

    public void removeListener(Listener listener) {
        this.listeners.remove(listener);
    }

    void connect(String endpoint, Map<String, String> httpHeaders, int connectTimeout) throws URISyntaxException {
//        System.out.println("Room is connecting to " + endpoint);
        this.connection = new Connection(new URI(endpoint), connectTimeout, httpHeaders, new Connection.Listener() {
            @Override
            public void onError(Exception e) {
                //System.err.println("Possible causes: room's onAuth() failed or maxClients has been reached.");
                for (Listener listener : listeners) {
                    if (listener != null) listener.onError(e);
                }
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                if (code == CloseFrame.PROTOCOL_ERROR && reason != null && reason.startsWith("Invalid status code received: 401")) {
                    for (Listener listener : listeners) {
                        if (listener != null) listener.onError(new Exception(reason));
                    }
                }
                for (Listener listener : listeners) {
                    if (listener != null) listener.onLeave();
                }
                client.onRoomLeave(id);
//                removeAllListeners();
            }

            @Override
            public void onOpen() {

            }

            @Override
            public void onMessage(ByteBuffer buf) {
                Room.this.onMessageCallback(buf);
            }
        });
    }

    private void onMessageCallback(ByteBuffer buf) {
        try {
            if (previousCode == 0) {
                byte code = buf.get();

                if (code == Protocol.JOIN_ROOM) {
                    byte[] bytes = new byte[buf.get()];
                    buf.get(bytes, 0, bytes.length);
                    sessionId = new String(bytes, StandardCharsets.UTF_8);
                    bytes = new byte[buf.get()];
                    buf.get(bytes, 0, bytes.length);
                    String serializerId = new String(bytes, StandardCharsets.UTF_8);
                    if (serializerId.equals("fossil-delta")) {
                        throw new Error("fossil-delta is not supported");
                    }
                    if (buf.hasRemaining()) {
                        byte[] b = new byte[buf.remaining()];
                        buf.get(b, 0, b.length);
                        serializer.handshake(b);
                    }
                    for (Listener listener : listeners) {
                        if (listener != null) listener.onJoin();
                    }
                } else if (code == Protocol.JOIN_ERROR) {
                    int length = buf.get();
                    byte[] bytes = new byte[length];
                    buf.get(bytes, 0, length);
                    String message = new String(bytes, StandardCharsets.UTF_8);
                    for (Listener listener : listeners) {
                        if (listener != null) listener.onError(new Exception(message));
                    }
                } else if (code == Protocol.LEAVE_ROOM) {
                    leave();
                } else {
                    previousCode = code;
                }

            } else {
                if (buf.hasRemaining()) {
                    if (previousCode == Protocol.ROOM_STATE) {
                        byte[] bytes = new byte[buf.remaining()];
                        buf.get(bytes);
                        setState(bytes);
                    } else if (previousCode == Protocol.ROOM_STATE_PATCH) {
                        byte[] bytes = new byte[buf.remaining()];
                        buf.get(bytes);
                        patch(bytes);
                    } else if (previousCode == Protocol.ROOM_DATA) {
                        byte[] bytes = new byte[buf.remaining()];
                        buf.get(bytes);
                        Object data = msgpackMapper.readValue(bytes, new TypeReference<Object>() {
                        });
                        for (Listener listener : listeners) {
                            if (listener != null) listener.onMessage(data);
                        }
                    }
                }
                previousCode = 0;
            }
        } catch (Exception e) {
            for (Listener listener : listeners) {
                if (listener != null) listener.onError(e);
            }
        }
    }

    private void dispatchOnMessage(Object message) {
        for (Listener listener : listeners) {
            if (listener != null) listener.onMessage(message);
        }
    }

//    public void removeAllListeners() {
//        this.listeners.clear();
//    }

    /**
     * Disconnect from the room.
     */
    public void leave() {
        leave(true);
    }

    public void leave(boolean consented) {
        if (id != null) {
            if (consented) {
                connection.send(Protocol.LEAVE_ROOM);
            } else {
                connection.close();
            }

        } else {
            for (Listener listener : listeners) {
                if (listener != null) listener.onLeave();
            }
        }
    }

    /**
     * Send message to the room handler.
     */
    public void send(Object data) {
        if (this.connection != null)
            this.connection.send(Protocol.ROOM_DATA, this.id, data);
        else {
            // room is created but not joined yet
            for (Listener listener : listeners) {
                if (listener != null) listener.onError(new Exception("send error: Room is created but not joined yet"));
            }
        }
    }


    public boolean hasJoined() {
        return this.sessionId != null;
    }

    private void setState(byte[] encodedState) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, NoSuchFieldException {
        serializer.setState(encodedState);
        for (Listener listener : listeners) {
            if (listener != null) listener.onStateChange(serializer.state._clone(), true);
        }
    }

    private void patch(byte[] delta) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, NoSuchFieldException {
        serializer.patch(delta);
        for (Listener listener : listeners) {
            if (listener != null) listener.onStateChange(serializer.state, false);
        }
    }
}

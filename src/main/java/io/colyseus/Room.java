package io.colyseus;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.colyseus.fossil_delta.FossilDelta;
import io.colyseus.state_listener.StateContainer;
import org.msgpack.jackson.dataformat.MessagePackFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Room extends StateContainer {

    public abstract static class Listener {

        boolean once = false;

        protected Listener() {

        }

        protected Listener(boolean once) {
            this.once = once;
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
        protected void onStateChange(LinkedHashMap<String, Object> state) {

        }
    }

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
    private List<Listener> listeners = new ArrayList<>();
    private Connection connection;
    private byte[] _previousState;
    private ObjectMapper objectMapper;

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

    public LinkedHashMap<String, Object> getState() {
        return state;
    }

    Room(String roomName, LinkedHashMap<String, Object> options) {
        super(new LinkedHashMap<String, Object>());
//        System.out.println("Room created: name: " + roomName + ", options: " + options);
        this.name = roomName;
        this.options = options;
        this.objectMapper = new ObjectMapper(new MessagePackFactory());
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
                List<Listener> toRemove = new ArrayList<>();
                for (Listener listener : listeners) {
                    listener.onError(e);
                    if (listener.once) toRemove.add(listener);
                }
                listeners.removeAll(toRemove);
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                if (code == 1002 && reason.equals("Invalid status code received: 401 Status line: HTTP/1.1 401 Unauthorized")) {
                    List<Listener> toRemove = new ArrayList<>();
                    for (Listener listener : listeners) {
                        listener.onError(new Exception(reason));
                        if (listener.once) toRemove.add(listener);
                    }
                    listeners.removeAll(toRemove);
                }
                List<Listener> toRemove = new ArrayList<>();
                for (Listener listener : listeners) {
                    listener.onLeave();
                    if (listener.once) toRemove.add(listener);
                }
                listeners.removeAll(toRemove);
                removeAllListeners();
            }

            @Override
            public void onOpen() {

            }

            @Override
            public void onMessage(byte[] bytes) {
                Room.this.onMessageCallback(bytes);
            }
        });
    }


    public void onMessageCallback(byte[] bytes) {
//        System.out.println("Room.onMessageCallback()");
        try {
            Object message = objectMapper.readValue(bytes, new TypeReference<Object>() {
            });
            if (message instanceof List) {
                List<Object> messageArray = (List<Object>) message;
                if (messageArray.get(0) instanceof Integer) {
                    int code = (int) messageArray.get(0);
                    switch (code) {
                        case Protocol.JOIN_ROOM: {
                            sessionId = (String) messageArray.get(1);
                            List<Listener> toRemove = new ArrayList<>();
                            for (Listener listener : listeners) {
                                listener.onJoin();
                                if (listener.once) toRemove.add(listener);
                            }
                            listeners.removeAll(toRemove);
                        }
                        break;

                        case Protocol.JOIN_ERROR: {
                            System.err.println("Error: " + messageArray.get(1));
                            List<Listener> toRemove = new ArrayList<>();
                            for (Listener listener : listeners) {
                                listener.onError(new Exception(messageArray.get(1).toString()));
                                if (listener.once) toRemove.add(listener);
                            }
                            listeners.removeAll(toRemove);
                        }
                        break;

                        case Protocol.ROOM_STATE: {
//                    const remoteCurrentTime = message[2];
//                    const remoteElapsedTime = message[3];
                            setState((byte[]) messageArray.get(1));
                        }
                        break;

                        case Protocol.ROOM_STATE_PATCH: {
                            patch((ArrayList<Integer>) messageArray.get(1));
                        }
                        break;

                        case Protocol.ROOM_DATA: {
                            List<Listener> toRemove = new ArrayList<>();
                            for (Listener listener : listeners) {
                                listener.onMessage(messageArray.get(1));
                                if (listener.once) toRemove.add(listener);
                            }
                            listeners.removeAll(toRemove);
                        }
                        break;

                        case Protocol.LEAVE_ROOM: {
                            leave();
                        }
                        break;

                        default:
                            dispatchOnMessage(message);
                    }
                } else dispatchOnMessage(message);
            } else dispatchOnMessage(message);
        } catch (Exception e) {
            e.printStackTrace();
            List<Listener> toRemove = new ArrayList<>();
            for (Listener listener : listeners) {
                listener.onError(e);
                if (listener.once) toRemove.add(listener);
            }
            listeners.removeAll(toRemove);
        }
    }

    private void dispatchOnMessage(Object message) {
        List<Listener> toRemove = new ArrayList<>();
        for (Listener listener : listeners) {
            listener.onMessage(message);
            if (listener.once) toRemove.add(listener);
        }
        listeners.removeAll(toRemove);
    }

    /**
     * Remove all event and data listeners.
     */
    @Override
    public void removeAllListeners() {
        super.removeAllListeners();
        this.listeners.clear();
    }

    /**
     * Disconnect from the room.
     */
    public void leave() {
        if (this.connection != null) {
            this.connection.send(Protocol.LEAVE_ROOM);
        } else {
            List<Listener> toRemove = new ArrayList<>();
            for (Listener listener : listeners) {
                listener.onLeave();
                if (listener.once) toRemove.add(listener);
            }
            listeners.removeAll(toRemove);
        }
    }

    /**
     * Send message to the room handler.
     */
    public void send(Object data) {
        if (this.connection != null)
            this.connection.send(Protocol.ROOM_DATA, this.id, data);
        // room is created but not joined yet
        List<Listener> toRemove = new ArrayList<>();
        for (Listener listener : listeners) {
            listener.onError(new Exception("send error: Room is created but not joined yet"));
            if (listener.once) toRemove.add(listener);
        }
        listeners.removeAll(toRemove);
    }


    public boolean hasJoined() {
        return this.sessionId != null;
    }

    private void setState(byte[] encodedState) throws IOException {
        this.set((LinkedHashMap<String, Object>) objectMapper.readValue(encodedState, Object.class));
        this._previousState = encodedState;
        List<Listener> toRemove = new ArrayList<>();
        for (Listener listener : listeners) {
            listener.onStateChange(this.state);
            if (listener.once) toRemove.add(listener);
        }
        listeners.removeAll(toRemove);
    }

    private void patch(ArrayList<Integer> binaryPatch) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (int i = 0; i < binaryPatch.size(); ++i) {
            baos.write(binaryPatch.get(i) & 0xFF);
        }
        this._previousState = FossilDelta.apply(this._previousState, baos.toByteArray());
        this.set((LinkedHashMap<String, Object>) objectMapper.readValue(this._previousState, Object.class));
        List<Listener> toRemove = new ArrayList<>();
        for (Listener listener : listeners) {
            listener.onStateChange(this.state);
            if (listener.once) toRemove.add(listener);
        }
        listeners.removeAll(toRemove);
    }
}

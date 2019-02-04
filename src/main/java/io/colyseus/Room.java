package io.colyseus;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.colyseus.fossil_delta.FossilDelta;
import io.colyseus.state_listener.StateContainer;
import org.msgpack.jackson.dataformat.MessagePackFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Room extends StateContainer {

    public abstract static class RoomListener {

        boolean once = false;

        protected RoomListener() {

        }

        protected RoomListener(boolean once) {
            this.once = once;
        }

        protected void onLeave() {

        }

        protected void onError(Exception e) {

        }

        protected void onMessage(Object message) {

        }

        protected void onJoin() {

        }

        protected void onStateChange(LinkedHashMap<String, Object> state) {

        }
    }

    private LinkedHashMap<String, Object> options;
    private String id;
    private String sessionId;
    private String name;
    private List<RoomListener> listeners = new ArrayList<>();
    private Connection connection;
    private byte[] _previousState;
    private ObjectMapper objectMapper;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSessionId(){
        return sessionId;
    }

    public void setSessionId(String sessionId){
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

    public LinkedHashMap<String,Object> getState() {
        return state;
    }

    Room(String roomName, LinkedHashMap<String, Object> options) {
        super(new LinkedHashMap<String, Object>());
//        System.out.println("Room created: name: " + roomName + ", options: " + options);
        this.name = roomName;
        this.options = options;
        this.objectMapper = new ObjectMapper(new MessagePackFactory());
    }

    public void addListener(RoomListener listener) {
        this.listeners.add(listener);
    }

    public void removeListener(RoomListener listener) {
        this.listeners.remove(listener);
    }

    void connect(String endpoint, Map<String, String> httpHeaders) throws Exception {
//        System.out.println("Room is connecting to " + endpoint);
        this.connection = new Connection(endpoint, 10000, httpHeaders, new Connection.Listener() {
            @Override
            public void onError(Exception e) {
                System.err.println("Possible causes: room's onAuth() failed or maxClients has been reached.");
                List<RoomListener> toRemove = new ArrayList<>();
                for (RoomListener listener : listeners) {
                    listener.onError(e);
                    if (listener.once) toRemove.add(listener);
                }
                listeners.removeAll(toRemove);
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                removeAllListeners();
                List<RoomListener> toRemove = new ArrayList<>();
                for (RoomListener listener : listeners) {
                    listener.onLeave();
                    if (listener.once) toRemove.add(listener);
                }
                listeners.removeAll(toRemove);
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
            Object message = objectMapper.readValue(bytes, new TypeReference<Object[]>() {
            });
            if(message instanceof Object[]) {
                Object[] messageArray = (Object[]) message;
                if(messageArray[0] instanceof Integer) {
                    int code = (int) messageArray[0];
                    switch (code) {
                        case Protocol.JOIN_ROOM: {
                            sessionId = (String) messageArray[1];
                            List<RoomListener> toRemove = new ArrayList<>();
                            for (RoomListener listener : listeners) {
                                listener.onJoin();
                                if (listener.once) toRemove.add(listener);
                            }
                            listeners.removeAll(toRemove);
                        }
                        break;

                        case Protocol.JOIN_ERROR: {
                            System.err.println("Error: " + messageArray[1]);
                            List<RoomListener> toRemove = new ArrayList<>();
                            for (RoomListener listener : listeners) {
                                listener.onError(new Exception(messageArray[1].toString()));
                                if (listener.once) toRemove.add(listener);
                            }
                            listeners.removeAll(toRemove);
                        }
                        break;

                        case Protocol.ROOM_STATE: {
//                    const remoteCurrentTime = message[2];
//                    const remoteElapsedTime = message[3];
                            setState((byte[]) messageArray[1]);
                        }
                        break;

                        case Protocol.ROOM_STATE_PATCH: {
                            patch((ArrayList<Integer>) messageArray[1]);
                        }
                        break;

                        case Protocol.ROOM_DATA: {
                            List<RoomListener> toRemove = new ArrayList<>();
                            for (RoomListener listener : listeners) {
                                listener.onMessage(messageArray[1]);
                                if (listener.once) toRemove.add(listener);
                            }
                            listeners.removeAll(toRemove);
                        }
                        break;

                        case Protocol.LEAVE_ROOM: {
                            leave();
                        }
                        break;

                        default: dispatchOnMessage(message);
                    }
                } else dispatchOnMessage(message);
            } else dispatchOnMessage(message);
        } catch (Exception e) {
            e.printStackTrace();
            List<RoomListener> toRemove = new ArrayList<>();
            for (RoomListener listener : listeners) {
                listener.onError(e);
                if (listener.once) toRemove.add(listener);
            }
            listeners.removeAll(toRemove);
        }
    }

    private void dispatchOnMessage(Object message) {
        List<RoomListener> toRemove = new ArrayList<>();
        for (RoomListener listener : listeners) {
            listener.onMessage(message);
            if (listener.once) toRemove.add(listener);
        }
        listeners.removeAll(toRemove);
    }

    @Override
    public void removeAllListeners() {
        super.removeAllListeners();
        this.listeners.clear();
    }

    public void leave() {
        if (this.connection != null) {
            this.connection.send(Protocol.LEAVE_ROOM);
        } else {
            List<RoomListener> toRemove = new ArrayList<>();
            for (RoomListener listener : listeners) {
                listener.onLeave();
                if (listener.once) toRemove.add(listener);
            }
            listeners.removeAll(toRemove);
        }
    }

    public void send(Object data) {
        this.connection.send(Protocol.ROOM_DATA, this.id, data);
    }


    public boolean hasJoined() {
        return this.sessionId != null;
    }

    protected void setState(byte[] encodedState) throws IOException {
        this.set((LinkedHashMap<String, Object>) objectMapper.readValue(encodedState, Object.class));
        this._previousState = encodedState;
        List<RoomListener> toRemove = new ArrayList<>();
        for (RoomListener listener : listeners) {
            listener.onStateChange(this.state);
            if (listener.once) toRemove.add(listener);
        }
        listeners.removeAll(toRemove);
    }

    protected void patch(ArrayList<Integer> binaryPatch) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for(int i=0; i < binaryPatch.size(); ++i)
        {
            baos.write(binaryPatch.get(i) & 0xFF);
        }
        this._previousState = FossilDelta.apply(this._previousState, baos.toByteArray());
        this.set((LinkedHashMap<String, Object>) objectMapper.readValue(this._previousState,Object.class));
        List<RoomListener> toRemove = new ArrayList<>();
        for (RoomListener listener : listeners) {
            listener.onStateChange(this.state);
            if (listener.once) toRemove.add(listener);
        }
        listeners.removeAll(toRemove);
    }
}

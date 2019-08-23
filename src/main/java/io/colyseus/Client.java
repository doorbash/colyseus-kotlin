package io.colyseus;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.colyseus.serializer.schema.Schema;
import io.colyseus.util.Http;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class Client {
    private String endpoint;
    private ThreadPoolExecutor threadPoolExecutor;
    private ObjectMapper objectMapper;

    public Client(String endpoint) {
        this.endpoint = endpoint;
        threadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(5);
        objectMapper = new ObjectMapper();
    }

    public interface RoomCallback<T extends Schema> {
        public void onResult(Room<T> room);
    }

    public interface OnError {
        public void onError(Exception e);
    }

    public static class AvailableRoom {
        public String roomId;
        public int clients;
        public int maxClients;
        public Object metadata;
    }

    public interface AvailableRoomsCallback {
        public void onResult(List<AvailableRoom> rooms);
    }

    public <T extends Schema> void joinOrCreate(String roomName, Class<T> rootSchema, RoomCallback<T> callback, OnError onError) {
        joinOrCreate(roomName, rootSchema, null, null, null, callback, onError);
    }

    public <T extends Schema> void joinOrCreate(String roomName, Class<T> rootSchema, LinkedHashMap<String, Object> options, RoomCallback<T> callback, OnError onError) {
        joinOrCreate(roomName, rootSchema, options, null, null, callback, onError);
    }

    public <T extends Schema> void joinOrCreate(String roomName, Class<T> rootSchema, LinkedHashMap<String, Object> options, Map<String, String> httpHeaders, Map<String, String> wsHeaders, RoomCallback<T> callback, OnError onError) {
        threadPoolExecutor.execute(() -> this.createMatchMakeRequest("joinOrCreate", roomName, options, rootSchema, httpHeaders, wsHeaders, callback, onError));
    }

    public <T extends Schema> void create(String roomName, Class<T> rootSchema, RoomCallback<T> callback, OnError onError) {
        create(roomName, rootSchema, null, null, null, callback, onError);
    }

    public <T extends Schema> void create(String roomName, Class<T> rootSchema, LinkedHashMap<String, Object> options, RoomCallback<T> callback, OnError onError) {
        create(roomName, rootSchema, options, null, null, callback, onError);
    }

    public <T extends Schema> void create(String roomName, Class<T> rootSchema, LinkedHashMap<String, Object> options, Map<String, String> httpHeaders, Map<String, String> wsHeaders, RoomCallback<T> callback, OnError onError) {
        threadPoolExecutor.execute(() -> this.createMatchMakeRequest("create", roomName, options, rootSchema, httpHeaders, wsHeaders, callback, onError));
    }

    public <T extends Schema> void join(String roomName, Class<T> rootSchema, RoomCallback<T> callback, OnError onError) {
        join(roomName, rootSchema, null, null, null, callback, onError);
    }

    public <T extends Schema> void join(String roomName, Class<T> rootSchema, LinkedHashMap<String, Object> options, RoomCallback<T> callback, OnError onError) {
        join(roomName, rootSchema, options, null, null, callback, onError);
    }

    public <T extends Schema> void join(String roomName, Class<T> rootSchema, LinkedHashMap<String, Object> options, Map<String, String> httpHeaders, Map<String, String> wsHeaders, RoomCallback<T> callback, OnError onError) {
        threadPoolExecutor.execute(() -> this.createMatchMakeRequest("join", roomName, options, rootSchema, httpHeaders, wsHeaders, callback, onError));
    }

    public <T extends Schema> void joinById(String roomId, Class<T> rootSchema, RoomCallback<T> callback, OnError onError) {
        joinById(roomId, rootSchema, null, null, null, callback, onError);
    }

    public <T extends Schema> void joinById(String roomId, Class<T> rootSchema, LinkedHashMap<String, Object> options, RoomCallback<T> callback, OnError onError) {
        joinById(roomId, rootSchema, options, null, null, callback, onError);
    }

    public <T extends Schema> void joinById(String roomId, Class<T> rootSchema, LinkedHashMap<String, Object> options, Map<String, String> httpHeaders, Map<String, String> wsHeaders, RoomCallback<T> callback, OnError onError) {
        threadPoolExecutor.execute(() -> this.createMatchMakeRequest("joinById", roomId, options, rootSchema, httpHeaders, wsHeaders, callback, onError));
    }

    public <T extends Schema> void reconnect(String roomId, String sessionId, Class<T> rootSchema, RoomCallback<T> callback, OnError onError) {
        reconnect(roomId, sessionId, rootSchema, null, null, callback, onError);
    }

    public <T extends Schema> void reconnect(String roomId, String sessionId, Class<T> rootSchema, Map<String, String> httpHeaders, Map<String, String> wsHeaders, RoomCallback<T> callback, OnError onError) {
        LinkedHashMap<String, Object> options = new LinkedHashMap<>();
        options.put("sessionId", sessionId);
        threadPoolExecutor.execute(() -> this.createMatchMakeRequest("joinById", roomId, options, rootSchema, httpHeaders, wsHeaders, callback, onError));
    }

    public void getAvailableRooms(String roomName, AvailableRoomsCallback callback, OnError onError) {
        threadPoolExecutor.execute(() -> {
            try {
                String url = endpoint.replace("ws", "http") + "/matchmake/" + (roomName == null ? "" : roomName);
                LinkedHashMap<String, String> httpHeaders = new LinkedHashMap<>();
                httpHeaders.put("Accept", "application/json");
                String response = Http.request(url, "GET", httpHeaders);
                List<AvailableRoom> data = objectMapper.readValue(response, ArrayList.class);
                callback.onResult(data);
            } catch (Exception e) {
                onError.onError(e);
            }
        });
    }

    private <T extends Schema> void createMatchMakeRequest(String method, String roomName, LinkedHashMap<String, Object> options, Class<T> rootType, Map<String, String> httpHeaders, Map<String, String> wsHeaders, RoomCallback callback, OnError onError) {
        try {
            String url = endpoint.replace("ws", "http") + "/matchmake/" + method + "/" + roomName;

            String body = null;
            if (options != null) {
                body = objectMapper.writeValueAsString(objectMapper.convertValue(options, JsonNode.class));
            }

            if (httpHeaders == null) httpHeaders = new LinkedHashMap<>();

            httpHeaders.put("Accept", "application/json");
            httpHeaders.put("Content-Type", "application/json");

            String res = Http.request(url, "POST", httpHeaders, body);

            System.out.println("response is " + res);

            JsonNode response = objectMapper.readValue(res, JsonNode.class);

            if (response.has("error")) {
                throw new MatchMakeException(response.get("error").asText(), response.get("error").asInt());
            }

            Room room = new Room<>(rootType, roomName);

            String roomId = response.get("room").get("roomId").asText();
            System.out.println("room id is " + roomId);
            room.setId(roomId);
            String sessionId = response.get("sessionId").asText();
            System.out.println("session id is " + sessionId);
            room.setSessionId(sessionId);
            room.setListener(new Room.Listener<T>() {
                @Override
                protected void onError(Exception e) {
                    onError.onError(e);
                }

                @Override
                protected void onJoin() {
                    room.setListener(null);
                    callback.onResult(room);
                }
            });
            LinkedHashMap<String, Object> wsOptions = new LinkedHashMap<>();
            wsOptions.put("sessionId", room.getSessionId());
            String wsUrl = buildEndpoint(response.get("room"), wsOptions);
            System.out.println("ws url is " + wsUrl);
            room.connect(wsUrl, wsHeaders);
        } catch (Exception e) {
            onError.onError(e);
        }
    }

    public static class MatchMakeException extends Exception {
        public int code;

        public MatchMakeException(String message, int code) {
            super(message);
            this.code = code;
        }
    }

    private String buildEndpoint(JsonNode room, LinkedHashMap<String, Object> options) throws UnsupportedEncodingException {
        String charset = "UTF-8";
        List<String> params = new ArrayList<>();
        for (String name : options.keySet()) {
            params.add(URLEncoder.encode(name, charset) + "=" + URLEncoder.encode(options.get(name).toString(), charset));
        }
        return this.endpoint + "/" + room.get("processId").asText() + "/" + room.get("roomId").asText() + "?" + String.join("&", params);
    }
}
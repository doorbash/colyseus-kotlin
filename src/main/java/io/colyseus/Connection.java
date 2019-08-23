package io.colyseus;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;
import org.msgpack.jackson.dataformat.MessagePackFactory;

import java.net.URI;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.Map;

public class Connection extends WebSocketClient {
    private static final int CONNECT_TIMEOUT = 10000;

    interface Listener {
        void onError(Exception e);

        void onClose(int code, String reason, boolean remote);

        void onOpen();

        void onMessage(ByteBuffer bytes);
    }

    private LinkedList<Object[]> _enqueuedCalls = new LinkedList<>();
    private Listener listener;
    private ObjectMapper msgpackMapper;

    Connection(URI uri, Map<String, String> httpHeaders, Listener listener) {
        super(uri, new Draft_6455(), httpHeaders, CONNECT_TIMEOUT);
        System.out.println("Connection()");
        System.out.println("wsUrl is " + uri);
        this.listener = listener;
        this.msgpackMapper = new ObjectMapper(new MessagePackFactory());
        connect();
    }

    void send(Object... data) {
        if (isOpen()) {
            try {
                send(msgpackMapper.writeValueAsBytes(data));
            } catch (Exception e) {
                onError(e);
            }
        } else {
            // WebSocket not connected.
            // Enqueue data to be sent when readyState == OPEN
            this._enqueuedCalls.push(data);
        }
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        if (Connection.this._enqueuedCalls.size() > 0) {
            for (Object[] objects : Connection.this._enqueuedCalls) {
                Connection.this.send(objects);
            }

            // clear enqueued calls.
            Connection.this._enqueuedCalls.clear();
        }
        if (Connection.this.listener != null) Connection.this.listener.onOpen();
    }

    @Override
    public void onMessage(String message) {
//        System.out.println("Connection.onMessage(String message)");
//        System.out.println(message);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        if (Connection.this.listener != null) Connection.this.listener.onClose(code, reason, remote);
    }

    @Override
    public void onError(Exception ex) {
        if (Connection.this.listener != null) Connection.this.listener.onError(ex);
    }

    @Override
    public void onMessage(ByteBuffer buf) {
//        System.out.println("Connection.onMessage(ByteBuffer bytes)");
        if (Connection.this.listener != null) {
//            byte[] bytes = new byte[buf.capacity()];
//            buf.get(bytes, 0, bytes.length);
            Connection.this.listener.onMessage(buf);
        }
    }
}
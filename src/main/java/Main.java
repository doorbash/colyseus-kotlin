import io.colyseus.Client;
import io.colyseus.Room;
import io.colyseus.state_listener.*;

import java.net.URISyntaxException;
import java.util.LinkedHashMap;
import java.util.List;

public class Main {

    static Client client;
    static Room room;

    public static void main(String[] args) {
        try {
Client client = new Client("http://localhost:3000", new Client.Listener() {
    @Override
    public void onOpen() {
        System.out.println("Client.onOpen()");
    }

    @Override
    public void onMessage(Object message) {
        System.out.println("Client.onMessage()");
        System.out.println(message);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("Client.onClose()");
        System.out.println("code: " + code + ", reason: " + reason + ", remote: " + remote);
    }

    @Override
    public void onError(Exception e) {
        System.out.println("Client.onError()");
        e.printStackTrace();
    }
});
            room = client.join("public");
room.addListener(new Room.RoomListener() {
    @Override
    protected void onLeave() {
        System.out.println("Room.onLeave()");
    }

    @Override
    protected void onError(Exception e) {
        System.out.println("Room.onError()");
        System.out.println("exception: " + e.getMessage());
    }

    @Override
    protected void onMessage(Object message) {
        System.out.println("Room.onMessage()");
        System.out.println("message: " + message);
    }

    @Override
    protected void onJoin() {
        System.out.println("Room.onJoin()");
    }

    @Override
    protected void onStateChange(LinkedHashMap<String, Object> state) {
        System.out.println("Room.onStateChange()");
        System.out.println("state: " + state);
    }
});
room.setDefaultPatchListener(new FallbackPatchListenerCallback() {
    @Override
    public void callback(PatchObject patchObject) {
        System.out.println("change: " + patchObject);
    }
});

room.addPatchListener("players/:id/:attribute", new PatchListenerCallback() {
    @Override
    protected void callback(DataChange dataChange) {
        System.out.println("change: " + dataChange);
    }
});

            client.getAvailableRooms("public", new Client.GetAvailableRoomsCallback() {
                @Override
                public void onCallback(List<Client.AvailableRoom> roomsAvailable, String error) {
                    System.out.println(roomsAvailable.toString());
                    if (error != null) System.out.println(error);
                }
            });

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        while (true) {
            try {
                Thread.sleep(3000);
//                LinkedHashMap<String,Object> data = new LinkedHashMap<>();
//                data.put("op","key");
//                data.put("key",Math.floor(Math.random() * 4));
//                room.send(data);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}

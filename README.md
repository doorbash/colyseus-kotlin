# Colyseus Client for Java

## Download

Download [the latest JAR](https://github.com/doorbash/colyseus-java/releases/latest) or grab via Maven:

```xml
<dependency>
    <groupId>ir.doorbash</groupId>
    <artifactId>colyseus-java</artifactId>
    <version>1.0.5</version>
    <type>pom</type>
</dependency>
```

Gradle: 
```groovy
dependencies {
    implementation 'ir.doorbash:colyseus-java:1.0.5'
}
```

## Usage

### Connecting to server:

```java
Client client = new Client("ws://localhost:3000", new Client.Listener() {
    @Override
    public void onOpen(String id) {
        System.out.println("Client.onOpen();");
        System.out.println("colyseus id: " + id);
        // you can store id on device and pass it to client next time:
        // Client = new Client("ws://localhost:3000", id);
    }

    @Override
    public void onMessage(Object message) {
        System.out.println("Client.onMessage()");
        System.out.println(message);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("Client.onClose();");
    }

    @Override
    public void onError(Exception e) {
        System.out.println("Client.onError()");
        e.printStackTrace();
    }
});
```

### Joining to a room:

```java
Room room = client.join("public");
```
### Listening to room events:

```java
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
```
### Listening to all changes on the room state:

```java
room.setDefaultPatchListener(new FallbackPatchListenerCallback() {
    @Override
    public void callback(PatchObject patchObject) {
        System.out.println(patchObject.path);
        System.out.println(patchObject.operation);
        System.out.println(patchObject.value);
    }
});
```
### Listening to add/remove on a specific key on the room state:

```java
room.addPatchListener("players/:id", new PatchListenerCallback() {
    @Override
    protected void callback(DataChange dataChange) {
        System.out.println(dataChange.path);
        System.out.println(dataChange.operation);
        System.out.println(dataChange.value);
    }
});
```

### Listening to specific data changes in the state:

```java
room.addPatchListener("players/:id/:axis", new PatchListenerCallback() {
    @Override
    protected void callback(DataChange dataChange) {
        System.out.println(dataChange.path);
        System.out.println(dataChange.operation);
        System.out.println(dataChange.value);
    }
});
```

### Sending message to the room handler:

```java
LinkedHashMap<String, Object> data = new LinkedHashMap<>();
data.put("op", "key");
data.put("key", Math.floor(Math.random() * 4));
room.send(data);
```

### Getting available rooms list:

```java
client.getAvailableRooms("public", new Client.GetAvailableRoomsCallback() {
    @Override
    public void onCallback(List<Client.AvailableRoom> availableRooms, String error) {
        if (error != null) System.out.println(error);
        else System.out.println(availableRooms.toString());
    }
});
```

## License

MIT
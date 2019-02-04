# Colyseus Client for Java

### Download

Download [the latest JAR](https://dl.bintray.com/doorbash/ColyseusJavaClient/ir/doorbash/colyseus-java/1.0.3/colyseus-java-1.0.3.jar) or grab via Maven:

```xml
<dependency>
    <groupId>ir.doorbash</groupId>
    <artifactId>colyseus-java</artifactId>
    <version>1.0.3</version>
    <type>pom</type>
</dependency>
```

Gradle: 
```groovy
dependencies {
    compile 'ir.doorbash:colyseus-java:1.0.3'
}
```

## Usage

### Connecting to server:

```java
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
### Listening to all changes on the room state

```java
room.setDefaultPatchListener(new FallbackPatchListenerCallback() {
    @Override
    public void callback(PatchObject patchObject) {
        System.out.println("change: " + patchObject);
    }
});
```
### Listening to add/remove on a specific key on the room state

```java
room.addPatchListener("players/:id", new PatchListenerCallback() {
    @Override
    protected void callback(DataChange dataChange) {
        System.out.println("change: " + dataChange);
    }
});
```

### Listening to specific data changes in the state

```java
room.addPatchListener("players/:id/:axis", new PatchListenerCallback() {
    @Override
    protected void callback(DataChange dataChange) {
        System.out.println("change: " + dataChange);
    }
});
```

### Sending message to server

```java
LinkedHashMap<String,Object> data = new LinkedHashMap<>();
data.put("op","key");
data.put("key",Math.floor(Math.random() * 4));
room.send(data);
```

## License

MIT
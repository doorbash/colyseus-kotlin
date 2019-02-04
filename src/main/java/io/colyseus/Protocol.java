package io.colyseus;

public class Protocol {
    // User-related (0~10)
    public static final int USER_ID = 1;

    // Room-related (10~20)
    public static final int JOIN_ROOM = 10;
    public static final int JOIN_ERROR = 11;
    public static final int LEAVE_ROOM = 12;
    public static final int ROOM_DATA = 13;
    public static final int ROOM_STATE = 14;
    public static final int ROOM_STATE_PATCH = 15;

    // Match-making related (20~29)
    public static final int ROOM_LIST = 20;

    // Generic messages (50~60)
    public static final int BAD_REQUEST = 50;
}

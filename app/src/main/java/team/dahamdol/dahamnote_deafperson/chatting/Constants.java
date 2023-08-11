package team.dahamdol.dahamnote_deafperson.chatting;

public class Constants {
    /** bluetooth chat **/
    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_CONNECTION_LOST = 4;
    public static final int MESSAGE_LECTURE_SAVE_RESULT = 5;

    // Key names received from the BluetoothChatService Handler
    public static final String TOAST = "toast";

    // Intent request codes
    public static final int REQUEST_CONNECT_DEVICE = 101;
    public static final int REQUEST_ENABLE_BT = 102;
}

package network.lab1;

public enum MessageType {
    LEAVE("I\'am leave", 0),
    CHECKIN("Check members and update", 2);

    private final String message;
    private final int code;

    MessageType(String message, int code) {
        this.message = message;
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public int getCode() {
        return code;
    }

    public static MessageType fromCode(int code){
        if(code == 0){
            return LEAVE;
        }
        return CHECKIN;
    }
}

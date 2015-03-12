package blue.stack.snowball.app.inbox;

public class User {
    public static final int ID_INVALID = -1;
    public static final int USER_TYPE_GROUP = 2;
    public static final int USER_TYPE_SINGLE = 1;
    public static final int USER_TYPE_UNKNOWN = 0;
    String appId;
    String appSpecificUserId;
    String displayName;
    int id;
    int type;

    public User(int id, String appId, String appSpecificUserId, String displayName, int type) {
        this.id = id;
        this.appId = appId;
        this.appSpecificUserId = appSpecificUserId;
        this.displayName = displayName;
        this.type = type;
    }

    public int getId() {
        return this.id;
    }

    public String getAppId() {
        return this.appId;
    }

    public String getAppSpecificUserId() {
        return this.appSpecificUserId;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public int getType() {
        return this.type;
    }
}

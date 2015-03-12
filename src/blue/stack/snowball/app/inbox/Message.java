package blue.stack.snowball.app.inbox;

import java.util.List;

import android.app.PendingIntent;
import blue.stack.snowball.app.notifications.Action;

public class Message {
    public static final int ID_INVALID = -1;
    public static final int MESSAGE_STATE_READ = 1;
    public static final int MESSAGE_STATE_UNREAD = 0;
    List<Action> actions;
    String appId;
    String body;
    int id;
    PendingIntent pendingIntent;
    User sender;
    int senderUserId;
    int state;
    long timestamp;

    public Message(String appId, int senderUserId, String body, int state, long timestamp) {
        this.id = ID_INVALID;
        this.appId = appId;
        this.senderUserId = senderUserId;
        this.body = body;
        this.state = state;
        this.timestamp = timestamp;
    }

    public Message(int id, String appId, int senderUserId, String body, int state, long timestamp) {
        this.id = id;
        this.appId = appId;
        this.senderUserId = senderUserId;
        this.body = body;
        this.state = state;
        this.timestamp = timestamp;
    }

    public int getId() {
        return this.id;
    }

    public String getAppId() {
        return this.appId;
    }

    public int getSenderUserId() {
        return this.senderUserId;
    }

    public String getBody() {
        return this.body;
    }

    public int getState() {
        return this.state;
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public User getSender() {
        return this.sender;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }

    public PendingIntent getPendingIntent() {
        return this.pendingIntent;
    }

    public void setPendingIntent(PendingIntent pendingIntent) {
        this.pendingIntent = pendingIntent;
    }

    public List<Action> getActions() {
        return this.actions;
    }

    public void setActions(List<Action> actions) {
        this.actions = actions;
    }

    public boolean isUnread() {
        return this.state == 0;
    }
}

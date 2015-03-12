package blue.stack.snowball.app.inbox;

import android.app.PendingIntent;

public class Conversation {
    public static final int ID_INVALID = -1;
    String appId;
    int id;
    Message lastMessage;
    int lastMessageId;
    int numUnreadMessages;
    PendingIntent pendingIntent;
    User sender;
    int senderUserId;

    public Conversation(int id, String appId, int senderUserId, int lastMessageId, int numUnreadMessages) {
        this.id = id;
        this.appId = appId;
        this.senderUserId = senderUserId;
        this.lastMessageId = lastMessageId;
        this.numUnreadMessages = numUnreadMessages;
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

    public int getLastMessageId() {
        return this.lastMessageId;
    }

    public int getNumUnreadMessages() {
        return this.numUnreadMessages;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }

    public User getSender() {
        return this.sender;
    }

    public void setLastMessage(Message lastMessage) {
        this.lastMessage = lastMessage;
    }

    public Message getLastMessage() {
        return this.lastMessage;
    }

    public void setPendingIntent(PendingIntent pendingIntent) {
        this.pendingIntent = pendingIntent;
    }

    public PendingIntent getPendingIntent() {
        return this.pendingIntent;
    }
}

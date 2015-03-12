package blue.stack.snowball.app.inbox;

public interface InboxListener {
    void onInboxCleared();

    void onInboxMessageAdded(Message message);

    void onInboxUpdated();
}

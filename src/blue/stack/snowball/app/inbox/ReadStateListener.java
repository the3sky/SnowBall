package blue.stack.snowball.app.inbox;

public interface ReadStateListener {
    void onClearMessagesWaiting();

    void onNewMessagesWaiting();
}

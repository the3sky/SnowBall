package blue.stack.snowball.app.core;

public interface ListenerSource<ListenerType> {
    void addListener(ListenerType listenerType);

    void addListener(Object obj, ListenerType listenerType);

    void removeListener(Object obj);
}

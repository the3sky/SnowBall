package blue.stack.snowball.app.core;

import java.util.HashMap;
import java.util.Iterator;

public class ListenerHandler<ListenerType> implements Iterable<ListenerType> {
    private HashMap<Object, ListenerType> listeners;

    public ListenerHandler() {
        this.listeners = new HashMap();
    }

    public void addListener(Object handle, ListenerType listener) {
        this.listeners.put(handle, listener);
    }

    public void addListener(ListenerType listener) {
        this.listeners.put(listener, listener);
    }

    public void removeListener(Object handle) {
        this.listeners.remove(handle);
    }

    public void clearAll() {
        this.listeners.clear();
    }

    public Iterator<ListenerType> iterator() {
        return this.listeners.values().iterator();
    }
}

package blue.stack.snowball.app.inbox;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.content.Context;
import blue.stack.snowball.app.apps.App;
import blue.stack.snowball.app.apps.AppListener;
import blue.stack.snowball.app.apps.AppManager;
import blue.stack.snowball.app.core.ListenerHandler;
import blue.stack.snowball.app.core.ListenerSource;
import blue.stack.snowball.app.inbox.ui.InboxViewListener;
import blue.stack.snowball.app.inbox.ui.InboxViewManager;
import blue.stack.snowball.app.lockscreen.LockScreenListener;
import blue.stack.snowball.app.lockscreen.LockScreenManager;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class ReadStateManager implements AppListener, InboxViewListener, LockScreenListener, InboxListener, ListenerSource<ReadStateListener> {
    private static final String TAG = "ReadStateManager";
    @Inject
    AppManager appManager;
    @Inject
    Context context;
    @Inject
    InboxManager inboxManager;
    @Inject
    InboxViewManager inboxViewManager;
    ListenerHandler<ReadStateListener> listenerHandler;
    @Inject
    LockScreenManager lockScreenManager;
    Map<String, Integer> unreadCountByApp;

    @Inject
    private ReadStateManager() {
        this.listenerHandler = new ListenerHandler();
        this.unreadCountByApp = new HashMap();
    }

    @Inject
    private void start() {
        this.appManager.addListener(this);
        this.inboxViewManager.addListener(this);
        this.lockScreenManager.addListener(this);
        this.inboxManager.addListener(this);
    }

    public void stop() {
        this.lockScreenManager.removeListener(this);
        this.inboxViewManager.removeListener(this);
        this.appManager.removeListener(this);
        this.inboxManager.removeListener(this);
        this.listenerHandler.clearAll();
    }

    public void onAppLaunched(App app) {
        this.inboxManager.markAppAsRead(app.getAppId());
        clearAppCount(app.getAppId());
    }

    public void onEnabledAppsChanged(List<App> list) {
    }

    public void onInboxOpened(InboxViewManager manager) {
        clearAllAppCounts();
    }

    public void onInboxClosed(InboxViewManager manager) {
        if (!this.lockScreenManager.isPhoneLocked() && !this.lockScreenManager.isScreenOff()) {
            for (App app : this.appManager.getCurrentRunningApps()) {
                this.inboxManager.markAppAsRead(app.getAppId());
                clearAppCount(app.getAppId());
            }
        }
    }

    public void onLockScreenStarted(LockScreenManager manager) {
    }

    public void onLockScreenStopped(LockScreenManager manager) {
        if (!this.inboxViewManager.isInboxViewOpen()) {
            for (App app : this.appManager.getCurrentRunningApps()) {
                this.inboxManager.markAppAsRead(app.getAppId());
                clearAppCount(app.getAppId());
            }
        }
    }

    public void onPhoneCall(LockScreenManager manager) {
    }

    public void onPhoneCallEnded(LockScreenManager manager) {
    }

    public void onInboxUpdated() {
    }

    public void onInboxMessageAdded(Message message) {
        if (!this.inboxViewManager.isInboxViewOpen()) {
            incrementAppCount(message.appId);
        }
    }

    public void onInboxCleared() {
    }

    private void incrementAppCount(String appId) {
        if (!this.unreadCountByApp.containsKey(appId)) {
            this.unreadCountByApp.put(appId, Integer.valueOf(1));
        }
        this.unreadCountByApp.put(appId, Integer.valueOf(((Integer) this.unreadCountByApp.get(appId)).intValue() + 1));
        fireOnNewMessagesWaiting();
    }

    private void clearAppCount(String appId) {
        this.unreadCountByApp.remove(appId);
        if (this.unreadCountByApp.size() == 0) {
            fireOnClearMessagesWaiting();
        }
    }

    private void clearAllAppCounts() {
        this.unreadCountByApp.clear();
        fireOnClearMessagesWaiting();
    }

    public void addListener(Object handle, ReadStateListener listener) {
        this.listenerHandler.addListener(handle, listener);
    }

    public void addListener(ReadStateListener listener) {
        this.listenerHandler.addListener(listener);
    }

    public void removeListener(Object handle) {
        this.listenerHandler.removeListener(handle);
    }

    private void fireOnNewMessagesWaiting() {
        Iterator i$ = this.listenerHandler.iterator();
        while (i$.hasNext()) {
            ((ReadStateListener) i$.next()).onNewMessagesWaiting();
        }
    }

    private void fireOnClearMessagesWaiting() {
        this.unreadCountByApp.clear();
        Iterator i$ = this.listenerHandler.iterator();
        while (i$.hasNext()) {
            ((ReadStateListener) i$.next()).onClearMessagesWaiting();
        }
    }
}

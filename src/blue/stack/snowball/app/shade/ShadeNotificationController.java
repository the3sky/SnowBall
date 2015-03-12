package blue.stack.snowball.app.shade;

import java.util.Iterator;

import blue.stack.snowball.app.R;
import android.content.Context;
import android.graphics.Rect;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import blue.stack.snowball.app.core.ListenerHandler;
import blue.stack.snowball.app.core.ListenerSource;
import blue.stack.snowball.app.logging.EventLoggerManager;
import blue.stack.snowball.app.settings.Settings;
import blue.stack.snowball.app.shade.ShadeNotificationController.ShadeNotificationListener;
import blue.stack.snowball.app.shade.ui.ShadeNotificationBarView;
import blue.stack.snowball.app.shade.ui.ShadeNotificationBarView.NotificationViewListener;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class ShadeNotificationController implements ListenerSource<ShadeNotificationListener> {
    static String TAG;
    @Inject
    Context context;
    @Inject
    EventLoggerManager eventLoggerManager;
    ListenerHandler<ShadeNotificationListener> listenerHandler;
    ShadeNotificationBarView notificationBarView;
    View notificationTouchListener;
    @Inject
    Settings settings;

    public static interface ShadeNotificationListener {
        void onNotificationClosed();

        void onNotificationOpening();

        void onNotificationTouched();
    }

    enum WindowMode {
        Normal,
        Migration
    }

    public static class DefaultShadeNotificationListener implements ShadeNotificationListener {
        public void onNotificationOpening() {
        }

        public void onNotificationClosed() {
        }

        public void onNotificationTouched() {
        }
    }

    static {
        TAG = "ShadeNotificationController";
    }

    @Inject
    private ShadeNotificationController() {
        this.listenerHandler = new ListenerHandler();
    }

    @Inject
    void init() {
        this.notificationBarView = (ShadeNotificationBarView) ((LayoutInflater) this.context.getSystemService("layout_inflater")).inflate(R.layout.shade_notification_bar, null);
        this.notificationTouchListener = new View(this.context);
        this.notificationTouchListener.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == 1) {
                    Rect viewRect = new Rect();
                    v.getHitRect(viewRect);
                    if (viewRect.contains(Math.round(v.getX() + event.getX()), Math.round(v.getY() + event.getY()))) {
                        ShadeNotificationController.this.fireOsnNotificationTouched();
                        ShadeNotificationController.this.notificationBarView.closePopup();
                    }
                }
                return true;
            }
        });
        this.notificationBarView.addListener((Object) this, new NotificationViewListener() {
            public void onNotificationViewClosed() {
                ShadeNotificationController.this.removeFromWindow();
                ShadeNotificationController.this.fireOnNotificationClosed();
            }
        });
    }

    public void closePopup() {
        this.notificationBarView.closePopup();
    }

    private void addToWindow(WindowMode mode) {
        if (this.notificationBarView.getParent() == null) {
            WindowManager windowManager = (WindowManager) this.context.getSystemService("window");
            LayoutParams params = new LayoutParams(-2, -2, 2010, 16843544, -3);
            if (mode == WindowMode.Migration) {
                params.type = 2002;
                params.gravity = 53;
                this.notificationBarView.setGravity(85);
            } else if (this.settings.getEnableLeftHandedMode()) {
                params.gravity = 51;
                this.notificationBarView.setGravity(83);
            } else {
                params.gravity = 53;
                this.notificationBarView.setGravity(85);
            }
            params.height = (int) this.context.getResources().getDimension(R.dimen.shade_status_bar_notification_height);
            windowManager.addView(this.notificationBarView, params);
            params = new LayoutParams(-2, -2, 2002, 16843528, -3);
            if (mode == WindowMode.Migration) {
                params.gravity = 53;
            } else if (this.settings.getEnableLeftHandedMode()) {
                params.gravity = 51;
            } else {
                params.gravity = 53;
            }
            params.height = (int) this.context.getResources().getDimension(R.dimen.shade_status_bar_notification_height);
            params.width = (int) this.context.getResources().getDimension(R.dimen.shade_status_bar_notification_width);
            windowManager.addView(this.notificationTouchListener, params);
        }
    }

    public void removeFromWindow() {
        if (this.notificationBarView.getParent() != null) {
            ((WindowManager) this.context.getSystemService("window")).removeView(this.notificationBarView);
        }
        if (this.notificationTouchListener.getParent() != null) {
            ((WindowManager) this.context.getSystemService("window")).removeView(this.notificationTouchListener);
        }
    }

    public void openMigrationPopup() {
        addToWindow(WindowMode.Migration);
        fireOnNotificationOpening();
        this.notificationBarView.openMigrationPopup();
    }

    public void closeAll() {
        this.notificationBarView.closePopup();
        removeFromWindow();
    }

    public void stop() {
        this.listenerHandler.clearAll();
        this.notificationBarView.removeListener(this);
        this.notificationBarView.stop();
    }

    public void addListener(Object handle, ShadeNotificationListener listener) {
        this.listenerHandler.addListener(handle, listener);
    }

    public void addListener(ShadeNotificationListener listener) {
        this.listenerHandler.addListener(listener);
    }

    public void removeListener(Object handle) {
        this.listenerHandler.removeListener(handle);
    }

    private void fireOnNotificationOpening() {
        Iterator i$ = this.listenerHandler.iterator();
        while (i$.hasNext()) {
            ((ShadeNotificationListener) i$.next()).onNotificationOpening();
        }
    }

    private void fireOnNotificationClosed() {
        Iterator i$ = this.listenerHandler.iterator();
        while (i$.hasNext()) {
            ((ShadeNotificationListener) i$.next()).onNotificationClosed();
        }
    }

    private void fireOsnNotificationTouched() {
        Iterator i$ = this.listenerHandler.iterator();
        while (i$.hasNext()) {
            ((ShadeNotificationListener) i$.next()).onNotificationTouched();
        }
    }
}

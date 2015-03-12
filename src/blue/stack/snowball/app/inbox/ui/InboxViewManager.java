package blue.stack.snowball.app.inbox.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import blue.stack.snowball.app.R;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Rect;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnSystemUiVisibilityChangeListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import blue.stack.snowball.app.MainService;
import blue.stack.snowball.app.core.GuiceModule;
import blue.stack.snowball.app.inbox.InboxManager;
import blue.stack.snowball.app.inbox.ReadStateManager;
import blue.stack.snowball.app.logging.EventLogger;
import blue.stack.snowball.app.logging.EventLoggerManager;
import blue.stack.snowball.app.oob.OOBManager;
import blue.stack.snowball.app.settings.Settings;
import blue.stack.snowball.app.settings.SettingsActivity;
import blue.stack.snowball.app.swipe.InboxLayout;
import blue.stack.snowball.app.swipe.InboxLayout.OnDrawerListener;

import com.google.inject.Inject;

public abstract class InboxViewManager {
    public static final String ACTION_BACKBUTTON = "blue.stack.snowball.app.inboxViewManager";
    @Inject
    protected Context context;
    @Inject
    protected EventLoggerManager eventLoggerManager;
    protected InboxLayout inboxDrawer;
    protected InboxFragment inboxFragment;
    @Inject
    protected InboxViewController inboxViewController;
    private View listenerView;
    protected List<InboxViewListener> listeners;
    @Inject
    Logger logger;
    @Inject
    protected OOBManager oobManager;
    @Inject
    private ReadStateManager readStateManager;
    BroadcastReceiver receiver;
    @Inject
    protected Settings settings;
    protected String startupReason;
    int visibilityReason;

    class AnonymousClass_8 extends View {
        AnonymousClass_8(Context x0) {
            super(x0);
        }

        protected boolean fitSystemWindows(Rect insets) {
            if (insets.top == 0) {
                InboxViewManager.this.logger.info("fitSystemWindows : 0");
                InboxViewManager.this.setTabVisibility(Boolean.valueOf(false), eVisibilityReason.FullScreenMode);
            } else {
                InboxViewManager.this.logger.info("fitSystemWindows : != 0");
                InboxViewManager.this.setTabVisibility(Boolean.valueOf(true), eVisibilityReason.FullScreenMode);
            }
            return super.fitSystemWindows(insets);
        }

        public boolean dispatchKeyEvent(KeyEvent event) {
            if (event.getKeyCode() == 4) {
            }
            return super.dispatchKeyEvent(event);
        }
    }

    public enum eVisibilityReason {
        Docked,
        Orientation,
        InboxIsClosed,
        LockScreen,
        FullScreenMode
    }

    public abstract void cacheAnimations();

    public abstract void closeDrawer();

    public abstract int getDrawerLayout();

    public abstract boolean isInboxViewOpen();

    public abstract void openDrawer();

    public InboxViewManager() {
        this.listeners = new ArrayList();
        this.visibilityReason = 0;
    }

    public void addListener(InboxViewListener listener) {
        this.listeners.add(listener);
    }

    public void removeListener(InboxViewListener listener) {
        this.listeners.remove(listener);
    }

    @Inject
    protected void start() throws Exception {
        this.inboxFragment = new InboxFragment(this.context, this.inboxViewController.getListView());
        createListenerView();
        createInboxView();
        createBroadcastReceiver();
    }

    private void createBroadcastReceiver() {
        this.receiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("android.intent.action.CLOSE_SYSTEM_DIALOGS")) {
                    InboxViewManager.this.closeDrawer();
                } else if (intent.getAction().equals(ACTION_BACKBUTTON)) {
                    InboxViewManager.this.closeDrawer();
                }
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.CLOSE_SYSTEM_DIALOGS");
        filter.addAction(ACTION_BACKBUTTON);
        this.context.registerReceiver(this.receiver, filter);
    }

    private void createInboxView() {
        this.inboxDrawer = (InboxLayout) ((LayoutInflater) this.context.getSystemService("layout_inflater")).inflate(getDrawerLayout(), null);
        RelativeLayout sideLayout = (RelativeLayout) this.inboxDrawer.findViewById(R.id.swipe_side_layout);
        sideLayout.addView(this.inboxViewController.getView(), new LayoutParams(-1, -1));
        View clearButton = this.inboxDrawer.findViewById(R.id.invisible_clear_button);
        clearButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Log.d("ClearButton", "CloseDrawer()");
            }
        });
        clearButton.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                Log.d("ClearButton", "onTouch()");
                return false;
            }
        });
        sideLayout.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View view, MotionEvent motionEvent) {
                InboxViewManager.this.closeDrawer();
                return true;
            }
        });
        this.inboxDrawer.setDrawerListener(new OnDrawerListener() {
            public void onBackButton() {
                InboxViewManager.this.closeDrawer();
            }

            public void onHomeButton() {
                InboxViewManager.this.closeDrawer();
            }
        });
        this.inboxFragment.setServiceClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                InboxViewManager.this.closeDrawer();
            }
        });
    }

    protected void onSettingPressed() {
        closeDrawer();
        new Handler().postDelayed(new Runnable() {
            public void run() {
                Intent intent = SettingsActivity.getLaunchIntent(InboxViewManager.this.context);
                intent.addFlags(268468224);
                InboxViewManager.this.context.startActivity(intent);
            }
        }, 700);
    }

    protected boolean onClearAllSelected() {
        ((InboxManager) GuiceModule.get().getInstance(InboxManager.class)).clearAllMessages();
        return true;
    }

    public void stop() {
        if (this.inboxFragment != null) {
            this.inboxFragment.onDetach();
        }
        this.listeners.clear();
        if (this.listenerView != null) {
            getWindowManager().removeView(this.listenerView);
            this.listenerView = null;
        }
    }

    public void pushStartupReason(String startupReason) {
        this.startupReason = startupReason;
        if (startupReason.equals(MainService.LAUNCH_REASON_OOB_COMPLETE) || startupReason.equals(MainService.LAUNCH_REASON_OPENSHADE)) {
            openDrawer();
        }
    }

    public Boolean isTabVisible() {
        return Boolean.valueOf(this.visibilityReason == 0);
    }

    public Boolean isTabVisibleFieldSet(eVisibilityReason reason) {
        boolean z = true;
        if ((this.visibilityReason & (1 << reason.ordinal())) == 0) {
            z = false;
        }
        return Boolean.valueOf(z);
    }

    public InboxViewController getInboxViewController() {
        return this.inboxViewController;
    }

    public void setTabVisibility(Boolean isVisible, eVisibilityReason reason) {
        String stateChange;
        int before = this.visibilityReason;
        if (isVisible.booleanValue()) {
            stateChange = "ON";
            this.visibilityReason &= (1 << reason.ordinal()) ^ -1;
        } else {
            stateChange = "OFF";
            this.visibilityReason |= 1 << reason.ordinal();
        }
        Log.d("Visibility", stateChange + " " + reason.toString() + "  Before: " + before + "Val:" + this.visibilityReason);
    }

    private void createListenerView() {
        this.listenerView = new AnonymousClass_8(this.context);
        this.listenerView.setOnSystemUiVisibilityChangeListener(new OnSystemUiVisibilityChangeListener() {
            public void onSystemUiVisibilityChange(int visibility) {
                if ((visibility & 4) == 0) {
                    InboxViewManager.this.setTabVisibility(Boolean.valueOf(true), eVisibilityReason.FullScreenMode);
                } else {
                    InboxViewManager.this.setTabVisibility(Boolean.valueOf(false), eVisibilityReason.FullScreenMode);
                }
            }
        });
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(-2, -2, 2002, 65816, -3);
        params.width = 1;
        params.height = 1;
        params.gravity = 51;
        getWindowManager().addView(this.listenerView, params);
    }

    protected WindowManager getWindowManager() {
        return (WindowManager) this.context.getSystemService("window");
    }

    protected void fireOnDrawerOpened() {
        for (InboxViewListener listener : this.listeners) {
            listener.onInboxOpened(this);
        }
        if (!this.settings.getHasPulledDownShade()) {
            this.settings.setHasPulledDownShade(true);
        }
        if (this.oobManager.getOOBNeedsShadeMigration()) {
            this.oobManager.setOOBNeedsShadeMigrationCompleted();
            this.eventLoggerManager.getEventLogger().addEvent(EventLogger.USER_MIGRATE_SUCCESS);
        }
    }

    protected void fireOnDrawerClosed() {
        for (InboxViewListener listener : this.listeners) {
            listener.onInboxClosed(this);
        }
    }
}

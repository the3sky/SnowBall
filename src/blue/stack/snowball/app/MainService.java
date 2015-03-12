package blue.stack.snowball.app;

import java.util.Timer;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.os.EnvironmentCompat;
import android.util.Log;
import blue.stack.snowball.app.apps.AppManager;
import blue.stack.snowball.app.core.GuiceModule;
import blue.stack.snowball.app.inbox.InboxManager;
import blue.stack.snowball.app.inbox.ui.InboxViewManager;
import blue.stack.snowball.app.lockscreen.LockScreenManager;
import blue.stack.snowball.app.logging.EventLogger;
import blue.stack.snowball.app.logging.EventLoggerManager;
import blue.stack.snowball.app.notifications.NotificationManager;
import blue.stack.snowball.app.notifications.NotificationTrayManager;
import blue.stack.snowball.app.oob.OOBManager;
import blue.stack.snowball.app.ui.anim.AnimationCache;

import com.google.inject.Inject;

public class MainService extends Service {
	public static final String HOCKEY_APP_ID = "2c8154a6e3a0321d626bfea7f1b749cb";
	public static final String LAUNCH_REASON = "launch_reason";
	public static final String LAUNCH_REASON_BOOT = "launch_reason_default";
	public static final String LAUNCH_REASON_DEFAULT = "launch_reason_default";
	public static final String LAUNCH_REASON_OOB_COMPLETE = "launch_reason_oob_complete";
	public static final String LAUNCH_REASON_OPENSHADE = "launch_reason_openshade";
	public static final String LAUNCH_REASON_PACKAGE_REPLACED = "launch_reason_package_replaced";
	public static final String TAG = "MainService";
	@Inject
	private AnimationCache animationCache;
	@Inject
	private AppManager appManager;
	@Inject
	private EventLoggerManager eventLoggerManager;
	@Inject
	private InboxManager inboxManager;
	@Inject
	private InboxViewManager inboxViewManager;
	@Inject
	private LockScreenManager lockScreenManager;
	@Inject
	private NotificationManager notificationManager;
	@Inject
	private NotificationTrayManager notificationTrayManager;
	@Inject
	private OOBManager oobManager;
	private Timer secondsTimer;
	private String startupReason;

	public InboxManager getInboxManager() {
		return this.inboxManager;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent == null || intent.getExtras() == null || !intent.getExtras().containsKey(LAUNCH_REASON)) {
			Log.d(TAG, "Launch Reason: Unknown");
			this.eventLoggerManager.getEventLogger().addEvent(EventLogger.MAINSERVICE_STARTED,
					EventLogger.MAINSERVICE_START_REASON, EnvironmentCompat.MEDIA_UNKNOWN);
		} else {
			this.startupReason = intent.getExtras().getString(LAUNCH_REASON);
			this.eventLoggerManager.getEventLogger().addEvent(EventLogger.MAINSERVICE_STARTED,
					EventLogger.MAINSERVICE_START_REASON, this.startupReason);
			Log.d(TAG, "Launch Reason:" + this.startupReason);
			if (this.inboxViewManager != null) {
				this.inboxViewManager.pushStartupReason(this.startupReason);
			}
		}
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(TAG, "OnCreate");
		GuiceModule.construct(this);
		GuiceModule.get().injectMembers(this);
		this.animationCache.flushCache();
		this.inboxViewManager.cacheAnimations();
		if (this.oobManager.isOOBComplete()) {
			this.notificationTrayManager.showPermissionNotification();
			startForeground(1, this.notificationTrayManager.getPersistentNotification());
			// TODO checkForCrashes();
			return;
		}
		stopSelf();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "onDestroy()");
		if (this.inboxViewManager != null) {
			this.inboxViewManager.stop();
			this.inboxViewManager = null;
		}
		if (this.secondsTimer != null) {
			this.secondsTimer.cancel();
		}
	}

	// private void checkForCrashes() {
	// CrashManager.register(this, HOCKEY_APP_ID, new
	// SwoopCrashManagerListener());
	// }

	public static void startMainService(Context context, String launchReason) {
		Intent launchMainServiceIntent = new Intent(context, MainService.class);
		launchMainServiceIntent.putExtra(LAUNCH_REASON, launchReason);
		context.startService(launchMainServiceIntent);
	}
}

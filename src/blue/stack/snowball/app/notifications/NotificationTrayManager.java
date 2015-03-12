package blue.stack.snowball.app.notifications;

import blue.stack.snowball.app.R;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.Notification.Builder;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Handler;
import android.support.v4.widget.ExploreByTouchHelper;
import android.widget.RemoteViews;
import blue.stack.snowball.app.MainService;
import blue.stack.snowball.app.inbox.InboxManager;
import blue.stack.snowball.app.inbox.ReadStateListener;
import blue.stack.snowball.app.inbox.ReadStateManager;
import blue.stack.snowball.app.inbox.ui.InboxViewManager;
import blue.stack.snowball.app.lockscreen.LockScreenListener;
import blue.stack.snowball.app.lockscreen.LockScreenManager;
import blue.stack.snowball.app.logging.EventLogger;
import blue.stack.snowball.app.oob.OOBManager;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class NotificationTrayManager implements NotificationListener, LockScreenListener {
	private static final String INTENT_ACTION_LAUNCH_SHADE = "blue.stack.snowball.app.notificationtraymanager.launchshade";
	private static final String INTENT_ACTION_OPEN_PERMISSIONS = "blue.stack.snowball.app.notificationtraymanager.openpermissions";
	private static final int QUERY_MESSAGES_DELAY = 3000;
	private static final String TAG = "NotificationTrayManager";
	@Inject
	Context context;
	boolean hasPendingMessages;
	@Inject
	InboxManager inboxManager;
	@Inject
	InboxViewManager inboxViewManager;
	@Inject
	LockScreenManager lockScreenManager;
	@Inject
	NotificationManager notificationManager;
	NotificationPermissionsReceiver notificationPermissionsReceiver;
	NotificationTrayManagerReceiver notificationTrayManagerReceiver;
	@Inject
	OOBManager oobManager;
	@Inject
	ReadStateManager readStateManager;

	public class NotificationPermissionsReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(INTENT_ACTION_OPEN_PERMISSIONS)) {
				NotificationTrayManager.this.inboxViewManager.closeDrawer();
				Intent i = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
				i.addFlags(1342177280);
				context.startActivity(i);
			}
		}
	}

	public class NotificationTrayManagerReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals("android.intent.action.SCREEN_ON")) {
				new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						NotificationTrayManager.this.updatePersistentNotification();
					}
				}, 3000);
			} else if (intent.getAction().equals("android.intent.action.SCREEN_OFF")) {
				NotificationTrayManager.this.updatePersistentNotification();
			} else if (intent.getAction().equals(INTENT_ACTION_LAUNCH_SHADE)
					&& NotificationTrayManager.this.oobManager.isOOBComplete()) {
				MainService.startMainService(context, MainService.LAUNCH_REASON_OPENSHADE);
			}
		}
	}

	@Inject
	private NotificationTrayManager() {
		this.hasPendingMessages = false;
	}

	@Inject
	private void start() {
		boolean z = true;
		clearPermissionNotification();
		updatePersistentNotification();
		this.notificationManager.addListener(this);
		this.lockScreenManager.addListener(this);
		this.notificationPermissionsReceiver = new NotificationPermissionsReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(INTENT_ACTION_OPEN_PERMISSIONS);
		this.context.registerReceiver(this.notificationPermissionsReceiver, filter);
		this.notificationTrayManagerReceiver = new NotificationTrayManagerReceiver();
		filter = new IntentFilter();
		filter.addAction("android.intent.action.SCREEN_ON");
		filter.addAction("android.intent.action.SCREEN_OFF");
		filter.addAction(INTENT_ACTION_LAUNCH_SHADE);
		this.context.registerReceiver(this.notificationTrayManagerReceiver, filter);
		if (this.inboxManager.getUnreadMessages(1).size() <= 0) {
			z = false;
		}
		this.hasPendingMessages = z;
		this.readStateManager.addListener(this, new ReadStateListener() {
			@Override
			public void onNewMessagesWaiting() {
				NotificationTrayManager.this.hasPendingMessages = true;
				NotificationTrayManager.this.updatePersistentNotification();
			}

			@Override
			public void onClearMessagesWaiting() {
				NotificationTrayManager.this.hasPendingMessages = false;
				NotificationTrayManager.this.updatePersistentNotification();
			}
		});
	}

	public void stop() {
		this.notificationManager.removeListener(this);
		this.lockScreenManager.removeListener(this);
		this.readStateManager.removeListener(this);
		this.context.unregisterReceiver(this.notificationPermissionsReceiver);
		this.context.unregisterReceiver(this.notificationTrayManagerReceiver);
	}

	/******* 持久通知 *******/
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public Notification getPersistentNotification() {
		PendingIntent pendingIntent = PendingIntent.getBroadcast(this.context, 0,
				new Intent(INTENT_ACTION_LAUNCH_SHADE), 0);
		RemoteViews content = new RemoteViews("blue.stack.snowball.app", R.layout.persistent_notification);
		String notificationTitle = this.context.getResources().getString(R.string.persistent_notification_title);
		String notificationContent = this.context.getResources().getString(R.string.persistent_notification_text);
		int priority = ExploreByTouchHelper.INVALID_ID;
		int smallIconId = 17170445;
		long when = Long.MIN_VALUE;
		if (this.hasPendingMessages) {
			notificationContent = this.context.getResources().getString(
					R.string.persistent_notification_with_messages_text);
			if (VERSION.SDK_INT <= 19) {
				priority = 1;
			}
			if (!(this.lockScreenManager.isPhoneLocked() || this.lockScreenManager.isScreenOff())) {
				when = System.currentTimeMillis();
				priority = 1;
			}
			smallIconId = R.drawable.ic_notification_small;
		}
		content.setTextViewText(R.id.notification_title_textview, notificationTitle);
		content.setTextViewText(R.id.notification_content_textview, notificationContent);
		return new Builder(this.context).setPriority(priority).setContentIntent(pendingIntent)
				.setSmallIcon(smallIconId).setContent(content).setWhen(when).setOngoing(true).build();
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	void updatePersistentNotification() {
		((android.app.NotificationManager) this.context.getSystemService(EventLogger.VALUE_APP_OPENED_VIA_NOTIFICATION))
				.notify(1,
						getPersistentNotification());
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public void showPermissionNotification() {
		PendingIntent pendingIntent = PendingIntent.getBroadcast(this.context, 0, new Intent(
				INTENT_ACTION_OPEN_PERMISSIONS), 0);
		String title = this.context.getResources().getString(R.string.permissions_notification_title);
		((android.app.NotificationManager) this.context.getSystemService(EventLogger.VALUE_APP_OPENED_VIA_NOTIFICATION))
				.notify(
						2,
						new Builder(this.context)
								.setSmallIcon(17301543)
								.setContentTitle(title)
								.setContentText(
										this.context.getResources().getString(R.string.permissions_notification_text))
								.setContentIntent(pendingIntent).setOngoing(true).setPriority(1).build());
	}

	public void clearPermissionNotification() {
		((android.app.NotificationManager) this.context.getSystemService(EventLogger.VALUE_APP_OPENED_VIA_NOTIFICATION))
				.cancel(2);
	}

	@Override
	public void onNotificationServiceRunning() {
		clearPermissionNotification();
	}

	@Override
	public void onNotificationPosted(blue.stack.snowball.app.notifications.Notification notification) {
		if (notification.getPackageName().equals(this.context.getApplicationContext().getPackageName())
				&& notification.getId() == 2) {
			clearPermissionNotification();
		}
	}

	@Override
	public void onLockScreenStarted(LockScreenManager manager) {
	}

	@Override
	public void onLockScreenStopped(LockScreenManager manager) {
		updatePersistentNotification();
	}

	@Override
	public void onPhoneCall(LockScreenManager manager) {
	}

	@Override
	public void onPhoneCallEnded(LockScreenManager manager) {
	}

}

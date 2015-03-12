/**
 * 
 */
package blue.stack.snowball.app.nsprocess;

import java.util.List;

import android.accessibilityservice.AccessibilityService;
import android.app.XNotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import blue.stack.snowball.app.App;
import blue.stack.snowball.app.apps.AppIds;

/**
 * @author BunnyBlue
 *
 */
public abstract class BaseService extends AccessibilityService {
	public static final String CMD_CHECK_SERVICE_RUNNING = "check_service_running";
	public static final String CMD_LIST = "list";
	public static final String CMD_REMOVE = "remove";
	public static final String CMD_REMOVE_ALL = "remove_all";
	public static final String CMD_REMOVE_SMS = "remove_sms";
	public static final String EVENT_ACTIVE_LISTING = "active_listing";
	public static final String EVENT_ALL_REMOVED = "all_removed";
	public static final String EVENT_POSTED = "posted";
	public static final String EVENT_REMOVED = "removed";
	public static final String EVENT_SERVICE_RUNNING = "service_running";
	private static final int FUZZY_TIMESTAMP_MATCH = 15000;
	public static final String INTENT_ACTION_CMDS = "blue.stack.snowball.app.notificationservice.commands";
	public static final String INTENT_ACTION_EVENTS = "blue.stack.snowball.app.notificationservice.events";
	public static final String KEY_NOTIFICATION = "key_notification";
	public static final String KEY_NOTIFICATIONS = "key_notifications";
	public static final String KEY_NOTIFICATION_ID = "key_notification_id";
	public static final String KEY_NOTIFICATION_KEY = "key_notification_key";
	public static final String KEY_NOTIFICATION_PACKAGE = "key_notification_package";
	public static final String KEY_NOTIFICATION_TAG = "key_notification_tag";
	public static final String KEY_SMS_ADDRESS = "key_sms_address";
	public static final String KEY_SMS_BODY = "key_sms_body";
	public static final String KEY_SMS_DISPLAY_ADDRESS = "key_sms_display_address";
	public static final String KEY_SMS_FROM = "key_sms_from";
	public static final String KEY_SMS_PACKAGE = "key_sms_package";
	public static final String KEY_SMS_TIMESTAMP = "key_sms_timestamp";
	private static final int PREFIX_MATCH_LENGTH = 10;
	private static final String TAG = "NotificationService";
	public static final String TYPE_CMD = "command";
	public static final String TYPE_EVENT = "event";
	private NotificationReceiver receiver;
	private List<String> supportedAppPackageNames;

	class NotificationReceiver extends BroadcastReceiver {
		NotificationReceiver() {
		}

		@Override
		public void onReceive(Context context, Intent intent) {
			String type = intent.getStringExtra(TYPE_CMD);
			if (type.equals(CMD_LIST)) {
				onRequestList();
			} else if (type.equals(CMD_REMOVE)) {
				onRequestRemove(intent.getStringExtra(KEY_NOTIFICATION_PACKAGE),
						intent.getStringExtra(KEY_NOTIFICATION_TAG), intent.getIntExtra(KEY_NOTIFICATION_ID, 0),
						intent.getStringExtra(KEY_NOTIFICATION_KEY));
			} else if (type.equals(CMD_REMOVE_SMS)) {
				onRequestRemoveSMS(intent.getStringExtra(KEY_SMS_PACKAGE),
						intent.getStringExtra(KEY_SMS_FROM), intent.getStringExtra(KEY_SMS_ADDRESS),
						intent.getStringExtra(KEY_SMS_DISPLAY_ADDRESS), intent.getStringExtra(KEY_SMS_BODY),
						intent.getLongExtra(KEY_SMS_TIMESTAMP, -1));
			} else if (type.equals(CMD_REMOVE_ALL)) {
				onRequestRemoveAll();
			} else if (type.equals(CMD_CHECK_SERVICE_RUNNING)) {
				onServiceRunning();
			}
		}

		/**
		 * @param stringExtra
		 * @param stringExtra2
		 * @param stringExtra3
		 * @param stringExtra4
		 * @param stringExtra5
		 * @param longExtra
		 */
		private void onRequestRemoveSMS(String stringExtra, String stringExtra2, String stringExtra3,
				String stringExtra4, String stringExtra5, long longExtra) {
			// TODO Auto-generated method stub
			throw new RuntimeException();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Service#onCreate()
	 */
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		this.receiver = new NotificationReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(INTENT_ACTION_CMDS);
		registerReceiver(this.receiver, filter);
		this.supportedAppPackageNames = AppIds.getSupportedAppPackageNames();
		onServiceRunning();
	}

	void onRequestList() {
		throw new RuntimeException();
		// StatusBarNotification[] statusBarNotifications =
		// getActiveNotifications();
		// ArrayList<Notification> notifications = new
		// ArrayList(statusBarNotifications.length);
		// for (StatusBarNotification statusBarNotification :
		// statusBarNotifications) {
		// notifications.add(notificationFromStatusBarNotification(statusBarNotification));
		// }
		// Intent i = new Intent(INTENT_ACTION_EVENTS);
		// i.putExtra(TYPE_EVENT, EVENT_ACTIVE_LISTING);
		// i.putExtra(KEY_NOTIFICATIONS, notifications);
		// sendBroadcast(i);
	}

	void onServiceRunning() {
		Intent i = new Intent(INTENT_ACTION_EVENTS);
		i.putExtra(TYPE_EVENT, EVENT_SERVICE_RUNNING);
		sendBroadcast(i);
	}

	void onRequestRemoveAll() {
		App.getInstance().getNoticeManger().cancelAll();
		Intent i = new Intent(INTENT_ACTION_EVENTS);
		i.putExtra(TYPE_EVENT, EVENT_ALL_REMOVED);
		sendBroadcast(i);
	}

	void onRequestRemove(String packageName, String tag, int id, String key) {
		XNotificationManager.cancel(packageName, tag, id, App.getInstance().getNoticeManger());
		// INotificationManager m;
		// if (VERSION.SDK_INT < 21) {
		// cancelNotification(packageName, tag, id);
		// App.getInstance().getNoticeManger().cancel(tag, id);
		// } else {
		// cancelNotification(key);
		// }
	}

	// void onRequestRemoveSMS(String packageName, String from, String address,
	// String displayAddress, String body,
	// long timestamp) {
	// try {
	// for (StatusBarNotification sbn : getActiveNotifications()) {
	// android.app.Notification n = sbn.getNotification();
	// if (sbn.getPackageName().equals(packageName)) {
	// if (isSMSFuzzyMatch(from, address, displayAddress, body, timestamp,
	// notificationFromStatusBarNotification(sbn))) {
	// if (VERSION.SDK_INT < 21) {
	// cancelNotification(sbn.getPackageName(), sbn.getTag(), sbn.getId());
	// } else {
	// cancelNotification(sbn.getKey());
	// }
	// }
	// }
	// }
	// } catch (Exception e) {
	// }
	// }

	/**
	 * @return
	 */
	private Object getActiveNotifications() {
		// TODO Auto-generated method stub
		return null;
	}

	// Notification notificationFromStatusBarNotification(StatusBarNotification
	// sbn) {
	// return new Notification(sbn.getPackageName(), sbn.getId(), sbn.getTag(),
	// null,
	// sbn.getNotification());
	// }
}

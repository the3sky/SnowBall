package blue.stack.snowball.app.nsprocess;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build.VERSION;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import blue.stack.snowball.app.apps.AppIds;
import blue.stack.snowball.app.notifications.Notification;

@TargetApi(18)
public class NotificationService extends NotificationListenerService {
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
				NotificationService.this.onRequestList();
			} else if (type.equals(CMD_REMOVE)) {
				NotificationService.this.onRequestRemove(intent.getStringExtra(KEY_NOTIFICATION_PACKAGE),
						intent.getStringExtra(KEY_NOTIFICATION_TAG), intent.getIntExtra(KEY_NOTIFICATION_ID, 0),
						intent.getStringExtra(KEY_NOTIFICATION_KEY));
			} else if (type.equals(CMD_REMOVE_SMS)) {
				NotificationService.this.onRequestRemoveSMS(intent.getStringExtra(KEY_SMS_PACKAGE),
						intent.getStringExtra(KEY_SMS_FROM), intent.getStringExtra(KEY_SMS_ADDRESS),
						intent.getStringExtra(KEY_SMS_DISPLAY_ADDRESS), intent.getStringExtra(KEY_SMS_BODY),
						intent.getLongExtra(KEY_SMS_TIMESTAMP, -1));
			} else if (type.equals(CMD_REMOVE_ALL)) {
				NotificationService.this.onRequestRemoveAll();
			} else if (type.equals(CMD_CHECK_SERVICE_RUNNING)) {
				NotificationService.this.onServiceRunning();
			}
		}
	}

	@Override
	public void onCreate() {
		super.onCreate();
		this.receiver = new NotificationReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(INTENT_ACTION_CMDS);
		registerReceiver(this.receiver, filter);
		this.supportedAppPackageNames = AppIds.getSupportedAppPackageNames();
		onServiceRunning();
	}

	@Override
	public void onDestroy() {
		unregisterReceiver(this.receiver);
		this.receiver = null;
	}

	@TargetApi(21)
	private String getStatusBarNotificationKey(StatusBarNotification statusBarNotification) {
		return statusBarNotification.getKey();
	}

	@Override
	public void onNotificationPosted(StatusBarNotification statusBarNotification) {
		Log.d(TAG, "Notification onNotificationPosted: " + statusBarNotification.getPackageName());
		if (isPermissionNotification(statusBarNotification)) {
			if (VERSION.SDK_INT < 21) {
				clearPermissionNotification(null);
			} else {
				clearPermissionNotification(getStatusBarNotificationKey(statusBarNotification));
			}
		}
		if (shouldBroadcastNotification(statusBarNotification)) {
			Log.d(TAG, "Notification posted: " + statusBarNotification.getPackageName());
			Notification notification = notificationFromStatusBarNotification(statusBarNotification);
			Intent i = new Intent(INTENT_ACTION_EVENTS);
			i.putExtra(TYPE_EVENT, EVENT_POSTED);
			i.putExtra(KEY_NOTIFICATION, notification);
			sendBroadcast(i);
		}
	}

	@Override
	public void onNotificationRemoved(StatusBarNotification statusBarNotification) {
		Log.d(TAG, "Notification removed: " + statusBarNotification.getPackageName());
		Notification notification = notificationFromStatusBarNotification(statusBarNotification);
		Intent i = new Intent(INTENT_ACTION_EVENTS);
		i.putExtra(TYPE_EVENT, EVENT_REMOVED);
		i.putExtra(KEY_NOTIFICATION, notification);
		sendBroadcast(i);
	}

	boolean isPermissionNotification(StatusBarNotification sbn) {
		return getPackageName().equals(sbn.getPackageName()) && 2 == sbn.getId();
	}

	boolean shouldBroadcastNotification(StatusBarNotification sbn) {
		if (isPermissionNotification(sbn)) {
			return true;
		}
		if (!sbn.isClearable()) {
			Log.d(TAG, "isClearable!");
			return false;
		}
		for (String supportedAppPackageName : this.supportedAppPackageNames) {
			if (sbn.getPackageName().equals(supportedAppPackageName)) {
				return true;
			}
		}
		Log.d(TAG, "sbn.getPackageName()" + sbn.getPackageName() + AppIds.getSMSAppPackageName(this));
		return sbn.getPackageName().equals(AppIds.getSMSAppPackageName(this));
	}

	@TargetApi(21)
	void clearPermissionNotification(String key) {
		if (VERSION.SDK_INT < 21) {
			cancelNotification(getPackageName(), null, 2);
		} else {
			cancelNotification(key);
		}
	}

	void onServiceRunning() {
		Intent i = new Intent(INTENT_ACTION_EVENTS);
		i.putExtra(TYPE_EVENT, EVENT_SERVICE_RUNNING);
		sendBroadcast(i);
	}

	void onRequestList() {
		StatusBarNotification[] statusBarNotifications = getActiveNotifications();
		ArrayList<Notification> notifications = new ArrayList(statusBarNotifications.length);
		for (StatusBarNotification statusBarNotification : statusBarNotifications) {
			notifications.add(notificationFromStatusBarNotification(statusBarNotification));
		}
		Intent i = new Intent(INTENT_ACTION_EVENTS);
		i.putExtra(TYPE_EVENT, EVENT_ACTIVE_LISTING);
		i.putExtra(KEY_NOTIFICATIONS, notifications);
		sendBroadcast(i);
	}

	@TargetApi(21)
	void onRequestRemoveSMS(String packageName, String from, String address, String displayAddress, String body,
			long timestamp) {
		try {
			for (StatusBarNotification sbn : getActiveNotifications()) {
				android.app.Notification n = sbn.getNotification();
				if (sbn.getPackageName().equals(packageName)) {
					if (isSMSFuzzyMatch(from, address, displayAddress, body, timestamp,
							notificationFromStatusBarNotification(sbn))) {
						if (VERSION.SDK_INT < 21) {
							cancelNotification(sbn.getPackageName(), sbn.getTag(), sbn.getId());
						} else {
							cancelNotification(sbn.getKey());
						}
					}
				}
			}
		} catch (Exception e) {
		}
	}

	boolean isSMSFuzzyMatch(String from, String address, String displayAddress, String body, long timestamp,
			Notification n) {
		if (isTimestampFuzzyMatch(timestamp, n.getWhen())) {
			return true;
		}
		ArrayList<String> stringsToInspect = new ArrayList();
		addStringToStringList(n.getTickerText(), stringsToInspect);
		addStringToStringList(n.getContentTitle(), stringsToInspect);
		addStringToStringList(n.getContentText(), stringsToInspect);
		addStringToStringList(n.getContentInfoText(), stringsToInspect);
		addStringToStringList(n.getExpandedContentTitle(), stringsToInspect);
		addStringStringToStringList(n.getExpandedInboxText(), stringsToInspect);
		addStringToStringList(n.getExpandedContentText(), stringsToInspect);
		Iterator i$ = stringsToInspect.iterator();
		while (i$.hasNext()) {
			String s = (String) i$.next();
			if (isStringFuzzyMatch(from, s) || isStringFuzzyMatch(address, s) || isStringFuzzyMatch(displayAddress, s)) {
				return true;
			}
			if (isStringFuzzyMatch(body, s)) {
				return true;
			}
		}
		return false;
	}

	boolean isTimestampFuzzyMatch(long t1, long t2) {
		return Math.abs(t1 - t2) < 15000;
	}

	boolean isStringFuzzyMatch(String container, String containee) {
		if (container == null || container.equals("") || containee == null || containee.equals("")) {
			return false;
		}
		if (container.contains(containee)) {
			return true;
		}
		int compareLength = containee.length();
		if (compareLength > PREFIX_MATCH_LENGTH) {
			compareLength = PREFIX_MATCH_LENGTH;
		}
		return container.startsWith(containee.substring(0, compareLength));
	}

	void addStringToStringList(String string, ArrayList<String> stringList) {
		if (string != null) {
			stringList.add(string);
		}
	}

	void addStringStringToStringList(List<String> strings, ArrayList<String> stringList) {
		if (strings != null) {
			for (String string : strings) {
				if (string != null) {
					stringList.add(string);
				}
			}
		}
	}

	void onRequestRemoveAll() {
		cancelAllNotifications();
		Intent i = new Intent(INTENT_ACTION_EVENTS);
		i.putExtra(TYPE_EVENT, EVENT_ALL_REMOVED);
		sendBroadcast(i);
	}

	@TargetApi(21)
	void onRequestRemove(String packageName, String tag, int id, String key) {
		if (VERSION.SDK_INT < 21) {
			cancelNotification(packageName, tag, id);
		} else {
			cancelNotification(key);
		}
	}

	@TargetApi(21)
	Notification notificationFromStatusBarNotification(StatusBarNotification sbn) {
		return VERSION.SDK_INT < 21 ? new Notification(sbn.getPackageName(), sbn.getId(), sbn.getTag(), null,
				sbn.getNotification()) : new Notification(sbn.getPackageName(), sbn.getId(), sbn.getTag(),
				sbn.getKey(), sbn.getNotification());
	}
}

package blue.stack.snowball.app.notifications;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build.VERSION;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import blue.stack.snowball.app.apps.App;
import blue.stack.snowball.app.apps.AppListener;
import blue.stack.snowball.app.apps.AppManager;
import blue.stack.snowball.app.inbox.InboxManager;
import blue.stack.snowball.app.inbox.RawMessage;
import blue.stack.snowball.app.logging.EventLogger;
import blue.stack.snowball.app.logging.EventLoggerManager;
import blue.stack.snowball.app.nsprocess.NotificationService;
import blue.stack.snowball.app.oob.OOBManager;
import blue.stack.snowball.app.settings.Settings;
import blue.stack.snowball.app.settings.Settings.SettingChangeListener;
import blue.stack.snowball.app.tools.SwoopBroadcastReceiver;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class NotificationManager implements AppListener, SettingChangeListener {
	private static final long LONG_REMOVE_NOTIFICATION_DELAY = 3000;
	public static final String REGEX_FOR_URLS = "(?i)\\b((?:https?://|www\\d{0,3}[.]|[a-z0-9.\\-]+[.][a-z]{2,4}/)(?:[^\\s()<>]+|\\(([^\\s()<>]+|(\\([^\\s()<>]+\\)))*\\))+(?:\\(([^\\s()<>]+|(\\([^\\s()<>]+\\)))*\\)|[^\\s`!()\\[\\]{};:'\".,<>???\u201c\u201d\u2018\u2019]))";
	private static final long REMOVE_NOTIFICATION_DELAY = 800;
	private static final long SHORT_REMOVE_NOTIFICATION_DELAY = 500;
	private static final long SMS_REMOVE_NOTIFICATION_DELAY = 3000;
	private static final String TAG = "NotificationManager";
	@Inject
	AppManager appManager;
	@Inject
	Context context;
	boolean didNotificationServiceStart;
	@Inject
	EventLoggerManager eventLoggerManager;
	@Inject
	InboxManager inboxManager;
	boolean keepNotificationsInDrawer;
	List<NotificationListener> listeners;
	@Inject
	OOBManager oobManager;
	List<NotificationProcessor> processors;
	NotificationManagerReceiver receiver;
	@Inject
	Settings settings;

	class AnonymousClass_1 extends Handler {
		final/* synthetic */String val$address;
		final/* synthetic */String val$body;
		final/* synthetic */String val$displayAddress;
		final/* synthetic */String val$from;
		final/* synthetic */String val$smsPackage;
		final/* synthetic */long val$timestamp;

		AnonymousClass_1(String str, String str2, String str3, String str4, String str5, long j) {
			this.val$smsPackage = str;
			this.val$from = str2;
			this.val$address = str3;
			this.val$displayAddress = str4;
			this.val$body = str5;
			this.val$timestamp = j;
		}

		@Override
		public void handleMessage(Message msg) {
			Intent i = new Intent(NotificationService.INTENT_ACTION_CMDS);
			i.putExtra(SwoopBroadcastReceiver.TYPE_CMD, NotificationService.CMD_REMOVE_SMS);
			i.putExtra(NotificationService.KEY_SMS_PACKAGE, this.val$smsPackage);
			i.putExtra(NotificationService.KEY_SMS_FROM, this.val$from);
			i.putExtra(NotificationService.KEY_SMS_ADDRESS, this.val$address);
			i.putExtra(NotificationService.KEY_SMS_DISPLAY_ADDRESS, this.val$displayAddress);
			i.putExtra(NotificationService.KEY_SMS_BODY, this.val$body);
			i.putExtra(NotificationService.KEY_SMS_TIMESTAMP, this.val$timestamp);
			NotificationManager.this.context.sendBroadcast(i);
		}
	}

	class AnonymousClass_2 extends Handler {
		final/* synthetic */int val$id;
		final/* synthetic */String val$key;
		final/* synthetic */String val$packageName;
		final/* synthetic */String val$tag;

		AnonymousClass_2(String str, String str2, int i, String str3) {
			this.val$packageName = str;
			this.val$tag = str2;
			this.val$id = i;
			this.val$key = str3;
		}

		@Override
		public void handleMessage(Message msg) {
			Intent i = new Intent(NotificationService.INTENT_ACTION_CMDS);
			i.putExtra(SwoopBroadcastReceiver.TYPE_CMD, NotificationService.CMD_REMOVE);
			i.putExtra(NotificationService.KEY_NOTIFICATION_PACKAGE, this.val$packageName);
			i.putExtra(NotificationService.KEY_NOTIFICATION_TAG, this.val$tag);
			i.putExtra(NotificationService.KEY_NOTIFICATION_ID, this.val$id);
			i.putExtra(NotificationService.KEY_NOTIFICATION_KEY, this.val$key);
			NotificationManager.this.context.sendBroadcast(i);
		}
	}

	class NotificationManagerReceiver extends BroadcastReceiver {
		NotificationManagerReceiver() {
		}

		@Override
		public void onReceive(Context context, Intent intent) {
			Log.e("me", intent.getAction());
			String type = intent.getStringExtra(NotificationService.TYPE_EVENT);
			if (type.equals(NotificationService.EVENT_POSTED)) {
				NotificationManager.this.onNotificationPosted((Notification) intent
						.getParcelableExtra(NotificationService.KEY_NOTIFICATION));
			} else if (type.equals(NotificationService.EVENT_SERVICE_RUNNING)) {
				NotificationManager.this.onNotificationServiceRunning();
			}
		}
	}

	@Inject
	private NotificationManager() {
		this.listeners = new ArrayList();
		this.didNotificationServiceStart = false;
	}

	@Inject
	private void start() {
		this.receiver = new NotificationManagerReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(NotificationService.INTENT_ACTION_EVENTS);
		Log.e("me", "boot NotificationService.INTENT_ACTION_EVENTS");
		this.context.registerReceiver(this.receiver, filter);
		this.keepNotificationsInDrawer = this.settings.getKeepNotificationsInDrawer();
		this.settings.registerSettingChangeListener(this);
		this.processors = this.appManager.getNotificationFilters();
		this.appManager.addListener(this);
		checkIfNotificationSerivceIsRunning();
	}

	public void stop() {
		this.context.unregisterReceiver(this.receiver);
		Log.e("me", "receiver killed");
	}

	public void addListener(NotificationListener listener) {
		this.listeners.add(listener);
	}

	public void removeListener(NotificationListener listener) {
		this.listeners.remove(listener);
	}

	public boolean didNotificationServiceStart() {
		return this.didNotificationServiceStart;
	}

	@Override
	public void onAppLaunched(App app) {
	}

	@Override
	public void onEnabledAppsChanged(List<App> list) {
		this.processors = this.appManager.getNotificationFilters();
	}

	public void removeNotificationsForSMS(String smsPackage, String from, String address, String displayAddress,
			String body, long timestamp) {
		if (!this.keepNotificationsInDrawer) {
			Handler handler = new AnonymousClass_1(smsPackage, from, address, displayAddress, body, timestamp);
			handler.sendMessageDelayed(handler.obtainMessage(), SMS_REMOVE_NOTIFICATION_DELAY);
		}
	}

	boolean processIncomingNotification(Notification n) {
		for (NotificationProcessor processor : this.processors) {
			try {
				if (processor.shouldHandleNotification(n)) {
					NotificationProcessorResult result = processor.processNotification(n);
					RawMessage message = null;
					if (result != null) {
						message = result.getMessage();
					}
					if (!(message == null || TextUtils.isEmpty(message.getBody()) || !Pattern.compile(REGEX_FOR_URLS)
							.matcher(message.getBody()).find())) {
						EventLogger eventLogger = this.eventLoggerManager.getEventLogger();
						if (eventLogger != null) {
							eventLogger.addEvent(EventLogger.MESSAGE_CONTAINS_URL);
						}
					}
					if (message != null) {
						boolean handled = result.shouldRemoveNotification();
						this.inboxManager.saveMesssage(message);
						return handled;
					}
				} else {
					continue;
				}
			} catch (Exception e) {
				Log.d(TAG, "Caught exception: " + Log.getStackTraceString(e));
			}
		}
		return false;
	}

	@Override
	public void onSettingChanged(Settings settings, String key) {
		if (key.equals(Settings.KEY_NOTIFICATIONS_IN_DRAWER)) {
			this.keepNotificationsInDrawer = settings.getKeepNotificationsInDrawer();
		}
	}

	void checkIfNotificationSerivceIsRunning() {
		Intent i = new Intent(NotificationService.INTENT_ACTION_CMDS);
		i.putExtra(SwoopBroadcastReceiver.TYPE_CMD, NotificationService.CMD_CHECK_SERVICE_RUNNING);
		this.context.sendBroadcast(i);
	}

	void removeNotification(Notification notification) {
		String packageName = notification.getPackageName();
		String tag = notification.getTag();
		int id = notification.getId();
		String key = notification.getKey();
		long delay = REMOVE_NOTIFICATION_DELAY;
		if (VERSION.SDK_INT >= 21 && notification.getPriority() >= 1) {
			delay = SHORT_REMOVE_NOTIFICATION_DELAY;
		}
		Handler handler = new AnonymousClass_2(packageName, tag, id, key);
		handler.sendMessageDelayed(handler.obtainMessage(), delay);
	}

	void onNotificationPosted(Notification notification) {
		Log.d(TAG, "Notification posted: [" + notification.getPackageName() + "] " + notification.getContentTitle());
		notification.logNotification();
		if (this.oobManager.isOOBComplete()) {
			for (NotificationListener listener : this.listeners) {
				listener.onNotificationPosted(notification);
			}
			if (processIncomingNotification(notification) && !this.keepNotificationsInDrawer) {
				removeNotification(notification);
			}
		}
	}

	void onNotificationServiceRunning() {
		this.didNotificationServiceStart = true;
		for (NotificationListener listener : this.listeners) {
			listener.onNotificationServiceRunning();
		}
	}
}

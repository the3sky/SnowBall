package blue.stack.snowball.app.apps;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import blue.stack.snowball.app.apps.AppMonitor.AppMonitorListener;
import blue.stack.snowball.app.inbox.InboxManager;
import blue.stack.snowball.app.inbox.InboxSQLiteHelper;
import blue.stack.snowball.app.inbox.Message;
import blue.stack.snowball.app.logging.EventLogger;
import blue.stack.snowball.app.logging.EventLoggerManager;
import blue.stack.snowball.app.notifications.NotificationProcessor;
import blue.stack.snowball.app.settings.Settings;
import blue.stack.snowball.app.settings.Settings.SettingChangeListener;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class AppManager implements SettingChangeListener {
	public static final int NEVER_LAUNCHED = 0;
	private static final String TAG = "AppManager";
	private AppInstalledReceiver appInstallReceiver;
	AppMonitor appMonitor;
	List<App> apps;
	@Inject
	Context context;
	private SQLiteDatabase database;
	private AppManagerSQLiteHelper dbHelper;
	List<App> enabledApps;
	@Inject
	EventLoggerManager eventLoggerManager;
	@Inject
	InboxManager inboxManager;
	List<App> installedApps;
	Map<String, Long> lastLaunchedCache;
	List<AppListener> listeners;
	@Inject
	Settings settings;
	SMSApp smsApp;

	class AppInstalledReceiver extends BroadcastReceiver {
		AppInstalledReceiver() {
		}

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals("android.intent.action.PACKAGE_ADDED")) {
				AppManager.this.onAppInstalled(intent.getData().getSchemeSpecificPart());
			} else if (intent.getAction().equals("android.intent.action.PACKAGE_REPLACED")) {
				AppManager.this.onAppUpdated(intent.getData().getSchemeSpecificPart());
			} else if (intent.getAction().equals("android.intent.action.PACKAGE_REMOVED")) {
				AppManager.this.onAppUninstalled(intent.getData().getSchemeSpecificPart());
			}
		}
	}

	public enum AppLaunchMethod {
		Inbox,
		Lockscreen,
		Quicklaunch,
		Notification,
		HeadsUp
	}

	@Inject
	private AppManager() {
		this.listeners = new ArrayList();
		this.enabledApps = new ArrayList();
		this.apps = new ArrayList();
	}

	@Inject
	private void start() {
		this.appInstallReceiver = new AppInstalledReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction("android.intent.action.PACKAGE_ADDED");
		filter.addAction("android.intent.action.PACKAGE_REPLACED");
		filter.addAction("android.intent.action.PACKAGE_REMOVED");
		filter.addDataScheme("package");
		this.context.registerReceiver(this.appInstallReceiver, filter);
		this.dbHelper = new AppManagerSQLiteHelper(this.context);
		this.database = this.dbHelper.getWritableDatabase();
		loadApps();
		loadEnabledApps();
		loadLastLaunchedCached();
		this.settings.registerSettingChangeListener(this);
		this.appMonitor = new AppMonitor();
		this.appMonitor.setListener(new AppMonitorListener() {
			@Override
			public void onMonitoredAppStarted(App app) {
				AppManager.this.incrementAppLaunched(app, System.currentTimeMillis());
			}
		});
		this.appMonitor.start(this.context, getSupportedApps());
		logInstalledApps();
	}

	public void stop() {
		this.appMonitor.setListener(null);
		this.appMonitor.stop();
		this.appMonitor = null;
		this.lastLaunchedCache = null;
		this.database.close();
		this.context.unregisterReceiver(this.appInstallReceiver);
	}

	public void clear() {
		this.database.delete(AppManagerSQLiteHelper.TABLE_APP_LAUNCH_STATE, null, null);
		loadLastLaunchedCached();
	}

	public void addListener(AppListener listener) {
		this.listeners.add(listener);
	}

	public void removeListener(AppListener listener) {
		this.listeners.remove(listener);
	}

	public void incrementAppLaunched(App app, long lastLaunchedTimestamp) {
		int launchCount = getLaunchCount(app.getAppId());
		if (launchCount == 0) {
			insertAppIntoAppDB(app.getAppId(), lastLaunchedTimestamp);
		} else {
			updateAppInAppDB(app.getAppId(), lastLaunchedTimestamp, launchCount);
		}
		fireOnAppLaunched(app);
	}

	public long getLastLaunchedTimestamp(String appId) {
		Long lastLaunchedObj = this.lastLaunchedCache.get(appId);
		return lastLaunchedObj != null ? lastLaunchedObj.longValue() : 0;
	}

	public int getLaunchCount(String appId) {
		Cursor cursor = this.database.query(AppManagerSQLiteHelper.TABLE_APP_LAUNCH_STATE,
				new String[] { AppManagerSQLiteHelper.COLUMN_LAUNCH_COUNT }, "_app_id='" + appId + "'", null, null,
				null, null);
		return (cursor == null || !cursor.moveToFirst()) ? NEVER_LAUNCHED : cursor.getInt(NEVER_LAUNCHED);
	}

	public List<App> getSupportedApps() {
		return new ArrayList(this.apps);
	}

	public List<App> getSupportedInstalledApps() {
		if (this.installedApps == null) {
			List<App> installedApps = new ArrayList();
			for (App app : this.apps) {
				if (app.isAppInstalled()) {
					installedApps.add(app);
				}
			}
			this.installedApps = installedApps;
		}
		return this.installedApps;
	}

	private void invalidateSupportedInstalledApps() {
		this.installedApps = null;
	}

	public List<App> getQuickLaunchApps() {
		List<App> quickLaunchApps = getQuickLaunchAppsWithNoCleanup();
		Iterator<App> iter = quickLaunchApps.iterator();
		while (iter.hasNext()) {
			App app = iter.next();
			if (!this.installedApps.contains(app)) {
				iter.remove();
			}
			if (!this.enabledApps.contains(app)) {
				iter.remove();
			}
		}
		removeDuplicatePackages(quickLaunchApps);
		return quickLaunchApps;
	}

	public void setQuickLaunchApps(List<App> quickLaunchApps) {
		List<String> quickLaunchAppIds = new ArrayList();
		for (App quickLaunchApp : quickLaunchApps) {
			quickLaunchAppIds.add(quickLaunchApp.getAppId());
		}
		this.settings.updateQuickLaunchApps(quickLaunchAppIds);
	}

	void removeDuplicatePackages(List<App> apps) {
		List<String> appPackages = new ArrayList();
		List<App> duplicates = new ArrayList();
		for (App app : apps) {
			if (appPackages.contains(app.getAppPackageName())) {
				duplicates.add(app);
			} else {
				appPackages.add(app.getAppPackageName());
			}
		}
		for (App duplicate : duplicates) {
			apps.remove(duplicate);
		}
	}

	List<App> getQuickLaunchAppsWithNoCleanup() {
		List<String> quickLaunchAppIds = this.settings.getQuickLaunchApps();
		List<App> installedApps = getSupportedInstalledApps();
		if (quickLaunchAppIds == null) {
			removeDuplicatePackages(installedApps);
			List<App> quickLaunchApps = new ArrayList(installedApps);
			setQuickLaunchApps(quickLaunchApps);
			return quickLaunchApps;
		}
		List<App> quickLaunchApps = new ArrayList();
		for (String quickLaunchAppId : quickLaunchAppIds) {
			App app = getAppById(quickLaunchAppId);
			if (app != null) {
				quickLaunchApps.add(app);
			}
		}
		for (App installedApp : installedApps) {
			if (!quickLaunchApps.contains(installedApp)) {
				quickLaunchApps.add(installedApp);
			}
		}
		return quickLaunchApps;
	}

	public App getAppById(String appId) {
		for (App app : this.apps) {
			if (app.getAppId().equals(appId)) {
				return app;
			}
		}
		return null;
	}

	public String getSMSAppPackageName() {
		return this.smsApp != null ? this.smsApp.getAppPackageName() : null;
	}

	public void launchAppWithBackButton(App app, AppLaunchMethod launchedFrom) {
		launchApp(app, launchedFrom);
	}

	public void launchAppForMessageWithBackButton(Message message, AppLaunchMethod launchedFrom) {
		launchAppForMessage(message, launchedFrom);
	}

	public void launchApp(App app, AppLaunchMethod launchedFrom) {
		registerAppLaunched(app, launchedFrom);
		Intent launchIntent = app.getLaunchIntent();
		if (launchIntent != null) {
			launchIntent.addFlags(268435456);
			safeStartActivity(launchIntent);
		}
	}

	public void launchAppForMessage(Message message, AppLaunchMethod launchedFrom) {
		App app = getAppById(message.getAppId());
		registerAppLaunched(app, launchedFrom);
		PendingIntent pendingIntent = message.getPendingIntent();
		if (pendingIntent != null) {
			try {
				pendingIntent.send();
				return;
			} catch (CanceledException e) {
				Log.d(TAG, "Failed to launch pending intent because it was cancelled");
			}
		}
		Intent launchIntent = app.getLaunchIntentForMessage(message);
		if (launchIntent != null) {
			launchIntent.addFlags(268435456);
			safeStartActivity(launchIntent);
		}
	}

	public void launchAppWithMessageId(int messageId, AppLaunchMethod launchedFrom) {
		Message message = this.inboxManager.getMessage(messageId);
		if (message != null) {
			launchAppForMessage(message, launchedFrom);
		}
	}

	public void launchAppForAppId(String appId, AppLaunchMethod launchedFrom) {
		launchApp(getAppById(appId), launchedFrom);
	}

	public List<NotificationProcessor> getNotificationFilters() {
		List<NotificationProcessor> notificationProcessors = new ArrayList();
		for (App enabledApp : this.enabledApps) {
			if (enabledApp instanceof NotificationProcessor) {
				notificationProcessors.add((NotificationProcessor) enabledApp);
			}
		}
		return notificationProcessors;
	}

	public boolean isAppRunning(String appId) {
		for (App runningApp : this.appMonitor.getCurrentlyRunningMonitoredApps()) {
			if (runningApp.getAppId().equals(appId)) {
				return true;
			}
		}
		return false;
	}

	public List<App> getCurrentRunningApps() {
		return this.appMonitor.getCurrentlyRunningMonitoredApps();
	}

	void safeStartActivity(Intent intent) {
		try {
			this.context.startActivity(intent);
		} catch (ActivityNotFoundException e) {
			Log.d(TAG, "Failed to start activity");
		}
	}

	void loadLastLaunchedCached() {
		this.lastLaunchedCache = new HashMap();
		Cursor cursor = this.database.query(AppManagerSQLiteHelper.TABLE_APP_LAUNCH_STATE, new String[] {
				InboxSQLiteHelper.COLUMN_USERS_APP_ID, AppManagerSQLiteHelper.COLUMN_LAST_LAUNCHED }, null, null, null,
				null, null);
		if (cursor != null) {
			while (cursor.moveToNext()) {
				this.lastLaunchedCache.put(cursor.getString(NEVER_LAUNCHED), Long.valueOf(cursor.getLong(1)));
			}
		}
	}

	void insertAppIntoAppDB(String appId, long lastLaunchedTimestamp) {
		ContentValues values = new ContentValues();
		values.put(InboxSQLiteHelper.COLUMN_USERS_APP_ID, appId);
		values.put(AppManagerSQLiteHelper.COLUMN_LAST_LAUNCHED, Long.valueOf(lastLaunchedTimestamp));
		values.put(AppManagerSQLiteHelper.COLUMN_LAUNCH_COUNT, Integer.valueOf(1));
		if (this.database.insert(AppManagerSQLiteHelper.TABLE_APP_LAUNCH_STATE, null, values) == -1) {
			Log.d(TAG, "Failed to insert app in appdb table");
		}
		this.lastLaunchedCache.put(appId, Long.valueOf(lastLaunchedTimestamp));
	}

	void updateAppInAppDB(String appId, long lastLaunchedTimestamp, int launchCount) {
		ContentValues values = new ContentValues();
		values.put(InboxSQLiteHelper.COLUMN_USERS_APP_ID, appId);
		values.put(AppManagerSQLiteHelper.COLUMN_LAST_LAUNCHED, Long.valueOf(lastLaunchedTimestamp));
		values.put(AppManagerSQLiteHelper.COLUMN_LAUNCH_COUNT, Integer.valueOf(launchCount));
		if (this.database
				.update(AppManagerSQLiteHelper.TABLE_APP_LAUNCH_STATE, values, "_app_id='" + appId + "'", null) == 0) {
			Log.d(TAG, "Failed to update app in appdb table");
		}
		this.lastLaunchedCache.put(appId, Long.valueOf(lastLaunchedTimestamp));
	}

	void fireOnAppLaunched(App app) {
		for (AppListener listener : this.listeners) {
			listener.onAppLaunched(app);
		}
	}

	void fireOnEnabledAppsChanged() {
		for (AppListener listener : this.listeners) {
			listener.onEnabledAppsChanged(this.enabledApps);
		}
	}

	void registerAppLaunched(App app, AppLaunchMethod launchedFrom) {
		EventLogger eventLogger = this.eventLoggerManager.getEventLogger();
		incrementAppLaunched(app, System.currentTimeMillis());
		Map properties = new HashMap();
		properties.put(EventLogger.PROPERTY_APP_ID, app.getAppId());
		if (launchedFrom == AppLaunchMethod.Lockscreen) {
			properties.put(EventLogger.PROPERTY_APP_OPENED_METHOD, EventLogger.VALUE_SNOWBALL_OPENED_VIA_LOCKSCREEN);
		} else if (launchedFrom == AppLaunchMethod.Quicklaunch) {
			properties.put(EventLogger.PROPERTY_APP_OPENED_METHOD, EventLogger.VALUE_APP_OPENED_VIA_QUICKLAUNCH);
		} else if (launchedFrom == AppLaunchMethod.Notification) {
			properties.put(EventLogger.PROPERTY_APP_OPENED_METHOD, EventLogger.VALUE_APP_OPENED_VIA_NOTIFICATION);
		} else if (launchedFrom == AppLaunchMethod.HeadsUp) {
			properties.put(EventLogger.PROPERTY_APP_OPENED_METHOD, EventLogger.VALUE_APP_OPENED_VIA_HEADSUP);
		} else {
			properties.put(EventLogger.PROPERTY_APP_OPENED_METHOD, EventLogger.VALUE_APP_OPENED_VIA_INBOX);
		}
		eventLogger.addEvent(EventLogger.APP_OPENED, properties);
	}

	@Override
	public void onSettingChanged(Settings settings, String key) {
		if (key.equals(Settings.KEY_EXCLUDED_APPS)) {
			loadEnabledApps();
			fireOnEnabledAppsChanged();
		}
	}

	void loadApps() {
		App app;
		for (App app2 : this.apps) {
			app2.stop();
		}
		this.apps.clear();
		this.smsApp = null;
		Class[] arr$ = AppIds.SUPPORTED_APPS;
		int len$ = arr$.length;
		App app2;
		for (int i$ = NEVER_LAUNCHED; i$ < len$; i$++) {
			try {
				app2 = (App) arr$[i$].getConstructor(new Class[NEVER_LAUNCHED]).newInstance(new Object[NEVER_LAUNCHED]);
				app2.start(this.context);
				this.apps.add(app2);
				if (app2 instanceof SMSApp) {
					this.smsApp = (SMSApp) app2;
				}
			} catch (InstantiationException e) {
				Log.d(TAG, "InstantiationException when instantiating app");
			} catch (IllegalAccessException e2) {
				Log.d(TAG, "IllegalAccessException when instantiating app");
			} catch (NoSuchMethodException e3) {
				Log.d(TAG, "NoSuchMethodException when instantiating app");
			} catch (InvocationTargetException ite) {
				Log.d(TAG, "InvocationTargetException when instantiating app: " + Log.getStackTraceString(ite));
			}
		}
	}

	void loadEnabledApps() {
		Set<String> excludedFilters = this.settings.getExcludedApps();
		this.enabledApps.clear();
		for (App app : this.apps) {
			if (!excludedFilters.contains(app.getAppId())) {
				this.enabledApps.add(app);
			}
		}
	}

	void onAppInstalled(String packageName) {
		restartApp(packageName);
		addInstalledAppToQuickLaunch(packageName);
		logInstalledApps();
	}

	void onAppUpdated(String packageName) {
		restartApp(packageName);
		logInstalledApps();
	}

	void onAppUninstalled(String packageName) {
		removeUninstalledAppFromQuickLaunch(packageName);
		logInstalledApps();
	}

	void restartApp(String packageName) {
		for (App app : this.apps) {
			if (packageName.equals(app.getAppPackageName())) {
				app.restart();
			}
		}
	}

	void addInstalledAppToQuickLaunch(String packageName) {
		invalidateSupportedInstalledApps();
		App installedApp = null;
		for (App supportedApp : getSupportedApps()) {
			if (supportedApp.getAppPackageName().equals(packageName)) {
				installedApp = supportedApp;
				break;
			}
		}
		if (installedApp != null) {
			List<App> quickLaunchApps = getQuickLaunchApps();
			for (App quickLaunchApp : quickLaunchApps) {
				if (quickLaunchApp.getAppPackageName().equals(packageName)) {
					return;
				}
			}
			quickLaunchApps.add(installedApp);
			setQuickLaunchApps(quickLaunchApps);
		}
	}

	// void addInstalledAppToQuickLaunchX(String packageName) {
	// invalidateSupportedInstalledApps();
	// App installedApp = null;
	// for (App supportedApp : getSupportedApps()) {
	// if (supportedApp.getAppPackageName().equals(packageName)) {
	// installedApp = supportedApp;
	// break;
	// }
	// }
	// if (installedApp != null) {
	// List<App> quickLaunchApps = getQuickLaunchApps();
	// for (App quickLaunchApp : quickLaunchApps) {
	// if (quickLaunchApp.getAppPackageName().equals(packageName)) {
	// return;
	// }
	// }
	// quickLaunchApps.add(installedApp);
	// setQuickLaunchApps(quickLaunchApps);
	// }
	// }

	void removeUninstalledAppFromQuickLaunch(String packageName) {
		invalidateSupportedInstalledApps();
		List<App> uninstalledApps = new ArrayList();
		List<App> quickLaunchApps = getQuickLaunchAppsWithNoCleanup();
		for (App quickLaunchApp : quickLaunchApps) {
			if (quickLaunchApp.getAppPackageName().equals(packageName)) {
				uninstalledApps.add(quickLaunchApp);
			}
		}
		if (uninstalledApps.size() != 0) {
			for (App uninstalledApp : uninstalledApps) {
				quickLaunchApps.remove(uninstalledApp);
			}
			setQuickLaunchApps(quickLaunchApps);
		}
	}

	void logInstalledApps() {
		List<App> installedApps = getSupportedInstalledApps();
		this.eventLoggerManager.getEventLogger().setPersonProperty(EventLogger.APPS_INSTALLED_COUNT,
				Integer.toString(installedApps.size()));
		this.eventLoggerManager.getEventLogger().addSuperProperty(EventLogger.APPS_INSTALLED_COUNT,
				Integer.toString(installedApps.size()));
		List installedAppIds = new ArrayList();
		for (App installedApp : installedApps) {
			installedAppIds.add(installedApp.getAppId());
		}
		this.eventLoggerManager.getEventLogger().setPersonProperty(EventLogger.APPS_INSTALLED, installedAppIds);
		this.eventLoggerManager.getEventLogger().addSuperProperty(EventLogger.APPS_INSTALLED, installedAppIds);
	}
}

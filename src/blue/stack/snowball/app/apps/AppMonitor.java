package blue.stack.snowball.app.apps;

import java.util.ArrayList;
import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.PowerManager;
import android.util.Log;

public class AppMonitor {
	private static final String ACTION_MONITOR_APPS = "blue.stack.snowball.app.apps.AppMonitor.action_monitor_apps";
	private static final int ALARM_REPEAT_TIME = 2000;
	private static final boolean ENABLE_PERIODIC_MONITOR = false;
	private static final String TAG = "AppMonitor";
	PendingIntent alarmPendingIntent;
	List<App> appsToMonitor;
	Context context;
	List<App> lastSeenRunningApps;
	AppMonitorListener listener;
	AppMonitorReceiver receiver;

	public static interface AppMonitorListener {
		void onMonitoredAppStarted(App app);
	}

	class AppMonitorReceiver extends BroadcastReceiver {
		AppMonitorReceiver() {
		}

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals("android.intent.action.SCREEN_OFF")) {
				AppMonitor.this.onScreenOff();
			} else if (intent.getAction().equals("android.intent.action.SCREEN_ON")) {
				AppMonitor.this.onScreenOn();
			} else if (intent.getAction().equals(ACTION_MONITOR_APPS)) {
				AppMonitor.this.onMonitorApps();
			}
		}
	}

	public void start(Context context, List<App> appsToMonitor) {
		this.context = context;
		this.appsToMonitor = appsToMonitor;
		this.lastSeenRunningApps = new ArrayList();
		IntentFilter filter = new IntentFilter();
		filter.addAction("android.intent.action.SCREEN_OFF");
		filter.addAction("android.intent.action.SCREEN_ON");
		filter.addAction(ACTION_MONITOR_APPS);
		this.receiver = new AppMonitorReceiver();
		context.registerReceiver(this.receiver, filter);
		if (((PowerManager) context.getSystemService("power")).isScreenOn()) {
			startMonitoring();
		}
	}

	public void stop() {
		stopMonitoring();
		this.context.unregisterReceiver(this.receiver);
		this.receiver = null;
		this.lastSeenRunningApps = null;
		this.appsToMonitor = null;
		this.listener = null;
		this.context = null;
	}

	public void setListener(AppMonitorListener listener) {
		this.listener = listener;
	}

	void onMonitorApps() {
		List<App> runningApps = getCurrentlyRunningMonitoredApps();
		for (App runningApp : runningApps) {
			if (!this.lastSeenRunningApps.contains(runningApp)) {
				Log.d(TAG, "New app started running: " + runningApp.getAppId());
				this.listener.onMonitoredAppStarted(runningApp);
			}
		}
		this.lastSeenRunningApps = runningApps;
	}

	void onScreenOff() {
		stopMonitoring();
	}

	void onScreenOn() {
		startMonitoring();
	}

	void startMonitoring() {
	}

	void stopMonitoring() {
		if (this.alarmPendingIntent != null) {
			Log.d(TAG, "Stopping app monitoring");
			this.lastSeenRunningApps.clear();
			((AlarmManager) this.context.getSystemService("alarm")).cancel(this.alarmPendingIntent);
			this.alarmPendingIntent.cancel();
			this.alarmPendingIntent = null;
		}
	}

	public List<App> getCurrentlyRunningMonitoredApps() {
		List<App> runningMonitoredApps = new ArrayList();
		List<RunningAppProcessInfo> procInfos = ((ActivityManager) this.context.getSystemService("activity"))
				.getRunningAppProcesses();
		if (procInfos != null) {
			for (RunningAppProcessInfo procInfo : procInfos) {
				String processName = procInfo.processName;
				if (!shouldExcludeRunningApp(procInfo)) {
					for (App app : this.appsToMonitor) {
						if (app.getAppPackageName() != null && app.getAppPackageName().equals(processName)) {
							runningMonitoredApps.add(app);
						}
					}
				}
			}
		}
		return runningMonitoredApps;
	}

	boolean shouldExcludeRunningApp(RunningAppProcessInfo procInfo) {
		try {
			int flags = RunningAppProcessInfo.class.getField("flags").getInt(procInfo);
			if (procInfo.importance != 100) {
				return true;
			}
			if ((flags & 2) == 2) {
				return true;
			}
			return (flags & 4) == 0 ? true : ENABLE_PERIODIC_MONITOR;
		} catch (NoSuchFieldException e) {
			return true;
		} catch (IllegalAccessException e2) {
			return true;
		}
	}
}

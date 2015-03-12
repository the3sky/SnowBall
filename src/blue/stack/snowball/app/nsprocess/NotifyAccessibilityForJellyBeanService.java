package blue.stack.snowball.app.nsprocess;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Notification;
import android.content.Intent;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

public class NotifyAccessibilityForJellyBeanService extends AccessibilityService {
	private boolean a;

	public NotifyAccessibilityForJellyBeanService() {
		this.a = false;
	}

	@Override
	public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
		Log.i("NotifyAccessibilityForJellyBeanService", "onAccessibilityEvent");
		if (accessibilityEvent != null) {
			String str = accessibilityEvent.getPackageName().toString();
			Log.i("NotifyAccessibilityForJellyBeanService", "onAccessibilityEvent packageName = " + str);
			if (str.contains(str)) {
				switch (accessibilityEvent.getEventType()) {
				case AccessibilityNodeInfoCompat.ACTION_ACCESSIBILITY_FOCUS /* 64 */:
					if (accessibilityEvent.getParcelableData() instanceof Notification) {
						Notification notification = (Notification) accessibilityEvent.getParcelableData();
						// if (notification != null) {
						// Bitmap a = a.a().a(getApplicationContext(),
						// notification.contentView, str);
						// if (a == null || (a != null && a.getHeight() < 100))
						// {
						// a = a.a().a(getApplicationContext(), str);
						// }
						// a.a().a(str, a);
						// c.a(getApplicationContext(), notification, str,
						// c.a(getApplicationContext(),
						// notification.contentView, ""));
						// return;
						// }
						Log.i("NotifyAccessibilityForJellyBeanService", "onAccessibilityEvent : notification == null"
								+ notification.tickerText);
						return;
					}
					Log.i("NotifyAccessibilityForJellyBeanService", "onAccessibilityEvent : is not notification!!!");
					return;
				default:
					return;
				}
			}
			Log.i("NotifyAccessibilityForJellyBeanService", "Not contains this application!!!!!!!");
		}
	}

	@Override
	public void onCreate() {
		Log.i("NotifyAccessibilityForJellyBeanService", "NotifyAccessibilityService onCreate");
		a();
		super.onCreate();
	}

	@Override
	public void onInterrupt() {
		Log.i("NotifyAccessibilityForJellyBeanService", "NotifyAccessibilityService onInterrupt");
		this.a = false;
	}

	private void a() {
	}

	@Override
	public void onServiceConnected() {
		if (!this.a) {
			Log.i("NotifyAccessibilityForJellyBeanService",
					"NotifyAccessibilityService Service connected not jelly bean");
			AccessibilityServiceInfo accessibilityServiceInfo = new AccessibilityServiceInfo();
			accessibilityServiceInfo.eventTypes = 64;
			accessibilityServiceInfo.feedbackType = 16;
			setServiceInfo(accessibilityServiceInfo);
			Log.i("NotifyAccessibilityForJellyBeanService",
					"NotifyAccessibilityService Service connected not jelly bean DONE");
			a();
			this.a = true;

			Log.i("chenbingdong", "Global.sIsSettingEnable = true;");
		}
	}

	@Override
	public void onDestroy() {
		Log.i("NotifyAccessibilityForJellyBeanService", "NotifyAccessibilityService onDestroy");

		Log.i("chenbingdong", "Global.sIsSettingEnable = false;");
		super.onDestroy();
	}

	@Override
	public int onStartCommand(Intent intent, int i, int i2) {
		Log.i("NotifyAccessibilityForJellyBeanService", "NotifyAccessibilityService onStartCommand");
		return super.onStartCommand(intent, i, i2);
	}
}
package blue.stack.snowball.app.nsprocess;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Notification;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.view.accessibility.AccessibilityEvent;

public class NotifyAccessibilityService extends AccessibilityService {
	private boolean a;

	public NotifyAccessibilityService() {
		this.a = false;
		System.out.println("NotifyAccessibilityService.NotifyAccessibilityService()");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.accessibilityservice.AccessibilityService#onAccessibilityEvent
	 * (android.view.accessibility.AccessibilityEvent)
	 */
	@Override
	public void onAccessibilityEvent(AccessibilityEvent event) {
		System.out.println("NotifyAccessibilityService.onAccessibilityEvent()");
		if (event != null) {
			String str = event.getPackageName().toString();
			// d.a("NotifyAccessibilityService",
			// "onAccessibilityEvent packageName = " + str);
			if (str.contains(str)) {// TODO
				switch (event.getEventType()) {
				case AccessibilityNodeInfoCompat.ACTION_ACCESSIBILITY_FOCUS /* 64 */:
					if (event.getParcelableData() instanceof Notification) {
						Notification notification = (Notification) event.getParcelableData();
						if (notification != null) {
							System.out.println(notification.tickerText);

						}
						// d.a("NotifyAccessibilityService",
						// "onAccessibilityEvent : notification == null");
						return;
					}
					// d.a("NotifyAccessibilityService",
					// "onAccessibilityEvent : is not notification!!!");
					return;
				default:
					return;
				}
			}
			// d.a("NotifyAccessibilityService",
			// "Not contains this application!!!!!!!");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.accessibilityservice.AccessibilityService#onInterrupt()
	 */
	@Override
	public void onInterrupt() {
		// TODO Auto-generated method stub
		a = false;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.accessibilityservice.AccessibilityService#onServiceConnected()
	 */
	@Override
	public void onServiceConnected() {
		System.out.println("NotifyAccessibilityService.onServiceConnected()");
		if (!this.a) {
			// d.a("NotifyAccessibilityService",
			// "NotifyAccessibilityService Service connected not jelly bean");
			AccessibilityServiceInfo accessibilityServiceInfo = new AccessibilityServiceInfo();
			accessibilityServiceInfo.packageNames = null;// new String[] {
															// "com.tencent.mm"
															// };
			accessibilityServiceInfo.eventTypes = AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED;
			accessibilityServiceInfo.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
			setServiceInfo(accessibilityServiceInfo);

			this.a = true;
			// c.h = true;
			// d.a("chenbingdong", "Global.sIsSettingEnable = true;");
		}
	}
}

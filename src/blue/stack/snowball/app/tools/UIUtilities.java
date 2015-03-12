package blue.stack.snowball.app.tools;

import android.os.Handler;
import android.support.v4.view.accessibility.AccessibilityEventCompat;
import android.text.format.DateUtils;

public class UIUtilities {
	public static void delayOneFrame(Runnable runnable) {
		new Handler().postDelayed(runnable, 15);
	}

	public static String formatTimestamp(long timestamp) {
		return timestamp == 0 ? "" : DateUtils.getRelativeTimeSpanString(timestamp, System.currentTimeMillis(), 1000,
				AccessibilityEventCompat.TYPE_GESTURE_DETECTION_END).toString();
	}
}

package blue.stack.snowball.app.tools;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.os.Build.VERSION;

public class Tools {
	public static boolean isServiceRunning(Context context, Class<?> serviceClass) {
		for (RunningServiceInfo service : ((ActivityManager) context.getSystemService("activity"))
				.getRunningServices(Integer.MAX_VALUE)) {
			if (serviceClass.getName().equals(service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}

	public static boolean hasJellyBeanMR2() {
		return VERSION.SDK_INT >= 18;
	}

	public static boolean hasKitKat() {
		return VERSION.SDK_INT >= 19;
	}
}

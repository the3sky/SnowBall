package blue.stack.snowball.app.apps;

import java.util.ArrayList;
import java.util.List;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build.VERSION;
import android.provider.Telephony.Sms;
import android.util.Log;

public class AppIds {
	public static final Class[] SUPPORTED_APPS;
	private static final String TAG = "AppIds";

	static {
		SUPPORTED_APPS = new Class[] { SMSApp.class, FBMessengerApp.class, WhatsAppApp.class, SnapchatApp.class,
				HangoutsApp.class, TwitterApp.class, QQApp.class, WeChatApp.class, SlackApp.class, TelegramApp.class,
				LineMessengerApp.class, SkypeApp.class, ViberApp.class, HikeMessengerApp.class, BBMApp.class,
				KikApp.class, GroupMeMessengerApp.class, KakaoTalkApp.class };
	}

	public static List<String> getSupportedAppPackageNames() {
		List<String> packageNames = new ArrayList();
		for (Class supportedApp : SUPPORTED_APPS) {
			try {
				Object value = supportedApp.getDeclaredField("PACKAGE_NAME").get(null);
				if (value != null && (value instanceof String)) {
					packageNames.add((String) value);
				}
			} catch (NoSuchFieldException e) {
				Log.d(TAG, "Failed to find PACKAGE_NAME for class: " + supportedApp.getCanonicalName());
			} catch (IllegalAccessException e2) {
				Log.d(TAG,
						"Illegal access exception when finding PACKAGE_NAME for class: "
								+ supportedApp.getCanonicalName());
			}
		}
		return packageNames;
	}

	@TargetApi(19)
	public static String getSMSAppPackageName(Context context) {
		if (VERSION.SDK_INT >= 19) {
			return Sms.getDefaultSmsPackage(context);
		}
		Intent intent = new Intent("android.intent.action.MAIN");
		intent.addCategory("android.intent.category.DEFAULT");
		intent.setType("vnd.android-dir/mms-sms");
		ComponentName componentName = intent.resolveActivity(context.getPackageManager());
		return componentName != null ? componentName.getPackageName() : null;
	}
}

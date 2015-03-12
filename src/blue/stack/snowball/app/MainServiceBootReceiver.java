package blue.stack.snowball.app;

import static blue.stack.snowball.app.MainService.LAUNCH_REASON_DEFAULT;
import static blue.stack.snowball.app.MainService.LAUNCH_REASON_PACKAGE_REPLACED;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import blue.stack.snowball.app.core.GuiceModule;
import blue.stack.snowball.app.oob.OOBManager;

public class MainServiceBootReceiver extends BroadcastReceiver {
	private static final String TAG = "MainServiceBootReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
			Log.d(TAG, "Boot complete");
			GuiceModule.construct(context);
			if (GuiceModule.get().getInstance(OOBManager.class).isOOBComplete()) {
				MainService.startMainService(context, LAUNCH_REASON_DEFAULT);
			}
		} else if (intent.getAction().equals("android.intent.action.MY_PACKAGE_REPLACED")) {
			Log.d(TAG, "My Packaged Replaces");
			MainService.startMainService(context, LAUNCH_REASON_PACKAGE_REPLACED);
		}
	}
}

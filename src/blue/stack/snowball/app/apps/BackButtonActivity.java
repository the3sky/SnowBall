package blue.stack.snowball.app.apps;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import blue.stack.snowball.app.R;
import blue.stack.snowball.app.apps.AppManager.AppLaunchMethod;
import blue.stack.snowball.app.core.GuiceModule;
import blue.stack.snowball.app.inbox.Message;
import blue.stack.snowball.app.inbox.ui.InboxViewManager;

import com.google.inject.Inject;

public class BackButtonActivity extends Activity {
	private static final int FINISH_DELAY = 3000;
	private static final String KEY_APP_ID = "appId";
	private static final String KEY_APP_LAUNCH_METHOD = "appLaunchMethod";
	private static final String KEY_MESSAGE_ID = "messageId";
	private static final String KEY_TYPE = "type";
	private static final String LAUNCH_TYPE_APP = "app";
	private static final String LAUNCH_TYPE_MESSAGE = "message";
	private static final String TAG = "BackButtonActivity";
	@Inject
	private AppManager appManager;
	boolean hasLaunched;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		GuiceModule.get().injectMembers(this);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_back_button);
		findViewById(R.id.backbutton_activity_view).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				Log.d(TAG, "Received touch.  Self destructing");
				BackButtonActivity.this.finishWithNoAnimation();
			}
		});
		Log.d(TAG, "BackButtonActivity starting!");
		this.hasLaunched = false;
	}

	@Override
	protected void onDestroy() {
		Log.d(TAG, "BackButtonActivity is being destroyed");
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (!this.hasLaunched) {
			Intent intent = getIntent();
			String type = intent.getStringExtra(KEY_TYPE);
			if (LAUNCH_TYPE_APP.equals(type)) {
				launchApp(
						intent.getStringExtra(KEY_APP_ID),
						AppLaunchMethod.values()[intent.getIntExtra(KEY_APP_LAUNCH_METHOD,
								AppLaunchMethod.Inbox.ordinal())]);
			} else if (LAUNCH_TYPE_MESSAGE.equals(type)) {
				launchMessage(
						intent.getIntExtra(KEY_MESSAGE_ID, -1),
						AppLaunchMethod.values()[intent.getIntExtra(KEY_APP_LAUNCH_METHOD,
								AppLaunchMethod.Inbox.ordinal())]);
			}
			this.hasLaunched = true;
		}
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		if (this.hasLaunched) {
			openDrawerAndFinish();
		}
	}

	void finishWithNoAnimation() {
		finish();
		overridePendingTransition(0, 0);
	}

	void openDrawerAndFinish() {
		GuiceModule.get().getInstance(InboxViewManager.class).openDrawer();
		Log.d(TAG, "Opened drawer");
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				Log.d(TAG, "BackButtonActivity finishing");
				BackButtonActivity.this.finishWithNoAnimation();
			}
		}, 3000);
	}

	void launchApp(String appId, AppLaunchMethod launchedFrom) {
		if (appId != null) {
			App app = this.appManager.getAppById(appId);
			if (app != null) {
				this.appManager.launchApp(app, launchedFrom);
			}
		}
	}

	void launchMessage(int messageId, AppLaunchMethod launchedFrom) {
		this.appManager.launchAppWithMessageId(messageId, launchedFrom);
	}

	public static void launchAppForMessage(Context context, Message message, AppLaunchMethod launchedFrom) {
		Intent intent = new Intent(context, BackButtonActivity.class);
		intent.putExtra(KEY_TYPE, LAUNCH_TYPE_MESSAGE);
		intent.putExtra(KEY_MESSAGE_ID, message.getId());
		intent.putExtra(KEY_APP_LAUNCH_METHOD, launchedFrom.ordinal());
		intent.addFlags(268533760);
		context.startActivity(intent);
	}

	public static void launchApp(Context context, App app, AppLaunchMethod launchedFrom) {
		Intent intent = new Intent(context, BackButtonActivity.class);
		intent.putExtra(KEY_TYPE, LAUNCH_TYPE_APP);
		intent.putExtra(KEY_APP_ID, app.getAppId());
		intent.putExtra(KEY_APP_LAUNCH_METHOD, launchedFrom.ordinal());
		intent.addFlags(268533760);
		context.startActivity(intent);
	}
}

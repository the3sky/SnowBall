package blue.stack.snowball.app.oob;

import blue.stack.snowball.app.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.view.View;
import android.view.View.OnClickListener;
import blue.stack.snowball.app.core.GuiceModule;
import blue.stack.snowball.app.logging.EventLogger;
import blue.stack.snowball.app.logging.EventLoggerManager;

import com.google.inject.Inject;

public class OOBSplashScreenActivity extends Activity implements OOBListener {
	@Inject
	EventLoggerManager eventLoggerManager;
	@Inject
	OOBManager oobManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		GuiceModule.get().injectMembers(this);
		setContentView(R.layout.activity_oob_splash);
		findViewById(R.id.oob_next).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				OOBSplashScreenActivity.this.startOOBTutorial();
			}
		});
		this.oobManager.addListener(this);
		EventLogger eventLogger = this.eventLoggerManager.getEventLogger();
		if (this.oobManager.isOOBTutorialComplete()) {
			eventLogger.addEvent(EventLogger.OOB_SPLASH_SKIPPED);
			startOOBTutorial();
			finish();
			return;
		}
		eventLogger.addEvent(EventLogger.OOB_SPLASH_SHOWN);
	}

	void startOOBTutorial() {
		Intent intent = new Intent(this, OOBStartupSpinner.class);
		intent.addFlags(AccessibilityNodeInfoCompat.ACTION_CUT);
		startActivity(intent);
	}

	@Override
	protected void onDestroy() {
		this.oobManager.removeListener(this);
		super.onDestroy();
	}

	@Override
	public void onOOBComplete() {
		finish();
	}
}

package blue.stack.snowball.app.oob;

import blue.stack.snowball.app.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import blue.stack.snowball.app.core.GuiceModule;
import blue.stack.snowball.app.logging.EventLoggerManager;
import blue.stack.snowball.app.settings.Settings;

import com.google.inject.Inject;

public class OOBStartupSpinner extends Activity {
	boolean completedStartup;
	@Inject
	EventLoggerManager eventLoggerManager;
	@Inject
	Settings settings;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_oobstartup_spinner);
		GuiceModule.get().injectMembers(this);
		this.completedStartup = false;

	}

	private void completeStartup() {
		if (!this.completedStartup) {
			this.completedStartup = true;
			Intent intent = new Intent(this, OOBTutorialActivity.class);
			intent.addFlags(AccessibilityNodeInfoCompat.ACTION_CUT);
			startActivity(intent);
			finish();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		completeStartup();
	}

	// @Override
	// public void onTestsReady() {
	// completeStartup();
	// }

	@Override
	protected void onDestroy() {

		super.onDestroy();

	}
}

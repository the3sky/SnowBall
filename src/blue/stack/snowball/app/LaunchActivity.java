package blue.stack.snowball.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import blue.stack.snowball.app.core.GuiceModule;
import blue.stack.snowball.app.oob.OOBManager;

import com.google.inject.Inject;

public class LaunchActivity extends Activity {
	@Inject
	OOBManager oobManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		GuiceModule.construct(this);
		GuiceModule.get().injectMembers(this);
		setContentView(R.layout.activity_launch);
	}

	@Override
	protected void onStart() {
		super.onStart();
		if (!this.oobManager.isOOBComplete()) {
			this.oobManager.startOOB();
		} else if (this.oobManager.getOOBNeedsShadeMigration()) {
			startActivity(new Intent(this, MigrateActivity.class));
		} else {
			startActivity(new Intent(this, MainActivity.class));
		}
		finish();
	}
}

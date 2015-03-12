package blue.stack.snowball.app;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import blue.stack.snowball.app.core.GuiceModule;
import blue.stack.snowball.app.logging.EventLogger;
import blue.stack.snowball.app.logging.EventLoggerManager;
import blue.stack.snowball.app.settings.Settings;
import blue.stack.snowball.app.settings.Settings.SettingChangeListener;
import blue.stack.snowball.app.settings.SettingsActivity;
import blue.stack.snowball.app.shade.ShadeNotificationController;
import blue.stack.snowball.app.shade.ShadeNotificationController.DefaultShadeNotificationListener;
import blue.stack.snowball.app.shade.ShadeViewManager;

import com.google.inject.Inject;

public class MigrateActivity extends Activity implements SettingChangeListener {
	@Inject
	EventLoggerManager eventLoggerManager;
	@Inject
	Settings settings;
	@Inject
	ShadeNotificationController shadeNotificationController;
	@Inject
	ShadeViewManager shadeViewManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		GuiceModule.construct(this);
		GuiceModule.get().injectMembers(this);
		setContentView(R.layout.activity_migrate);
		this.settings.registerSettingChangeListener(this);
		onLeftModeModeChanged(this.settings.getEnableLeftHandedMode());
		findViewById(R.id.settings).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				MigrateActivity.this.startActivity(SettingsActivity.getLaunchIntent(MigrateActivity.this));
			}
		});
		this.shadeViewManager.addShadeStatusBar();
	}

	@Override
	protected void onPause() {
		super.onPause();
		this.shadeNotificationController.removeListener(this);
		finish();
	}

	@Override
	protected void onResume() {
		super.onResume();
		this.eventLoggerManager.getEventLogger().addEvent(EventLogger.MIGRATE_ACTIVITY_OPENED);
		this.shadeNotificationController.closeAll();
		this.shadeNotificationController.addListener(this, new DefaultShadeNotificationListener() {
			@Override
			public void onNotificationOpening() {
				super.onNotificationOpening();
				MigrateActivity.this.shadeNotificationController.closeAll();
			}
		});
		this.settings.setHasSeenMigrationScreen(true);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		this.settings.unregisterSettingChangeListener(this);
	}

	@Override
	public void onSettingChanged(Settings settings, String key) {
		if (key.equals(Settings.KEY_ENABLE_LEFT_HANDED_MODE)) {
			onLeftModeModeChanged(settings.getEnableLeftHandedMode());
		}
	}

	void onLeftModeModeChanged(boolean enable) {
		this.settings.setEnableLeftHandedMode(enable);
		View left = findViewById(R.id.where_to_pull_image_left);
		View right = findViewById(R.id.where_to_pull_image_right);
		if (enable) {
			left.setVisibility(0);
			right.setVisibility(4);
			return;
		}
		left.setVisibility(4);
		right.setVisibility(0);
	}
}

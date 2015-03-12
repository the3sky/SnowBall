package blue.stack.snowball.app;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import blue.stack.snowball.app.core.GuiceModule;
import blue.stack.snowball.app.logging.EventLogger;
import blue.stack.snowball.app.logging.EventLoggerManager;
import blue.stack.snowball.app.oob.OOBManager;
import blue.stack.snowball.app.settings.Settings;
import blue.stack.snowball.app.settings.Settings.SettingChangeListener;
import blue.stack.snowball.app.settings.SettingsActivity;

import com.google.inject.Inject;

public class MainActivity extends Activity implements SettingChangeListener {
	@Inject
	EventLoggerManager eventLoggerManager;
	@Inject
	OOBManager oobManager;
	@Inject
	Settings settings;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		GuiceModule.construct(this);
		GuiceModule.get().injectMembers(this);
		setContentView(R.layout.activity_main);
		this.eventLoggerManager.getEventLogger().addEvent(EventLogger.MAIN_ACTIVITY_OPENED);
		onLeftModeModeChanged(this.settings.getEnableLeftHandedMode(), true);
		((SwitchCompat) findViewById(R.id.left_handed_mode)).setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
				EventLogger eventLogger = MainActivity.this.eventLoggerManager.getEventLogger();
				if (b) {
					eventLogger.addEvent(EventLogger.SETTING_LEFT_HANDED_MODE_CHANGED,
							EventLogger.PROPERTY_SETTING_LEFT_HANDED_MODE_VALUE, EventLogger.VALUE_ENABLED);
				} else {
					eventLogger.addEvent(EventLogger.SETTING_LEFT_HANDED_MODE_CHANGED,
							EventLogger.PROPERTY_SETTING_LEFT_HANDED_MODE_VALUE, EventLogger.VALUE_DISABLED);
				}
				MainActivity.this.onLeftModeModeChanged(b, false);
			}
		});
		SwitchCompat shadeNotification = (SwitchCompat) findViewById(R.id.headsup_notification);
		onShadeNotificationChanged(this.settings.getEnableHeadsUpNotification(), true);
		shadeNotification.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
				EventLogger eventLogger = MainActivity.this.eventLoggerManager.getEventLogger();
				if (b) {
					eventLogger.addEvent(EventLogger.SETTING_HEADSUP_NOTIFICATION_CHANGED,
							EventLogger.PROPERTY_HEADSUP_SHADE_NOTIFICATION_VALUE, EventLogger.VALUE_ENABLED);
				} else {
					eventLogger.addEvent(EventLogger.SETTING_HEADSUP_NOTIFICATION_CHANGED,
							EventLogger.PROPERTY_HEADSUP_SHADE_NOTIFICATION_VALUE, EventLogger.VALUE_DISABLED);
				}
				MainActivity.this.onShadeNotificationChanged(b, false);
			}
		});
		findViewById(R.id.advanced_settings).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				MainActivity.this.startActivity(SettingsActivity.getLaunchIntent(MainActivity.this));
			}
		});
		this.settings.registerSettingChangeListener(this);
	}

	@Override
	protected void onDestroy() {
		this.settings.unregisterSettingChangeListener(this);
		super.onDestroy();
	}

	void onLeftModeModeChanged(boolean enable, boolean updateUI) {
		this.settings.setEnableLeftHandedMode(enable);
		View left = findViewById(R.id.where_to_pull_image_left);
		View right = findViewById(R.id.where_to_pull_image_right);
		if (enable) {
			left.setVisibility(0);
			right.setVisibility(4);
		} else {
			left.setVisibility(4);
			right.setVisibility(0);
		}
		if (updateUI) {
			((SwitchCompat) findViewById(R.id.left_handed_mode)).setChecked(enable);
		}
	}

	void onShadeNotificationChanged(boolean enable, boolean updateUI) {
		this.settings.setEnableHeadsUpNotification(enable);
		if (updateUI) {
			((SwitchCompat) findViewById(R.id.headsup_notification)).setChecked(enable);
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		if (this.oobManager.isOOBComplete()) {
			MainService.startMainService(this, MainService.LAUNCH_REASON_DEFAULT);
		}
	}

	@Override
	public void onSettingChanged(Settings settings, String key) {
		if (key.equals(Settings.KEY_ENABLE_LEFT_HANDED_MODE)) {
			onLeftModeModeChanged(settings.getEnableLeftHandedMode(), true);
		} else if (key.equals(Settings.KEY_ENABLE_HEADSUP_NOTIFICATION)) {
			onShadeNotificationChanged(settings.getEnableHeadsUpNotification(), true);
		}
	}
}

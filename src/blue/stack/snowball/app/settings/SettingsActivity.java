package blue.stack.snowball.app.settings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import blue.stack.snowball.app.R;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.preference.TwoStatePreference;
import android.widget.Toast;
import blue.stack.snowball.app.apps.App;
import blue.stack.snowball.app.apps.AppManager;
import blue.stack.snowball.app.core.GuiceModule;
import blue.stack.snowball.app.logging.EventLogger;
import blue.stack.snowball.app.logging.EventLoggerManager;
import blue.stack.snowball.app.logging.RemoteLogger;

public class SettingsActivity extends PreferenceActivity {

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static class EnabledAppsFragment extends PreferenceFragment {
		private static final String TAG = "EnabledAppsFragment";
		Set<String> excludedApps;

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.settings_apps);
			this.excludedApps = GuiceModule.get().getInstance(Settings.class).getExcludedApps();
		}

		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState);
			PreferenceScreen screen = getPreferenceScreen();
			PreferenceCategory category = new PreferenceCategory(getActivity());
			category.setTitle(R.string.title_enabled_apps);
			screen.addPreference(category);
			List<CheckBoxPreference> checkBoxPreferences = new ArrayList();
			for (App app : GuiceModule.get().getInstance(AppManager.class).getSupportedInstalledApps()) {
				String appName = app.getAppName();
				Drawable appIcon = app.getAppIcon();
				CheckBoxPreference pref = new CheckBoxPreference(getActivity());
				pref.setPersistent(false);
				pref.setTitle(appName);
				pref.setKey(app.getAppId());
				if (appIcon != null) {
					pref.setIcon(appIcon);
				}
				if (this.excludedApps.contains(app.getAppId())) {
					pref.setChecked(false);
				} else {
					pref.setChecked(true);
				}
				pref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(Preference preference) {
						CheckBoxPreference pref = (CheckBoxPreference) preference;
						blue.stack.snowball.app.settings.SettingsActivity.EnabledAppsFragment.this.updateExcludedList(
								pref.getKey(), pref.isChecked());
						return true;
					}
				});
				checkBoxPreferences.add(pref);
			}
			Collections.sort(checkBoxPreferences, new Comparator() {
				@Override
				public int compare(Object o1, Object o2) {
					return ((CheckBoxPreference) o1).getTitle().toString()
							.compareToIgnoreCase(((CheckBoxPreference) o2).getTitle().toString());
				}
			});
			for (CheckBoxPreference checkBoxPreference : checkBoxPreferences) {
				category.addPreference(checkBoxPreference);
			}
		}

		void updateExcludedList(String appId, boolean isEnabled) {
			Settings settings = GuiceModule.get().getInstance(Settings.class);
			List<String> quickLaunchApps = settings.getQuickLaunchApps();
			if (isEnabled) {
				this.excludedApps.remove(appId);
				quickLaunchApps.add(appId);
			} else {
				this.excludedApps.add(appId);
			}
			settings.updateExcludedApps(this.excludedApps);
			settings.updateQuickLaunchApps(quickLaunchApps);
		}
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static class GeneralFragment extends PreferenceFragment {
		private static final int NUM_CLICK_TO_UNLOCK_DEBUG = 10;
		private static final String PRIVACY_POLICY_URL = "http://www.trysnowball.com/privacy";
		int debugUnlockCount;
		boolean debugUnlocked;

		public GeneralFragment() {
			this.debugUnlockCount = 0;
			this.debugUnlocked = false;
		}

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.settings);
			findPreference(Settings.KEY_ENABLE_LEFT_HANDED_MODE).setOnPreferenceChangeListener(
					new OnPreferenceChangeListener() {
						@Override
						public boolean onPreferenceChange(Preference preference, Object o) {
							Boolean enableLeftHandedMode = (Boolean) o;
							EventLogger eventLogger = GuiceModule.get().getInstance(EventLoggerManager.class)
									.getEventLogger();
							if (enableLeftHandedMode.booleanValue()) {
								eventLogger.addEvent(EventLogger.SETTING_LEFT_HANDED_MODE_CHANGED,
										EventLogger.PROPERTY_SETTING_LEFT_HANDED_MODE_VALUE, EventLogger.VALUE_ENABLED);
							} else {
								eventLogger
										.addEvent(EventLogger.SETTING_LEFT_HANDED_MODE_CHANGED,
												EventLogger.PROPERTY_SETTING_LEFT_HANDED_MODE_VALUE,
												EventLogger.VALUE_DISABLED);
							}
							return true;
						}
					});
			findPreference(Settings.KEY_ENABLE_HEADSUP_NOTIFICATION).setOnPreferenceChangeListener(
					new OnPreferenceChangeListener() {
						@Override
						public boolean onPreferenceChange(Preference preference, Object o) {
							Boolean enableHeadsUpNotification = (Boolean) o;
							EventLogger eventLogger = GuiceModule.get().getInstance(EventLoggerManager.class)
									.getEventLogger();
							if (enableHeadsUpNotification.booleanValue()) {
								eventLogger.addEvent(EventLogger.SETTING_HEADSUP_NOTIFICATION_CHANGED,
										EventLogger.PROPERTY_HEADSUP_SHADE_NOTIFICATION_VALUE,
										EventLogger.VALUE_ENABLED);
							} else {
								eventLogger.addEvent(EventLogger.SETTING_HEADSUP_NOTIFICATION_CHANGED,
										EventLogger.PROPERTY_HEADSUP_SHADE_NOTIFICATION_VALUE,
										EventLogger.VALUE_DISABLED);
							}
							return true;
						}
					});
			findPreference("notification_access_permission").setOnPreferenceClickListener(
					new OnPreferenceClickListener() {
						@Override
						public boolean onPreferenceClick(Preference preference) {
							Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
							intent.addFlags(1073741824);
							blue.stack.snowball.app.settings.SettingsActivity.GeneralFragment.this.getActivity()
									.startActivity(intent);
							return true;
						}
					});
			Preference buildPreference = findPreference("build");
			buildPreference.setSummary(String.format("Version: %s\nBuild Number: %s", new Object[] { "1.0", "heep" }));
			buildPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference preference) {
					if (!blue.stack.snowball.app.settings.SettingsActivity.GeneralFragment.this.debugUnlocked) {
						blue.stack.snowball.app.settings.SettingsActivity.GeneralFragment generalFragment = blue.stack.snowball.app.settings.SettingsActivity.GeneralFragment.this;
						generalFragment.debugUnlockCount++;
						if (blue.stack.snowball.app.settings.SettingsActivity.GeneralFragment.this.debugUnlockCount >= NUM_CLICK_TO_UNLOCK_DEBUG) {
							blue.stack.snowball.app.settings.SettingsActivity.GeneralFragment.this.unlockDebug();
						}
						blue.stack.snowball.app.settings.SettingsActivity.GeneralFragment.this.showDebugUnlockToast();
					}
					return true;
				}
			});
			if (GuiceModule.get().getInstance(Settings.class).volatileGetShowDebugSettings()) {
				unlockDebug();
			}
			findPreference("privacy_policy").setOnPreferenceClickListener(new OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference preference) {
					Intent i = new Intent("android.intent.action.VIEW");
					i.setData(Uri.parse(PRIVACY_POLICY_URL));
					blue.stack.snowball.app.settings.SettingsActivity.GeneralFragment.this.startActivity(i);
					return true;
				}
			});
		}

		void showDebugUnlockToast() {
			int stepsLeft = 10 - this.debugUnlockCount;
			Toast toast = null;
			if (stepsLeft == 0) {
				toast = Toast.makeText(getActivity(), R.string.debug_unlocked_success, 0);
			} else if (stepsLeft == 1) {
				toast = Toast.makeText(getActivity(), R.string.a_few_more_to_unlock_one, 0);
			} else if (stepsLeft <= 7) {
				toast = Toast.makeText(getActivity(), String.format(
						getActivity().getResources().getString(R.string.a_few_more_to_unlock),
						new Object[] { Integer.valueOf(stepsLeft) }), 0);
			}
			if (toast != null) {
				toast.show();
			}
		}

		void unlockDebug() {
			this.debugUnlocked = true;
			GuiceModule.get().getInstance(Settings.class).volatileUpdateShowDebugSettings(true);
			addDebugCategory();
		}

		void addDebugCategory() {
			addPreferencesFromResource(R.xml.settings_debug);
			CheckBoxPreference remoteLoggingPreference = (CheckBoxPreference) findPreference("enable_remote_logging");
			remoteLoggingPreference.setChecked(false);
			remoteLoggingPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
				@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
				@Override
				public boolean onPreferenceClick(Preference preference) {
					GuiceModule.get().getInstance(RemoteLogger.class)
							.enableRemoteLogging(((TwoStatePreference) preference).isChecked());
					return true;
				}
			});
			findPreference("send_diagnostics").setOnPreferenceClickListener(new OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference preference) {
					GuiceModule.get().getInstance(RemoteLogger.class).sendDiagnostics();
					return true;
				}
			});
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public void onBuildHeaders(List<Header> target) {
		loadHeadersFromResource(R.xml.settings_headers, target);
	}

	@Override
	protected boolean isValidFragment(String fragmentName) {
		return GeneralFragment.class.getName().equals(fragmentName)
				|| EnabledAppsFragment.class.getName().equals(fragmentName)
				|| QuickLaunchPreferenceFragment.class.getName().equals(fragmentName);
	}

	public static Intent getLaunchIntent(Context context) {
		Intent intent = new Intent(context, SettingsActivity.class);
		intent.putExtra(":android:show_fragment", GeneralFragment.class.getName());
		intent.putExtra(":android:no_headers", true);
		return intent;
	}
}

package blue.stack.snowball.app.settings;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import blue.stack.snowball.app.R;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.TypedValue;
import blue.stack.snowball.app.swipe.SwipeTabViewController;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class Settings {
	public static final String KEY_ENABLE_HEADSUP_NOTIFICATION = "enable_headsup_notification";
	public static final String KEY_ENABLE_LEFT_HANDED_MODE = "enable_left_handed_mode";
	public static final String KEY_ENABLE_LOCK_SCREEN = "enable_lock_screen";
	private static final String KEY_ENABLE_SHADE_NOTIFICATION = "enable_shade_notification";
	private static final String KEY_ENABLE_TAB = "enable_tab";
	public static final String KEY_EXCLUDED_APPS = "excluded_apps";
	public static final String KEY_FIRST_INBOX_MESSAGE_ADDED = "first_inbox_entry_added";
	public static final String KEY_FIRST_REVIEW_REQUEST = "last_review_request";
	public static final String KEY_HAS_PULLED_DOWN_SHADE = "has_pulled_down_shade";
	public static final String KEY_HAS_SEEN_MIGRATION_SCREEN = "has_seen_migration_screen";
	public static final String KEY_LOCKSCREEN_WIDGET_Y = "lockscreen_widget_y";
	public static final String KEY_NOTIFICATIONS_IN_DRAWER = "notifications_in_drawer";
	public static final String KEY_OOB_COMPLETE = "oob_complete";
	public static final String KEY_OOB_NEEDS_SHADE_MIGRATION = "oob_needs_shade_migration";
	public static final String KEY_OOB_TUTORIAL_COMPLETE = "oob_tutorial_complete";
	public static final String KEY_QUICK_LAUNCH_APPS = "quick_launch_apps2";
	public static final String KEY_REVIEW_COMPLETE = "review_complete";
	public static final String KEY_REVIEW_REQUEST_COUNT = "review_request_count";
	public static final String KEY_TAB_DISPLAY_STATE = "tab_display_state";
	public static final String KEY_TAB_GRAVITY = "tab_gravity";
	public static final String KEY_TAB_VERTICAL_OFFSET = "tab_vertical_offset";
	@Inject
	private Context context;
	Map<SettingChangeListener, OnSharedPreferenceChangeListener> listenerMap;
	boolean volatileShowDebugSettings;

	class AnonymousClass_2 implements OnSharedPreferenceChangeListener {
		final/* synthetic */SettingChangeListener val$listener;

		AnonymousClass_2(SettingChangeListener settingChangeListener) {
			this.val$listener = settingChangeListener;
		}

		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
			this.val$listener.onSettingChanged(Settings.this, key);
		}
	}

	public static interface SettingChangeListener {
		void onSettingChanged(Settings settings, String str);
	}

	@Inject
	private Settings() {
		this.volatileShowDebugSettings = false;
	}

	@Inject
	private void start() {
		this.listenerMap = new HashMap();
		PreferenceManager.setDefaultValues(this.context, R.xml.settings, false);
		migrateSettings();
	}

	public void stop() {
	}

	private void migrateSettings() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.context);
		if (prefs.getString(KEY_TAB_DISPLAY_STATE, "none").equals("none")) {
			if (prefs.getBoolean(KEY_ENABLE_TAB, true)) {
				setTabDisplayState(SwipeTabViewController.TAB_DISPLAY_STATE_VISIBLE);
			} else {
				setTabDisplayState(SwipeTabViewController.TAB_DISPLAY_STATE_HIDDEN);
			}
		}
		if (prefs.contains(KEY_ENABLE_SHADE_NOTIFICATION)) {
			boolean value = prefs.getBoolean(KEY_ENABLE_SHADE_NOTIFICATION, true);
			Editor editor = prefs.edit();
			editor.remove(KEY_ENABLE_SHADE_NOTIFICATION);
			editor.commit();
			setEnableHeadsUpNotification(value);
		}
	}

	public boolean getOOBComplete() {
		return PreferenceManager.getDefaultSharedPreferences(this.context).getBoolean(KEY_OOB_COMPLETE, false);
	}

	public void updateOOBComplete(boolean oobComplete) {
		Editor editor = PreferenceManager.getDefaultSharedPreferences(this.context).edit();
		editor.putBoolean(KEY_OOB_COMPLETE, oobComplete);
		editor.commit();
	}

	public boolean getOOBTutorialComplete() {
		return PreferenceManager.getDefaultSharedPreferences(this.context).getBoolean(KEY_OOB_TUTORIAL_COMPLETE, false);
	}

	public void updateOOBTutorialComplete(boolean oobTutorialComplete) {
		Editor editor = PreferenceManager.getDefaultSharedPreferences(this.context).edit();
		editor.putBoolean(KEY_OOB_TUTORIAL_COMPLETE, oobTutorialComplete);
		editor.commit();
	}

	public boolean getOOBNeedsShadeMigration() {
		return PreferenceManager.getDefaultSharedPreferences(this.context).getBoolean(KEY_OOB_NEEDS_SHADE_MIGRATION,
				true);
	}

	public void updateOOBNeedsShadeMigration(boolean oobNeedsShadeMigration) {
		Editor editor = PreferenceManager.getDefaultSharedPreferences(this.context).edit();
		editor.putBoolean(KEY_OOB_NEEDS_SHADE_MIGRATION, oobNeedsShadeMigration);
		editor.commit();
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public Set<String> getExcludedApps() {
		Set<String> currentExcludedApps = PreferenceManager.getDefaultSharedPreferences(this.context).getStringSet(
				KEY_EXCLUDED_APPS, null);
		return currentExcludedApps == null ? new HashSet() : new HashSet(currentExcludedApps);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public void updateExcludedApps(Set<String> excludedApps) {
		Editor editor = PreferenceManager.getDefaultSharedPreferences(this.context).edit();
		editor.putStringSet(KEY_EXCLUDED_APPS, excludedApps);
		editor.commit();
	}

	public List<String> getQuickLaunchApps() {
		return (List) new Gson().fromJson(
				PreferenceManager.getDefaultSharedPreferences(this.context).getString(KEY_QUICK_LAUNCH_APPS, null),
				new TypeToken<List<String>>() {
				}.getType());
	}

	public void updateQuickLaunchApps(List<String> quickLaunchApps) {
		String serializedApps = new Gson().toJson(quickLaunchApps);
		Editor editor = PreferenceManager.getDefaultSharedPreferences(this.context).edit();
		editor.putString(KEY_QUICK_LAUNCH_APPS, serializedApps);
		editor.commit();
	}

	public boolean getKeepNotificationsInDrawer() {
		return PreferenceManager.getDefaultSharedPreferences(this.context).getBoolean(KEY_NOTIFICATIONS_IN_DRAWER,
				false);
	}

	public void updateKeepNotificationsInDrawer(boolean keepNotificationsInDrawer) {
		Editor editor = PreferenceManager.getDefaultSharedPreferences(this.context).edit();
		editor.putBoolean(KEY_NOTIFICATIONS_IN_DRAWER, keepNotificationsInDrawer);
		editor.commit();
	}

	public boolean getEnableLockScreen() {
		return PreferenceManager.getDefaultSharedPreferences(this.context).getBoolean(KEY_ENABLE_LOCK_SCREEN, true);
	}

	public void updateEnableLockScreen(boolean enableLockScreen) {
		Editor editor = PreferenceManager.getDefaultSharedPreferences(this.context).edit();
		editor.putBoolean(KEY_ENABLE_LOCK_SCREEN, enableLockScreen);
		editor.commit();
	}

	public void setTabVerticalOffset(int tabVerticalOffset) {
		Editor editor = PreferenceManager.getDefaultSharedPreferences(this.context).edit();
		editor.putInt(KEY_TAB_VERTICAL_OFFSET, tabVerticalOffset);
		editor.commit();
	}

	public int getTabVerticalOffset() {
		return PreferenceManager.getDefaultSharedPreferences(this.context).getInt(KEY_TAB_VERTICAL_OFFSET,
				(int) this.context.getResources().getDimension(R.dimen.tab_vertical_offset));
	}

	public void setTabGravity(int gravity) {
		Editor editor = PreferenceManager.getDefaultSharedPreferences(this.context).edit();
		editor.putInt(KEY_TAB_GRAVITY, gravity);
		editor.commit();
	}

	public int getTabGravity() {
		return PreferenceManager.getDefaultSharedPreferences(this.context).getInt(KEY_TAB_GRAVITY, 5);
	}

	public void setFirstReviewRequest(long firstReviewRequestTimestamp) {
		Editor editor = PreferenceManager.getDefaultSharedPreferences(this.context).edit();
		editor.putLong(KEY_FIRST_REVIEW_REQUEST, firstReviewRequestTimestamp);
		editor.commit();
	}

	public long getFirstReviewRequest() {
		long firstReviewRequestTimestamp = PreferenceManager.getDefaultSharedPreferences(this.context).getLong(
				KEY_FIRST_REVIEW_REQUEST, 0);
		if (firstReviewRequestTimestamp != 0) {
			return firstReviewRequestTimestamp;
		}
		firstReviewRequestTimestamp = System.currentTimeMillis();
		setFirstReviewRequest(firstReviewRequestTimestamp);
		return firstReviewRequestTimestamp;
	}

	public void setReviewRequestedCount(int reviewRequestCount) {
		Editor editor = PreferenceManager.getDefaultSharedPreferences(this.context).edit();
		editor.putInt(KEY_REVIEW_REQUEST_COUNT, reviewRequestCount);
		editor.commit();
	}

	public int getReviewRequestedCount() {
		return PreferenceManager.getDefaultSharedPreferences(this.context).getInt(KEY_REVIEW_REQUEST_COUNT, 0);
	}

	public void setReviewComplete(boolean reviewComplete) {
		Editor editor = PreferenceManager.getDefaultSharedPreferences(this.context).edit();
		editor.putBoolean(KEY_REVIEW_COMPLETE, reviewComplete);
		editor.commit();
	}

	public boolean getReviewComplete() {
		return PreferenceManager.getDefaultSharedPreferences(this.context).getBoolean(KEY_REVIEW_COMPLETE, false);
	}

	public void setFirstInboxMessageAdded(boolean firstInboxMessageAdded) {
		Editor editor = PreferenceManager.getDefaultSharedPreferences(this.context).edit();
		editor.putBoolean(KEY_FIRST_INBOX_MESSAGE_ADDED, firstInboxMessageAdded);
		editor.commit();
	}

	public boolean getFirstInboxMessageAdded() {
		return PreferenceManager.getDefaultSharedPreferences(this.context).getBoolean(KEY_FIRST_INBOX_MESSAGE_ADDED,
				false);
	}

	public void setEnableLeftHandedMode(boolean enableLeftHandedMode) {
		Editor editor = PreferenceManager.getDefaultSharedPreferences(this.context).edit();
		editor.putBoolean(KEY_ENABLE_LEFT_HANDED_MODE, enableLeftHandedMode);
		editor.commit();
	}

	public boolean getEnableLeftHandedMode() {
		return PreferenceManager.getDefaultSharedPreferences(this.context).getBoolean(KEY_ENABLE_LEFT_HANDED_MODE,
				false);
	}

	public void setEnableHeadsUpNotification(boolean enableHeadsUpNotification) {
		Editor editor = PreferenceManager.getDefaultSharedPreferences(this.context).edit();
		editor.putBoolean(KEY_ENABLE_HEADSUP_NOTIFICATION, enableHeadsUpNotification);
		editor.commit();
	}

	public boolean getEnableHeadsUpNotification() {
		return PreferenceManager.getDefaultSharedPreferences(this.context).getBoolean(KEY_ENABLE_HEADSUP_NOTIFICATION,
				true);
	}

	public void setTabDisplayState(String tabDisplayState) {
		Editor editor = PreferenceManager.getDefaultSharedPreferences(this.context).edit();
		editor.putString(KEY_TAB_DISPLAY_STATE, tabDisplayState);
		editor.commit();
	}

	public String getTabDisplayState() {
		return PreferenceManager.getDefaultSharedPreferences(this.context).getString(KEY_TAB_DISPLAY_STATE,
				SwipeTabViewController.TAB_DISPLAY_STATE_VISIBLE);
	}

	public boolean getHasSeenMigrationScreen() {
		return PreferenceManager.getDefaultSharedPreferences(this.context).getBoolean(KEY_HAS_SEEN_MIGRATION_SCREEN,
				false);
	}

	public void setHasSeenMigrationScreen(boolean hasSeenMigrationScreen) {
		Editor editor = PreferenceManager.getDefaultSharedPreferences(this.context).edit();
		editor.putBoolean(KEY_HAS_SEEN_MIGRATION_SCREEN, hasSeenMigrationScreen);
		editor.commit();
	}

	public boolean getHasPulledDownShade() {
		return PreferenceManager.getDefaultSharedPreferences(this.context).getBoolean(KEY_HAS_PULLED_DOWN_SHADE, false);
	}

	public void setHasPulledDownShade(boolean hasPulledDownShade) {
		Editor editor = PreferenceManager.getDefaultSharedPreferences(this.context).edit();
		editor.putBoolean(KEY_HAS_PULLED_DOWN_SHADE, hasPulledDownShade);
		editor.commit();
	}

	public boolean volatileGetShowDebugSettings() {
		return this.volatileShowDebugSettings;
	}

	public void volatileUpdateShowDebugSettings(boolean showDebugSettings) {
		this.volatileShowDebugSettings = showDebugSettings;
	}

	public void registerSettingChangeListener(SettingChangeListener listener) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.context);
		OnSharedPreferenceChangeListener spListener = new AnonymousClass_2(listener);
		prefs.registerOnSharedPreferenceChangeListener(spListener);
		this.listenerMap.put(listener, spListener);
	}

	public float getLockscreenWidgetY() {
		return PreferenceManager.getDefaultSharedPreferences(this.context).getFloat(
				KEY_LOCKSCREEN_WIDGET_Y,
				TypedValue.applyDimension(1,
						this.context.getResources().getDimension(R.dimen.lockscreen_widget_y_offset), this.context
								.getResources().getDisplayMetrics()));
	}

	public void updateLockscreenWidgetY(float y) {
		Editor editor = PreferenceManager.getDefaultSharedPreferences(this.context).edit();
		editor.putFloat(KEY_LOCKSCREEN_WIDGET_Y, y);
		editor.commit();
	}

	public void unregisterSettingChangeListener(SettingChangeListener listener) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.context);
		OnSharedPreferenceChangeListener spListener = this.listenerMap.get(listener);
		if (spListener != null) {
			prefs.unregisterOnSharedPreferenceChangeListener(spListener);
		}
		this.listenerMap.remove(listener);
	}
}

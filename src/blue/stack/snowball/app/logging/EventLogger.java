package blue.stack.snowball.app.logging;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

public class EventLogger {
	public static final String APPS_INSTALLED = "apps_installed";
	public static final String APPS_INSTALLED_COUNT = "apps_installed_count";
	public static final String APP_OPENED = "app_opened";
	public static final String APP_REVIEW_FEEDBACK_GIVEN = "app_review_feedback_given";
	public static final String APP_REVIEW_RATED = "app_review_rated";
	public static final String APP_REVIEW_SKIPPED = "app_review_skipped";
	public static final String FIRST_INBOX_MESSAGE_ADDED = "first_inbox_entry_added";
	public static final String INBOX_MESSAGE_ADDED = "inbox_entry_added";
	public static final String MAINSERVICE_STARTED = "mainservice_started";
	public static final String MAINSERVICE_START_REASON = "mainservice_start_reason";
	public static final String MAIN_ACTIVITY_OPENED = "main_activity_opened";
	public static final String MESSAGE_CONTAINS_URL = "message_contains_url";
	public static final String MIGRATE_ACTIVITY_OPENED = "migrate_activity_opened";
	public static final String MIXPANEL_TOKEN = "d492e9b3bf9e28d96f01c2ef43397ee8";
	public static final String OOB_COMPLETED = "oob_completed";
	public static final String OOB_SPLASH_SHOWN = "oob_splash_shown";
	public static final String OOB_SPLASH_SKIPPED = "oob_splash_skipped";
	public static final String OOB_TUTORIAL_DRAWER_OPENED = "oob_tutorial_drawer_opened";
	public static final String OOB_TUTORIAL_SET_NOTIFICATION_ACCESS_PRESSED = "oob_tutorial_notif_access_pressed";
	public static final String OOB_TUTORIAL_STARTED = "oob_tutorial_started";
	public static final String OOB_TUTORIAL_STARTED_AT_NOTIFICATION_ACCESS = "oob_tutorial_started_at_notif_access";
	public static final String PROPERTY_APP_ID = "app_id";
	public static final String PROPERTY_APP_OPENED_METHOD = "app_opened_method";
	public static final String PROPERTY_HEADSUP_SHADE_NOTIFICATION_VALUE = "shade_headsup_value";
	public static final String PROPERTY_SETTING_LEFT_HANDED_MODE_VALUE = "shade_left_handed_mode_value";
	public static final String PROPERTY_SNOWBALL_OPENED_METHOD = "snowball_opened_method";
	public static final String SETTING_HEADSUP_NOTIFICATION_CHANGED = "setting_shade_headsup_changed";
	public static final String SETTING_LEFT_HANDED_MODE_CHANGED = "setting_left_handed_mode_changed";
	public static final String SNOWBALL_OPENED = "snowball_opened";
	private static final String TAG = "EventLogger";
	public static final String TEST_DEFAULT_SHADE_NOTIFICATION_OFF = "test_default_shade_notification_off";
	public static final String TEST_SHOW_OOB_INBOX_WELCOME_MESSAGE = "test_show_oob_inbox_welcome_message";
	public static final String USER_MIGRATE_SUCCESS = "user_migrate_success";
	public static final String VALUE_APP_OPENED_VIA_HEADSUP = "headsup";
	public static final String VALUE_APP_OPENED_VIA_INBOX = "inbox";
	public static final String VALUE_APP_OPENED_VIA_LOCKSCREEN = "lockscreen";
	public static final String VALUE_APP_OPENED_VIA_NOTIFICATION = "notification";
	public static final String VALUE_APP_OPENED_VIA_QUICKLAUNCH = "quicklaunch";
	public static final String VALUE_DISABLED = "disabled";
	public static final String VALUE_ENABLED = "enabled";
	public static final String VALUE_SNOWBALL_OPENED_VIA_DRAG = "drag";
	public static final String VALUE_SNOWBALL_OPENED_VIA_LOCKSCREEN = "lockscreen";
	public static final String VALUE_SNOWBALL_OPENED_VIA_PERSISTENT_NOTIFICATION = "persistent_notification";
	public static final String VALUE_SNOWBALL_OPENED_VIA_SHADE_NOTIFICATION = "shade_notification";

	// MixpanelAPI mixpanel;

	protected EventLogger(Context context) {
	}

	public void addEvent(String eventName) {
	}

	public void addEvent(String eventName, JSONObject properties) {
		Log.i(eventName, properties.toString());
	}

	public void addEvent(String eventName, Map<String, String> properties) {
		addEvent(eventName, new JSONObject(properties));
	}

	public void addEvent(String eventName, String propertyName, String propertyValue) {
		Map propertyMap = new HashMap();
		propertyMap.put(propertyName, propertyValue);
		addEvent(eventName, propertyMap);
	}

	public void addAppEvent(String eventName, String appId) {
		try {
			addEvent(eventName, new JSONObject("{\"appId\":\"" + appId + "\"}"));
		} catch (JSONException exception) {
			Log.d(TAG, "Couldn't send data to Mixpanel!", exception);
		}
	}

	public void addSuperProperties(JSONObject properties) {
	}

	public void addSuperProperties(Map<String, String> properties) {
		addSuperProperties(new JSONObject(properties));
	}

	public void addSuperProperty(String propertyName, String propertyValue) {
		Map propertyMap = new HashMap();
		propertyMap.put(propertyName, propertyValue);
		addSuperProperties(propertyMap);
	}

	public void addSuperProperty(String propertyName, List<String> array) {
		JSONArray jsonArray = new JSONArray(array);
		Map<String, JSONArray> propertyMap = new HashMap();
		propertyMap.put(propertyName, jsonArray);
		addSuperProperties(new JSONObject(propertyMap));
	}

	public void onDestroy() {
	}

	public void setUDID(String udid) {
	}

	public void setPersonId(String personId) {
	}

	public void setPersonProperty(String key, String value) {
	}

	public void setPersonProperty(String key, List<String> array) {
	}
}

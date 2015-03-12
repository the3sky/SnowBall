package blue.stack.snowball.app.oob;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import blue.stack.snowball.app.MainService;
import blue.stack.snowball.app.logging.EventLogger;
import blue.stack.snowball.app.logging.EventLoggerManager;
import blue.stack.snowball.app.notifications.Notification;
import blue.stack.snowball.app.notifications.NotificationListener;
import blue.stack.snowball.app.notifications.NotificationManager;
import blue.stack.snowball.app.settings.Settings;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class OOBManager implements NotificationListener {
	private static final String TAG = "OOBManager";
	@Inject
	Context context;
	@Inject
	EventLoggerManager eventLoggerManager;
	List<OOBListener> listeners;
	@Inject
	NotificationManager notificationManager;
	@Inject
	Settings settings;

	boolean testsConfigured;

	@Inject
	private OOBManager() {
		this.listeners = new ArrayList();

	}

	@Inject
	private void start() {
		this.testsConfigured = false;
		this.notificationManager.addListener(this);
		// this.testManager.addListener(this);
		// if (this.testManager.areTestsReady().booleanValue()) {
		// configureTests();
		// }
	}

	public void stop() {
		// this.testManager.removeListener(this);
		this.notificationManager.removeListener(this);
	}

	public void addListener(OOBListener listener) {
		this.listeners.add(listener);
	}

	public void removeListener(OOBListener listener) {
		this.listeners.remove(listener);
	}

	public boolean isOOBComplete() {
		return this.settings.getOOBComplete();
	}

	public boolean isOOBTutorialComplete() {
		return this.settings.getOOBTutorialComplete();
	}

	public void setOOBTutorialCompleted() {
		this.settings.updateOOBTutorialComplete(true);
		if (this.notificationManager.didNotificationServiceStart() && !isOOBComplete()) {
			onOOBComplete();
		}
	}

	public boolean getOOBNeedsShadeMigration() {
		return this.settings.getOOBNeedsShadeMigration();
	}

	public void setOOBNeedsShadeMigrationCompleted() {
		this.settings.updateOOBNeedsShadeMigration(false);
	}

	public void resetOOB() {
		this.settings.updateOOBComplete(false);
		this.settings.updateOOBTutorialComplete(false);
	}

	public void startOOB() {
		Intent intent = new Intent(this.context, OOBSplashScreenActivity.class);
		intent.addFlags(268435456);
		this.context.startActivity(intent);
	}

	public void startOOBTutorial() {
		Intent intent = new Intent(this.context, OOBTutorialActivity.class);
		intent.addFlags(268435456);
		this.context.startActivity(intent);
	}

	@Override
	public void onNotificationServiceRunning() {
		if (!isOOBComplete() && isOOBTutorialComplete()) {
			onOOBComplete();
		}
	}

	@Override
	public void onNotificationPosted(Notification notification) {
	}

	void onOOBComplete() {
		setOOBNeedsShadeMigrationCompleted();
		// if
		// (this.testManager.isInTestGroup(TestManager.TEST_SHOW_OOB_INBOX_WELCOME_MESSAGE))
		// {
		// new OOBWelcomeMessageGenerator().generateWelcomeMessage();
		// }
		this.settings.updateOOBComplete(true);
		this.eventLoggerManager.getEventLogger().addEvent(EventLogger.OOB_COMPLETED);
		MainService.startMainService(this.context, MainService.LAUNCH_REASON_OOB_COMPLETE);
		for (OOBListener listener : this.listeners) {
			listener.onOOBComplete();
		}
	}

	// public void onTestsReady() {
	// configureTests();
	// }

	// void configureTests() {
	// if (!this.testsConfigured) {
	// this.testsConfigured = true;
	// if (!isOOBComplete()) {
	// if
	// (this.testManager.isInTestGroup(TestManager.TEST_DEFAULT_SHADE_NOTIFICATION_OFF))
	// {
	// this.eventLoggerManager.getEventLogger().addSuperProperty(EventLogger.TEST_DEFAULT_SHADE_NOTIFICATION_OFF,
	// Boolean.toString(true));
	// this.settings.setEnableHeadsUpNotification(false);
	// }
	// if
	// (this.testManager.isInTestGroup(TestManager.TEST_SHOW_OOB_INBOX_WELCOME_MESSAGE))
	// {
	// this.eventLoggerManager.getEventLogger().addSuperProperty(EventLogger.TEST_SHOW_OOB_INBOX_WELCOME_MESSAGE,
	// Boolean.toString(true));
	// }
	// }
	// }
	// }
}

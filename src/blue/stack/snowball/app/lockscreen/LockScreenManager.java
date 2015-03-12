package blue.stack.snowball.app.lockscreen;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.TargetApi;
import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import blue.stack.snowball.app.apps.App;
import blue.stack.snowball.app.inbox.InboxManager;
import blue.stack.snowball.app.inbox.Message;
import blue.stack.snowball.app.lockscreen.ui.LockScreenWidget;
import blue.stack.snowball.app.lockscreen.ui.LockScreenWidget.LockscreenWidgetListener;
import blue.stack.snowball.app.logging.RemoteDiagnostics;
import blue.stack.snowball.app.logging.RemoteDiagnosticsListener;
import blue.stack.snowball.app.logging.RemoteLogger;
import blue.stack.snowball.app.settings.Settings;
import blue.stack.snowball.app.settings.Settings.SettingChangeListener;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class LockScreenManager implements RemoteDiagnosticsListener, SettingChangeListener {
	private static final int IS_LOCKED_CHECK_DELAY = 2000;
	private static final String TAG = "LockScreenManager";
	private static final int UNLOCK_CHECK_TIMER_INTERVAL = 1000;
	@Inject
	Context context;
	@Inject
	InboxManager inboxManager;
	boolean isOnPhoneCall;
	boolean isPhoneLocked;
	List<LockScreenListener> listeners;
	LockScreenWidget lockscreenWidget;
	int messageIdSinceLastScreenOff;
	LockScreenReceiver receiver;
	@Inject
	RemoteLogger remoteLogger;
	@Inject
	Settings settings;
	Timer unlockCheckTimer;

	class AnonymousClass_4 extends TimerTask {
		final/* synthetic */Handler val$unlockCheckHandler;

		AnonymousClass_4(Handler handler) {
			this.val$unlockCheckHandler = handler;
		}

		@Override
		public void run() {
			this.val$unlockCheckHandler.obtainMessage().sendToTarget();
		}
	}

	class LockScreenPhoneStateListener extends PhoneStateListener {
		LockScreenPhoneStateListener() {
		}

		@Override
		public void onCallStateChanged(int state, String incomingNumber) {
			super.onCallStateChanged(state, incomingNumber);
			switch (state) {
			case 0 /* 0 */:
				Log.d(TAG, "LockState Phone idle");
				LockScreenManager.this.onPhoneCallEnded();
			default:
				Log.d(TAG, "LockState Phone not idle");
				LockScreenManager.this.onPhoneCall();
			}
		}
	}

	public class LockScreenReceiver extends BroadcastReceiver {
		private static final String TAG = "LockScreenReceiver";

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals("android.intent.action.SCREEN_OFF")) {
				LockScreenManager.this.onScreenOff();
			} else if (intent.getAction().equals("android.intent.action.SCREEN_ON")) {
				LockScreenManager.this.onScreenOn();
			} else if (intent.getAction().equals("android.intent.action.USER_PRESENT")) {
				LockScreenManager.this.onUserPresent();
			}
		}
	}

	@Inject
	private LockScreenManager() {
	}

	@Inject
	private void start() {
		this.listeners = new ArrayList();
		this.isPhoneLocked = false;
		this.isOnPhoneCall = false;
		IntentFilter filter = new IntentFilter("android.intent.action.SCREEN_ON");
		filter.addAction("android.intent.action.SCREEN_OFF");
		filter.addAction("android.intent.action.USER_PRESENT");
		this.receiver = new LockScreenReceiver();
		this.context.registerReceiver(this.receiver, filter);
		((TelephonyManager) this.context.getSystemService("phone")).listen(new LockScreenPhoneStateListener(), 32);
		this.lockscreenWidget = null;
		this.messageIdSinceLastScreenOff = this.inboxManager.getLatestMessageId();
		this.remoteLogger.addRemoteDiagnosticsListener(this);
		this.settings.registerSettingChangeListener(this);
		performIsLockedCheck();
	}

	public void stop() {
		this.settings.unregisterSettingChangeListener(this);
		this.remoteLogger.removeRemoteDiagnosticsListener(this);
		this.context.unregisterReceiver(this.receiver);
	}

	public void addListener(LockScreenListener listener) {
		this.listeners.add(listener);
	}

	public void removeListener(LockScreenListener listener) {
		this.listeners.remove(listener);
	}

	@Override
	public void onSettingChanged(Settings settings, String key) {
		if (key.equals(Settings.KEY_ENABLE_LOCK_SCREEN) && !isLockScreenNotificationEnabled()) {
			hideWidget();
		}
	}

	public boolean isPhoneLocked() {
		return this.isPhoneLocked;
	}

	public boolean isScreenOff() {
		return !((PowerManager) this.context.getSystemService("power")).isScreenOn();
	}

	public boolean isOnPhoneCall() {
		TelephonyManager telephonyManager = (TelephonyManager) this.context.getSystemService("phone");
		Log.d(TAG, "isOnPhoneCall: call state = " + telephonyManager.getCallState());
		return telephonyManager.getCallState() != 0;
	}

	public void unlockAndLaunchApp(App app) {
		if (isKeyguardUp() && isKeyguardSecure() && this.lockscreenWidget != null) {
			this.lockscreenWidget.showUnlockToContinue();
		}
		KeyguardDismissActivity.launchApp(this.context, app);
	}

	public void unlockAndLaunchApp(Message message) {
		if (isKeyguardUp() && isKeyguardSecure() && this.lockscreenWidget != null) {
			this.lockscreenWidget.showUnlockToContinue();
		}
		KeyguardDismissActivity.launchAppForMessage(this.context, message);
	}

	public void unlockAndLaunchInbox() {
		if (isKeyguardUp() && isKeyguardSecure() && this.lockscreenWidget != null) {
			this.lockscreenWidget.showUnlockToContinue();
		}
		KeyguardDismissActivity.launchInbox(this.context);
	}

	@Override
	public void onRemoteDiagnosticsRequested(RemoteDiagnostics diagnostics) {
		boolean z;
		boolean z2 = true;
		diagnostics.addDiagnostic(TAG, "hashCode", hashCode());
		diagnostics.addDiagnostic(TAG, "isPhoneLocked", this.isPhoneLocked);
		diagnostics.addDiagnostic(TAG, "isOnPhoneCall", this.isOnPhoneCall);
		diagnostics.addDiagnostic(TAG, "messageIdSinceLastScreenOff", this.messageIdSinceLastScreenOff);
		String str = TAG;
		String str2 = "isLockscreenWidgetNull";
		if (this.lockscreenWidget == null) {
			z = true;
		} else {
			z = false;
		}
		diagnostics.addDiagnostic(str, str2, z);
		String str3 = TAG;
		str = "isUnlockCheckTimerNull";
		if (this.unlockCheckTimer != null) {
			z2 = false;
		}
		diagnostics.addDiagnostic(str3, str, z2);
	}

	public boolean isLockScreenNotificationEnabled() {
		return this.settings.getEnableLockScreen();
	}

	void showWidget() {
		hideWidget();
		if (isLockScreenNotificationEnabled() && !isOnPhoneCall()) {
			this.remoteLogger.d(TAG, "LockScreen widget is being created");
			this.lockscreenWidget = new LockScreenWidget();
			this.lockscreenWidget.start(this.messageIdSinceLastScreenOff, new LockscreenWidgetListener() {
				@Override
				public void onWidgetCleared() {
					LockScreenManager.this.messageIdSinceLastScreenOff = LockScreenManager.this.inboxManager
							.getLatestMessageId();
					if (LockScreenManager.this.lockscreenWidget != null) {
						LockScreenManager.this.lockscreenWidget
								.setShowSinceLastMessageId(LockScreenManager.this.messageIdSinceLastScreenOff);
					}
				}
			});
		}
	}

	void hideWidget() {
		if (this.lockscreenWidget != null) {
			this.remoteLogger.d(TAG, "LockScreen widget is stopped and destroyed");
			this.lockscreenWidget.stop();
			this.lockscreenWidget = null;
		}
	}

	void onScreenOff() {
		this.messageIdSinceLastScreenOff = this.inboxManager.getLatestMessageId();
		this.remoteLogger.d(TAG, "Screen off, is keyguard up = " + isKeyguardUp());
		stopUnlockCheck();
		if (this.lockscreenWidget != null) {
			this.lockscreenWidget.hideUnlockToContinue();
		}
		performIsLockedCheck();
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				LockScreenManager.this.performIsLockedCheck();
			}
		}, 2000);
	}

	void onScreenOn() {
		this.remoteLogger.d(TAG, "Screen on, is keyguard up = " + isKeyguardUp());
		performIsLockedCheck();
		if (isPhoneLocked()) {
			startUnlockCheck();
		}
	}

	void performIsLockedCheck() {
		if (isKeyguardUp() && !isPhoneLocked()) {
			onPhoneLocked();
		}
	}

	void onUserPresent() {
		if (isPhoneLocked()) {
			onPhoneUnlocked();
		}
		stopUnlockCheck();
	}

	void onPhoneLocked() {
		boolean wasPhoneLocked = this.isPhoneLocked;
		this.isPhoneLocked = true;
		if (!isScreenOff()) {
			startUnlockCheck();
		}
		showWidget();
		if (!wasPhoneLocked) {
			for (LockScreenListener listener : this.listeners) {
				listener.onLockScreenStarted(this);
			}
		}
		this.remoteLogger.d(TAG, "Phone is locked");
	}

	void onPhoneUnlocked() {
		boolean wasPhoneLocked = this.isPhoneLocked;
		this.isPhoneLocked = false;
		hideWidget();
		if (wasPhoneLocked) {
			for (LockScreenListener listener : this.listeners) {
				listener.onLockScreenStopped(this);
			}
		}
		this.remoteLogger.d(TAG, "Phone is unlocked");
	}

	void onPhoneCall() {
		if (!this.isOnPhoneCall) {
			if (isPhoneLocked()) {
				hideWidget();
			}
			this.isOnPhoneCall = true;
			for (LockScreenListener listener : this.listeners) {
				listener.onPhoneCall(this);
			}
		}
	}

	void onPhoneCallEnded() {
		if (this.isOnPhoneCall) {
			for (LockScreenListener listener : this.listeners) {
				listener.onPhoneCallEnded(this);
			}
			this.isOnPhoneCall = false;
			if (isPhoneLocked()) {
				showWidget();
			}
		}
	}

	void startUnlockCheck() {
		if (this.unlockCheckTimer == null && isPhoneLocked() && !isScreenOff()) {
			this.remoteLogger.d(TAG, "Starting unlock timer");
			this.unlockCheckTimer = new Timer();
			this.unlockCheckTimer.scheduleAtFixedRate(new AnonymousClass_4(new Handler() {
				@Override
				public void handleMessage(android.os.Message msg) {
					if (!LockScreenManager.this.isKeyguardUp()) {
						LockScreenManager.this.stopUnlockCheck();
						new Handler(Looper.getMainLooper()).post(new Runnable() {
							@Override
							public void run() {
								LockScreenManager.this.onPhoneUnlocked();
							}
						});
					}
				}
			}), 1000, 1000);
		}
	}

	void stopUnlockCheck() {
		if (this.unlockCheckTimer != null) {
			this.remoteLogger.d(TAG, "Stopping unlock timer");
			this.unlockCheckTimer.cancel();
			this.unlockCheckTimer = null;
		}
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public boolean isKeyguardUp() {
		KeyguardManager km = (KeyguardManager) this.context.getSystemService("keyguard");
		return km.inKeyguardRestrictedInputMode() || km.isKeyguardLocked();
	}

	public boolean isKeyguardSecure() {
		return ((KeyguardManager) this.context.getSystemService("keyguard")).isKeyguardSecure();
	}
}

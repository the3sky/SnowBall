package blue.stack.snowball.app.shade;

import blue.stack.snowball.app.R;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Point;
import android.os.Build;
import android.os.Build.VERSION;
import android.provider.Settings.Secure;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import blue.stack.snowball.app.MainService;
import blue.stack.snowball.app.MigrateActivity;
import blue.stack.snowball.app.apps.AppManager;
import blue.stack.snowball.app.core.GuiceModule;
import blue.stack.snowball.app.inbox.AppReviewManager;
import blue.stack.snowball.app.inbox.InboxListener;
import blue.stack.snowball.app.inbox.InboxManager;
import blue.stack.snowball.app.inbox.Message;
import blue.stack.snowball.app.inbox.ReadStateManager;
import blue.stack.snowball.app.inbox.ui.AppReviewView;
import blue.stack.snowball.app.inbox.ui.AppReviewView.OnReviewCompleteListener;
import blue.stack.snowball.app.inbox.ui.InboxViewManager;
import blue.stack.snowball.app.lockscreen.LockScreenListener;
import blue.stack.snowball.app.lockscreen.LockScreenManager;
import blue.stack.snowball.app.logging.EventLogger;
import blue.stack.snowball.app.overlay.OverlayNotificationController;
import blue.stack.snowball.app.settings.Settings;
import blue.stack.snowball.app.settings.Settings.SettingChangeListener;
import blue.stack.snowball.app.shade.ShadeNotificationController.DefaultShadeNotificationListener;
import blue.stack.snowball.app.shade.ui.PanelBar;
import blue.stack.snowball.app.shade.ui.PanelBarListener;
import blue.stack.snowball.app.shade.ui.PanelView;
import blue.stack.snowball.app.shade.ui.QuickLaunchView;
import blue.stack.snowball.app.shade.ui.ShadeHeaderView;
import blue.stack.snowball.app.shade.ui.ShadePanelHolder;
import blue.stack.snowball.app.swipe.ui.YettiTabLayout;
import blue.stack.snowball.app.ui.anim.AnimationCache;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class ShadeViewManager extends InboxViewManager implements SettingChangeListener, InboxListener,
		LockScreenListener {
	static long FAR_AWAY = 0;
	public static final String LOCK_SCREEN_SHOW_NOTIFICATIONS = "lock_screen_show_notifications";
	static final String TAG = "ShadeViewManager";
	private static final long VIBRATE_DURATION = 70;
	@Inject
	AnimationCache animationCache;
	@Inject
	AppManager appManager;
	View appReviewContainerView;
	@Inject
	AppReviewManager appReviewManager;
	BroadcastReceiver broadcastReceiver;
	boolean hasShownWelcomeMessage;
	@Inject
	InboxManager inboxManager;
	@Inject
	LockScreenManager lockScreenManager;
	@Inject
	LockScreenManager lockscreenManager;
	@Inject
	OverlayNotificationController overlayNotificationController;
	@Inject
	ReadStateManager readStateManager;
	ShadePanelHolder shadeInboxBody;
	ShadeHeaderView shadeInboxHeader;
	@Inject
	ShadeNotificationController shadeNotificationController;
	PanelView shadePanelView;
	PanelBar shadeStatusBar;
	private int shadeWindowVisibility;

	class AnonymousClass_1 extends BroadcastReceiver {
		int orientation;
		final/* synthetic */int val$_orientation;

		AnonymousClass_1(int i) {
			this.val$_orientation = i;
			this.orientation = this.val$_orientation;
		}

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals("android.intent.action.CONFIGURATION_CHANGED")) {
				if (context.getResources().getConfiguration().orientation != this.orientation) {
					this.orientation = context.getResources().getConfiguration().orientation;
					ShadeViewManager.this.closeDrawer();
					ShadeViewManager.this.setShadeStatusBarSize(ShadeStatusBarSize.Small);
				}
			} else if (intent.getAction().equals("android.intent.action.SCREEN_OFF")) {
				ShadeViewManager.this.closeDrawer();
			}
		}
	}

	enum ShadeStatusBarSize {
		Small,
		Large
	}

	@Inject
	private ShadeViewManager() {
		this.shadeWindowVisibility = 4;
	}

	@Override
	@Inject
	protected void start() throws Exception {
		super.start();
		createViews();
		if (!this.settings.getOOBNeedsShadeMigration()) {
			addShadeStatusBar();
		}
		migrateInboxBodyWindow();
		setupFooterButtons();
		setupListeners();
	}

	private void setupListeners() {
		this.settings.registerSettingChangeListener(this);
		this.inboxManager.addListener(this);
		this.lockScreenManager.addListener(this);
		IntentFilter filter = new IntentFilter();
		filter.addAction("android.intent.action.SCREEN_OFF");
		filter.addAction("android.intent.action.CONFIGURATION_CHANGED");
		this.broadcastReceiver = new AnonymousClass_1(this.context.getResources().getConfiguration().orientation);
		this.context.registerReceiver(this.broadcastReceiver, filter);
		this.shadeNotificationController.addListener(new DefaultShadeNotificationListener() {
			@Override
			public void onNotificationTouched() {
				ShadeViewManager.this.handleNotificationTouched();
			}
		});
	}

	private void handleNotificationTouched() {
		if (this.settings.getOOBNeedsShadeMigration()) {
			Intent intent = new Intent(this.context, MigrateActivity.class);
			intent.addFlags(268468224);
			this.context.startActivity(intent);
			return;
		}
		GuiceModule.get().getInstance(InboxViewManager.class).openDrawer();
		this.eventLoggerManager.getEventLogger().addEvent(EventLogger.SNOWBALL_OPENED,
				EventLogger.PROPERTY_SNOWBALL_OPENED_METHOD, EventLogger.VALUE_SNOWBALL_OPENED_VIA_SHADE_NOTIFICATION);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	public void addShadeStatusBar() {
		if (this.shadeStatusBar.getParent() == null) {
			LayoutParams params = new LayoutParams(-1, -2, 2010, 16777480, -3);
			WindowManager windowManager = (WindowManager) this.context.getSystemService("window");
			Display display = windowManager.getDefaultDisplay();
			Point displaySize = new Point();
			display.getSize(displaySize);
			params.gravity = 51;
			params.width = (int) this.context.getResources().getDimension(R.dimen.shade_status_touch_bar_width);
			params.height = (int) this.context.getResources().getDimension(R.dimen.shade_status_touch_bar_height);
			if (this.settings.getEnableLeftHandedMode()) {
				params.x = 0;
			} else {
				params.x = displaySize.x - params.width;
			}
			Log.d(TAG, "addShadeStatusBar()");
			windowManager.addView(this.shadeStatusBar, params);
		}
	}

	void removeShadeStatusBar() {
		Log.d(TAG, "remove ShadeStatusBar()");
		WindowManager windowManager = (WindowManager) this.context.getSystemService("window");
		if (this.shadeStatusBar.getParent() != null) {
			windowManager.removeViewImmediate(this.shadeStatusBar);
		}
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	private void setShadeStatusBarSize(ShadeStatusBarSize size) {
		if (this.shadeStatusBar.getParent() != null) {
			WindowManager windowManager = (WindowManager) this.context.getSystemService("window");
			Display display = windowManager.getDefaultDisplay();
			Point displaySize = new Point();
			display.getSize(displaySize);
			LayoutParams params;
			if (size == ShadeStatusBarSize.Small) {
				params = (LayoutParams) this.shadeStatusBar.getLayoutParams();
				params.width = (int) this.context.getResources().getDimension(R.dimen.shade_status_touch_bar_width);
				params.height = (int) this.context.getResources().getDimension(R.dimen.shade_status_touch_bar_height);
				if (this.settings.getEnableLeftHandedMode()) {
					params.x = 0;
				} else {
					params.x = displaySize.x - params.width;
				}
				windowManager.updateViewLayout(this.shadeStatusBar, params);
				return;
			}
			params = (LayoutParams) this.shadeStatusBar.getLayoutParams();
			params.x = 0;
			params.width = displaySize.x;
			params.height = (int) this.context.getResources().getDimension(R.dimen.shade_status_bar_height);
			windowManager.updateViewLayout(this.shadeStatusBar, params);
		}
	}

	@Override
	public void onSettingChanged(Settings settings, String key) {
		if (key.equals(Settings.KEY_ENABLE_LEFT_HANDED_MODE)) {
			setShadeStatusBarSize(ShadeStatusBarSize.Small);
		}
	}

	static {
		FAR_AWAY = 100000;
	}

	void migrateInboxBodyWindow() {
		LayoutParams params = (LayoutParams) this.shadeInboxBody.getLayoutParams();
		WindowManager windowManager;
		if (this.lockScreenManager.isPhoneLocked() && (params == null || params.type != 2010)) {
			windowManager = (WindowManager) this.context.getSystemService("window");
			if (this.shadeInboxBody.getParent() != null) {
				windowManager.removeViewImmediate(this.shadeInboxBody);
			}
			addInboxBodyToWindow(2010);
		} else if (!this.lockScreenManager.isPhoneLocked()) {
			if (params == null || params.type != 2007) {
				windowManager = (WindowManager) this.context.getSystemService("window");
				if (this.shadeInboxBody.getParent() != null) {
					windowManager.removeViewImmediate(this.shadeInboxBody);
				}
				addInboxBodyToWindow(2007);
			}
		}
	}

	void setShadeWindowVisibility(int visible) {
		LayoutParams params;
		WindowManager windowManager = (WindowManager) this.context.getSystemService("window");
		this.shadeWindowVisibility = visible;
		migrateInboxBodyWindow();
		this.shadeInboxBody.setVisibility(visible);
		if (this.shadeInboxBody.getParent() != null) {
			params = (LayoutParams) this.shadeInboxBody.getLayoutParams();
			if (visible == 0) {
				params.x = 0;
				params.dimAmount = 0.7f;
				params.flags &= -25;
				params.flags |= 2;
			} else {
				params.x = (int) FAR_AWAY;
				params.dimAmount = 0.0f;
				params.flags |= 24;
				params.flags &= -3;
			}
			windowManager.updateViewLayout(this.shadeInboxBody, params);
		}
		if (visible == 0) {
			if (this.shadeInboxHeader.getParent() == null) {
				params = new LayoutParams(-2, -2, 2010, 16843544, -3);
				params.gravity = 51;
				params.width = -1;
				params.height = -2;
				params.x = 0;
				params.y = 0;
				windowManager.addView(this.shadeInboxHeader, params);
			}
		} else if (this.shadeInboxHeader.getParent() != null) {
			windowManager.removeViewImmediate(this.shadeInboxHeader);
		}
	}

	void addInboxBodyToWindow(int windowType) {
		if (windowType == 2010 || windowType == 2007) {
			Log.d(TAG, "add Tray()");
			WindowManager windowManager = (WindowManager) this.context.getSystemService("window");
			LayoutParams params = new LayoutParams(-2, -2, windowType, 16843544, -3);
			params.gravity = 51;
			params.width = -1;
			params.height = -1;
			params.x = (int) FAR_AWAY;
			params.y = 0;
			windowManager.addView(this.shadeInboxBody, params);
			this.shadeInboxBody.setVisibility(4);
			return;
		}
		Log.d(TAG, "Unrecognized Window Type: addInboxBodyToWindow()");
	}

	void removePanels() {
		Log.d(TAG, "removePanels()");
		WindowManager windowManager = (WindowManager) this.context.getSystemService("window");
		if (this.shadeInboxBody.getParent() != null) {
			windowManager.removeViewImmediate(this.shadeInboxBody);
		}
		if (this.shadeInboxHeader.getParent() != null) {
			windowManager.removeViewImmediate(this.shadeInboxHeader);
		}
	}

	void createViews() {
		LayoutInflater inflater = (LayoutInflater) this.context.getSystemService("layout_inflater");
		this.shadeInboxBody = (ShadePanelHolder) inflater.inflate(R.layout.shade_panel_holder, null);
		this.shadeInboxHeader = (ShadeHeaderView) inflater.inflate(R.layout.shade_inbox_header, null);
		this.shadePanelView = (PanelView) inflater.inflate(R.layout.shade_panel_view, null);
		this.shadeInboxBody.addView(this.shadePanelView);
		this.shadeInboxBody.setHeaderView(this.shadeInboxHeader);
		((LinearLayout) this.shadePanelView.findViewById(R.id.inbox_view)).addView(this.inboxDrawer);
		this.shadePanelView.setHandleView(this.shadePanelView.findViewById(R.id.handle));
		this.shadeStatusBar = new PanelBar(this.context, null);
		this.shadeStatusBar.setPanelHolder(this.shadeInboxBody);
		this.shadeStatusBar.setListener(new PanelBarListener() {
			public boolean openedOnce;

			{
				this.openedOnce = false;
			}

			@Override
			public void onPanelPeeked() {
				ShadeViewManager.this.eventLoggerManager.getEventLogger().addEvent(EventLogger.SNOWBALL_OPENED,
						EventLogger.PROPERTY_SNOWBALL_OPENED_METHOD, EventLogger.VALUE_SNOWBALL_OPENED_VIA_DRAG);
			}

			@Override
			public void onBeginOpeningPanel() {
				Log.d(TAG, "onBeginOpeningPanel()");
				displaySettingsButton();
				displayClearAllButton();
				displayRateReviewDialog();
				ShadeViewManager.this.inboxDrawer.setTouchable(false);
				ShadeViewManager.this.inboxViewController.onDrawerWillOpen();
				ShadeViewManager.this.shadeNotificationController.closePopup();
				ShadeViewManager.this.setShadeWindowVisibility(0);
				displayWelcomeMessage();
				displayUnlockMessage();
				((QuickLaunchView) ShadeViewManager.this.inboxDrawer.findViewById(R.id.quick_launch))
						.checkDefaultSMSAppIcon();
				ShadeViewManager.this.setShadeStatusBarSize(ShadeStatusBarSize.Large);
			}

			private void displayUnlockMessage() {
				if (ShadeViewManager.this.lockScreenManager.isPhoneLocked()
						&& ShadeViewManager.this.lockScreenManager.isKeyguardSecure()
						&& !ShadeViewManager.this.showNotification()) {
					ShadeViewManager.this.inboxViewController.showUnlockMessage();
				}
			}

			private void displayRateReviewDialog() {
				if (ShadeViewManager.this.appReviewManager.shouldAskForReview()) {
					ShadeViewManager.this.appReviewContainerView.setVisibility(0);
				} else {
					ShadeViewManager.this.appReviewContainerView.setVisibility(8);
				}
			}

			private void displayWelcomeMessage() {
				if (!(ShadeViewManager.this.startupReason == null
						|| !ShadeViewManager.this.startupReason.equals(MainService.LAUNCH_REASON_OOB_COMPLETE) || ShadeViewManager.this.hasShownWelcomeMessage)) {
					ShadeViewManager.this.inboxViewController.showWelcomeDialog();
				}
				ShadeViewManager.this.hasShownWelcomeMessage = true;
			}

			private void displaySettingsButton() {
				View settingsButton = ShadeViewManager.this.shadeInboxBody.findViewById(R.id.settings_button);
				if (ShadeViewManager.this.lockScreenManager.isPhoneLocked()) {
					settingsButton.setVisibility(4);
				} else {
					settingsButton.setVisibility(0);
				}
			}

			private void displayClearAllButton() {
				View clearAllButton = ShadeViewManager.this.shadeInboxBody.findViewById(R.id.clear_all);
				clearAllButton.setVisibility(0);
				if (VERSION.SDK_INT > 19 && ShadeViewManager.this.lockScreenManager.isPhoneLocked()
						&& ShadeViewManager.this.lockScreenManager.isKeyguardSecure()
						&& !ShadeViewManager.this.showNotification()) {
					clearAllButton.setVisibility(8);
				}
			}

			@Override
			public void onPanelOpened() {
				Log.d(TAG, "onPanelOpened()");
				ShadeViewManager.this.inboxDrawer.setTouchable(true);
				ShadeViewManager.this.shadeStatusBar.setTouchable(false);
				ShadeViewManager.this.fireOnDrawerOpened();
			}

			@Override
			public void onPanelClosed() {
				Log.d(TAG, "onPanelClosed()");
				ShadeViewManager.this.inboxDrawer.setTouchable(false);
				ShadeViewManager.this.shadeStatusBar.setTouchable(true);
				ShadeViewManager.this.setShadeWindowVisibility(4);
				ShadeViewManager.this.fireOnDrawerClosed();
				ShadeViewManager.this.setShadeStatusBarSize(ShadeStatusBarSize.Small);
			}
		});
		this.appReviewContainerView = this.inboxDrawer.findViewById(R.id.app_review_container);
		((AppReviewView) this.inboxDrawer.findViewById(R.id.app_review_view))
				.setOnReviewCompleteListener(new OnReviewCompleteListener() {
					@Override
					public void onRateApp() {
						ShadeViewManager.this.appReviewManager.rateApp();
						ShadeViewManager.this.closeDrawer();
						ShadeViewManager.this.appReviewContainerView.setVisibility(8);
					}

					@Override
					public void onGiveFeedback() {
						ShadeViewManager.this.appReviewManager.giveFeedback();
						ShadeViewManager.this.closeDrawer();
						ShadeViewManager.this.appReviewContainerView.setVisibility(8);
					}

					@Override
					public void onReviewSkipped() {
						ShadeViewManager.this.appReviewManager.reviewSkipped();
						ShadeViewManager.this.appReviewContainerView.setVisibility(8);
					}
				});
	}

	@Override
	public void openDrawer() {
		if (this.oobManager.getOOBNeedsShadeMigration()) {
			Intent i = new Intent(this.context, MigrateActivity.class);
			i.addFlags(268435456);
			this.context.startActivity(i);
			return;
		}
		this.shadePanelView.expand();
	}

	@Override
	public void closeDrawer() {
		this.shadePanelView.collapse();
	}

	@Override
	public boolean isInboxViewOpen() {
		return !this.shadePanelView.isFullyCollapsed();
	}

	@Override
	public int getDrawerLayout() {
		return R.layout.shade_inbox_layout;
	}

	@Override
	public void stop() {
		WindowManager windowManager = (WindowManager) this.context.getSystemService("window");
		if (this.shadeStatusBar.getParent() != null) {
			windowManager.removeViewImmediate(this.shadeStatusBar);
		}
		removePanels();
		this.inboxManager.removeListener(this);
		this.lockScreenManager.removeListener(this);
		this.settings.unregisterSettingChangeListener(this);
		this.shadeNotificationController.stop();
		this.overlayNotificationController.stop();
		if (this.broadcastReceiver != null) {
			this.context.unregisterReceiver(this.broadcastReceiver);
			this.broadcastReceiver = null;
		}
	}

	@Override
	public void cacheAnimations() {
		YettiTabLayout.CacheAnimations(this.animationCache);
	}

	@Override
	public void onInboxUpdated() {
	}

	@Override
	public void onInboxMessageAdded(Message message) {
		if (this.settings.getOOBNeedsShadeMigration()) {
			this.shadeNotificationController.openMigrationPopup();
			return;
		}
		if (shouldPerformHeadsUpPopup(message)) {
			this.overlayNotificationController.performHeadsUpPopup(message);
		}
		this.shadeInboxBody.refreshFooterPosition();
	}

	@Override
	public void onInboxCleared() {
		this.shadeInboxBody.refreshFooterPosition();
	}

	boolean shouldPerformHeadsUpPopup(Message message) {
		return true;
		// return (!this.settings.getEnableHeadsUpNotification() ||
		// this.lockScreenManager.isPhoneLocked() || isTabVisibleFieldSet(
		// eVisibilityReason.FullScreenMode).booleanValue()) ? false : true;
	}

	@Override
	public void onLockScreenStarted(LockScreenManager manager) {
		migrateInboxBodyWindow();
		if (VERSION.SDK_INT <= 19 && manager.isKeyguardSecure()) {
			removeShadeStatusBar();
		}
		if (this.lockScreenManager.isKeyguardSecure() && !showNotification()) {
			this.inboxViewController.showUnlockMessage();
		}
	}

	@TargetApi(21)
	private boolean showNotification() {
		return Secure.getInt(this.context.getContentResolver(), LOCK_SCREEN_SHOW_NOTIFICATIONS, 0) != 0;
	}

	@Override
	public void onLockScreenStopped(LockScreenManager manager) {
		if (!this.settings.getOOBNeedsShadeMigration()) {
			addShadeStatusBar();
		}
		migrateInboxBodyWindow();
		setShadeWindowVisibility(this.shadeWindowVisibility);
	}

	@Override
	public void onPhoneCall(LockScreenManager manager) {
		closeDrawer();
	}

	@Override
	public void onPhoneCallEnded(LockScreenManager manager) {
	}

	protected void setupFooterButtons() {
		((ImageButton) this.shadeInboxBody.findViewById(R.id.settings_button))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						ShadeViewManager.this.onSettingPressed();
					}
				});
		((ImageButton) this.shadeInboxBody.findViewById(R.id.clear_all)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ShadeViewManager.this.closeDrawer();
				ShadeViewManager.this.onClearAllSelected();
			}
		});
	}
}

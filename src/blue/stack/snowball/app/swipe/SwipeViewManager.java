package blue.stack.snowball.app.swipe;

import blue.stack.snowball.app.R;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Point;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager.LayoutParams;
import android.widget.RelativeLayout;
import blue.stack.snowball.app.MainService;
import blue.stack.snowball.app.inbox.ui.InboxViewManager;
import blue.stack.snowball.app.lockscreen.LockScreenListener;
import blue.stack.snowball.app.lockscreen.LockScreenManager;
import blue.stack.snowball.app.logging.EventLoggerManager;
import blue.stack.snowball.app.swipe.SwipeTabViewController.OnTouchHandler;
import blue.stack.snowball.app.swipe.SwipeTabViewController.eTabSizeState;
import blue.stack.snowball.app.swipe.ui.QuickLaunchView;
import blue.stack.snowball.app.swipe.ui.YettiTabLayout;
import blue.stack.snowball.app.tools.SwoopBroadcastReceiver;
import blue.stack.snowball.app.ui.anim.AnimationCache;
import blue.stack.snowball.app.ui.anim.ElasticInInterpolator;
import blue.stack.snowball.app.ui.anim.ElasticOutInterpolator;

import com.android.volley.DefaultRetryPolicy;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mobeta.android.dslv.DragSortController;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
@Singleton
public class SwipeViewManager extends InboxViewManager {
	public static final String CMD_CLOSE_DRAWER = "close_drawer";
	public static final String CMD_OPEN_DRAWER = "open_drawer";
	public static final String CMD_TRASH_TAB = "trash_tab";
	private static final int DRAWER_CLOSE_DRAWER_ANIMATION_TIME = 200;
	private static final int DRAWER_CLOSE_QUICK_LAUNCH_ANIMATION_TIME = 50;
	private static final int DRAWER_OPEN_DRAWER_ANIMATION_TIME = 200;
	private static final int DRAWER_OPEN_QUICK_LAUNCH_ANIMATION_TIME = 100;
	public static final String INTENT_ACTION_CMDS = "blue.stack.snowball.app.swipemanager.commands";
	private static final String TAG = "SwipeViewManager";
	Boolean addedToWindow;
	@Inject
	private AnimationCache animationCache;
	eSwipeWindowState currentSwipeState;
	@Inject
	private EventLoggerManager eventLoggerManager;
	LockScreenListener lockScreenListener;
	@Inject
	private LockScreenManager lockscreenManager;
	private Boolean openedOnce;
	private SwipeManagerReceiver receiver;
	int screenHeight;
	int screenWidth;
	@Inject
	SwipeTabViewController swipeTabViewController;
	private Runnable switchStatusRunnable;

	static/* synthetic */class AnonymousClass_4 {
		static final/* synthetic */int[] $SwitchMap$com$squanda$swoop$app$swipe$SwipeViewManager$eSwipeWindowState;

		static {
			$SwitchMap$com$squanda$swoop$app$swipe$SwipeViewManager$eSwipeWindowState = new int[eSwipeWindowState
					.values().length];
			try {
				$SwitchMap$com$squanda$swoop$app$swipe$SwipeViewManager$eSwipeWindowState[eSwipeWindowState.InterpolatingToHidden
						.ordinal()] = 1;
			} catch (NoSuchFieldError e) {
			}
			try {
				$SwitchMap$com$squanda$swoop$app$swipe$SwipeViewManager$eSwipeWindowState[eSwipeWindowState.InterpolatingToFullScreen
						.ordinal()] = 2;
			} catch (NoSuchFieldError e2) {
			}
			try {
				$SwitchMap$com$squanda$swoop$app$swipe$SwipeViewManager$eSwipeWindowState[eSwipeWindowState.Hidden
						.ordinal()] = 3;
			} catch (NoSuchFieldError e3) {
			}
			try {
				$SwitchMap$com$squanda$swoop$app$swipe$SwipeViewManager$eSwipeWindowState[eSwipeWindowState.FullScreen
						.ordinal()] = 4;
			} catch (NoSuchFieldError e4) {
			}
		}
	}

	class SwipeManagerReceiver extends BroadcastReceiver {
		SwipeManagerReceiver() {
		}

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals("android.intent.action.CONFIGURATION_CHANGED")) {
				if (context.getResources().getConfiguration().orientation == 2) {
					SwipeViewManager.this.closeDrawer();
					SwipeViewManager.this.setTabVisibility(Boolean.valueOf(false), eVisibilityReason.Orientation);
					return;
				}
				SwipeViewManager.this.setTabVisibility(Boolean.valueOf(true), eVisibilityReason.Orientation);
			} else if (intent.getAction().equals(INTENT_ACTION_CMDS)) {
				String type = intent.getStringExtra(SwoopBroadcastReceiver.TYPE_CMD);
				if (type.equals(CMD_OPEN_DRAWER)) {
					SwipeViewManager.this.openDrawer();
				} else if (type.equals(CMD_CLOSE_DRAWER)) {
					SwipeViewManager.this.closeDrawer();
				} else if (type.equals(CMD_TRASH_TAB)) {
					SwipeViewManager.this.setTabVisibility(Boolean.valueOf(false), eVisibilityReason.Docked);
				}
			}
		}
	}

	enum eSwipeWindowState {
		Hidden,
		InterpolatingToFullScreen,
		FullScreen,
		InterpolatingToHidden
	}

	@Inject
	private SwipeViewManager() {
		this.addedToWindow = Boolean.valueOf(false);
		this.openedOnce = Boolean.valueOf(false);
		this.currentSwipeState = eSwipeWindowState.Hidden;
	}

	@Override
	@Inject
	protected void start() throws Exception {
		super.start();
		Log.d(TAG, "start");
		constructRunnableSwitch();
		createSwipeLayoutView();
		createSwipeTabView();
		createLockscreenListener();
		this.receiver = new SwipeManagerReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(INTENT_ACTION_CMDS);
		filter.addAction("android.intent.action.CONFIGURATION_CHANGED");
		this.context.registerReceiver(this.receiver, filter);
	}

	@Override
	public void setTabVisibility(Boolean isVisible, eVisibilityReason reason) {
		super.setTabVisibility(isVisible, reason);
		this.swipeTabViewController.refreshTabViewState();
	}

	private void createLockscreenListener() {
		this.lockScreenListener = new LockScreenListener() {
			@Override
			public void onLockScreenStarted(LockScreenManager manager) {
				SwipeViewManager.this.closeDrawerNoCallback();
				SwipeViewManager.this.setTabVisibility(Boolean.valueOf(false), eVisibilityReason.LockScreen);
			}

			@Override
			public void onLockScreenStopped(LockScreenManager manager) {
				SwipeViewManager.this.setTabVisibility(Boolean.valueOf(true), eVisibilityReason.FullScreenMode);
				SwipeViewManager.this.setTabVisibility(Boolean.valueOf(true), eVisibilityReason.Orientation);
				SwipeViewManager.this.setTabVisibility(Boolean.valueOf(true), eVisibilityReason.LockScreen);
			}

			@Override
			public void onPhoneCall(LockScreenManager manager) {
				SwipeViewManager.this.closeDrawerNoCallback();
				SwipeViewManager.this.setTabVisibility(Boolean.valueOf(false), eVisibilityReason.LockScreen);
			}

			@Override
			public void onPhoneCallEnded(LockScreenManager manager) {
				if (!SwipeViewManager.this.lockscreenManager.isPhoneLocked()) {
					SwipeViewManager.this.setTabVisibility(Boolean.valueOf(true), eVisibilityReason.LockScreen);
				}
			}
		};
		this.lockscreenManager.addListener(this.lockScreenListener);
	}

	@Override
	public void stop() {
		super.stop();
		this.lockscreenManager.removeListener(this.lockScreenListener);
		this.context.unregisterReceiver(this.receiver);
		if (this.inboxDrawer != null) {
			removeSwipeLayoutView();
		}
		if (this.swipeTabViewController != null) {
			this.swipeTabViewController.stop();
		}
	}

	@Override
	public void cacheAnimations() {
		YettiTabLayout.CacheAnimations(this.animationCache);
	}

	@Override
	public boolean isInboxViewOpen() {
		return this.currentSwipeState == eSwipeWindowState.FullScreen
				|| this.currentSwipeState == eSwipeWindowState.InterpolatingToFullScreen;
	}

	@Override
	public int getDrawerLayout() {
		return R.layout.swipe_inbox_layout;
	}

	@Override
	public void openDrawer() {
		setCurrentSwipeState(eSwipeWindowState.InterpolatingToFullScreen);
	}

	@Override
	public void closeDrawer() {
		setCurrentSwipeState(eSwipeWindowState.InterpolatingToHidden);
	}

	void closeDrawerNoCallback() {
		setCurrentSwipeState(eSwipeWindowState.InterpolatingToHidden);
	}

	private void createSwipeTabView() throws Exception {
		SwipeViewManager swipeViewManager = this;
		this.swipeTabViewController.start();
		if (this.context.getResources().getConfiguration().orientation == 2) {
			setTabVisibility(Boolean.valueOf(false), eVisibilityReason.Orientation);
		}
		this.swipeTabViewController.setTouchHandler(new OnTouchHandler() {
			@Override
			public void OnTapTab() {
				SwipeViewManager.this.openDrawer();
			}
		});
	}

	private void createSwipeLayoutView() {
		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		Point size2 = new Point();
		display.getCurrentSizeRange(size, size2);
		this.screenWidth = size.x;
		this.screenHeight = size2.y;
		this.inboxViewController.getView().setX(0.0f);
	}

	private void addSwipeLayoutView() {
		if (!this.addedToWindow.booleanValue()) {
			LayoutParams params = new LayoutParams(-2, -2, 2007, 16843010, -3);
			params.gravity = 53;
			params.dimAmount = 0.65f;
			params.height = this.screenHeight;
			params.width = this.screenWidth;
			params.y = getStatusBarHeight();
			getWindowManager().addView(this.inboxDrawer, params);
			this.addedToWindow = Boolean.valueOf(true);
		}
	}

	private void removeSwipeLayoutView() {
		if (this.addedToWindow.booleanValue()) {
			getWindowManager().removeView(this.inboxDrawer);
			this.addedToWindow = Boolean.valueOf(false);
		}
	}

	private void setCurrentSwipeState(eSwipeWindowState newSwipeState) {
		if (newSwipeState != this.currentSwipeState) {
			switch (AnonymousClass_4.$SwitchMap$com$squanda$swoop$app$swipe$SwipeViewManager$eSwipeWindowState[newSwipeState
					.ordinal()]) {
			case DragSortController.ON_DRAG /* 1 */:
				if (this.currentSwipeState == eSwipeWindowState.Hidden) {
					return;
				}
				if (this.currentSwipeState == eSwipeWindowState.InterpolatingToHidden) {
					return;
				}
				break;
			case DragSortController.ON_LONG_PRESS /* 2 */:
				if (this.currentSwipeState == eSwipeWindowState.InterpolatingToFullScreen) {
					return;
				}
				if (this.currentSwipeState == eSwipeWindowState.FullScreen) {
					return;
				}
				break;
			}
			Log.d("Swipe", "State Changed: " + this.currentSwipeState.toString());
			this.currentSwipeState = newSwipeState;
			this.switchStatusRunnable.run();
		}
	}

	private void constructRunnableSwitch() {
		this.switchStatusRunnable = new Runnable() {

			class AnonymousClass_3 implements Runnable {
				final/* synthetic */QuickLaunchView val$quickLaunchView;

				AnonymousClass_3(QuickLaunchView quickLaunchView) {
					this.val$quickLaunchView = quickLaunchView;
				}

				@Override
				public void run() {
					this.val$quickLaunchView.animateIn(DRAWER_OPEN_QUICK_LAUNCH_ANIMATION_TIME);
					SwipeViewManager.this.setCurrentSwipeState(eSwipeWindowState.FullScreen);
					SwipeViewManager.this.openedOnce = Boolean.valueOf(true);
				}
			}

			@Override
			public void run() {
				float tabHeight = SwipeViewManager.this.context.getResources().getDimension(R.dimen.tab_height);
				RelativeLayout sideLayout;
				float x;
				switch (AnonymousClass_4.$SwitchMap$com$squanda$swoop$app$swipe$SwipeViewManager$eSwipeWindowState[SwipeViewManager.this.currentSwipeState
						.ordinal()]) {
				case DragSortController.ON_DRAG /* 1 */:
					sideLayout = (RelativeLayout) SwipeViewManager.this.inboxDrawer
							.findViewById(R.id.swipe_side_layout);
					((QuickLaunchView) SwipeViewManager.this.inboxDrawer.findViewById(R.id.quick_launch))
							.animateOut(DRAWER_CLOSE_QUICK_LAUNCH_ANIMATION_TIME);
					x = (SwipeViewManager.this.screenWidth) / 2.0f;
					if (SwipeViewManager.this.swipeTabViewController.getTabGravity() == 3) {
						x = -x;
					}
					sideLayout
							.animate()
							.translationX(x)
							.translationY(
									((SwipeViewManager.this.swipeTabViewController.getTabVerticalOffset()) - ((SwipeViewManager.this.screenHeight) / 2.0f))
											+ tabHeight).scaleY(0.0f).scaleX(0.0f).setDuration(200).setStartDelay(50)
							.setInterpolator(new ElasticInInterpolator(1.2f)).withEndAction(new Runnable() {
								@Override
								public void run() {
									SwipeViewManager.this.setCurrentSwipeState(eSwipeWindowState.Hidden);
								}
							});
				case DragSortController.ON_LONG_PRESS /* 2 */:
					SwipeViewManager.this.addSwipeLayoutView();
					SwipeViewManager.this.inboxViewController.onDrawerWillOpen();
					new Handler().postDelayed(new Runnable() {
						@Override
						public void run() {
							SwipeViewManager.this.setTabVisibility(Boolean.valueOf(true), eVisibilityReason.Docked);
							SwipeViewManager.this.setTabVisibility(Boolean.valueOf(false),
									eVisibilityReason.InboxIsClosed);
						}
					}, 30);
					sideLayout = (RelativeLayout) SwipeViewManager.this.inboxDrawer
							.findViewById(R.id.swipe_side_layout);
					sideLayout.setScaleX(0.0f);
					sideLayout.setScaleY(0.0f);
					QuickLaunchView quickLaunchView = (QuickLaunchView) SwipeViewManager.this.inboxDrawer
							.findViewById(R.id.quick_launch);
					quickLaunchView.hide();
					if (SwipeViewManager.this.swipeTabViewController.getTabGravity() == 3) {
						quickLaunchView.setExitOnRight(false);
					} else {
						quickLaunchView.setExitOnRight(true);
					}
					x = (SwipeViewManager.this.screenWidth) / 2.0f;
					if (SwipeViewManager.this.swipeTabViewController.getTabGravity() == 3) {
						x = -x;
					}
					float y = ((SwipeViewManager.this.swipeTabViewController.getTabVerticalOffset()) - ((SwipeViewManager.this.screenHeight) / 2.0f))
							+ (1.5f * tabHeight);
					sideLayout.setX(x);
					sideLayout.setY(y);
					sideLayout.animate().translationX(0.0f).translationY(0.0f)
							.scaleY(DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
							.scaleX(DefaultRetryPolicy.DEFAULT_BACKOFF_MULT).setDuration(200)
							.setInterpolator(new ElasticOutInterpolator(1.2f))
							.withEndAction(new AnonymousClass_3(quickLaunchView));
					if (SwipeViewManager.this.startupReason != null
							&& SwipeViewManager.this.startupReason.equals(MainService.LAUNCH_REASON_OOB_COMPLETE)
							&& !SwipeViewManager.this.openedOnce.booleanValue()) {
						SwipeViewManager.this.inboxViewController.showWelcomeDialog();
					}
				case 3 /* 3 */:
					SwipeViewManager.this.swipeTabViewController.setTabSizeState(eTabSizeState.Minimized,
							Boolean.valueOf(false), null);
					SwipeViewManager.this.setTabVisibility(Boolean.valueOf(true), eVisibilityReason.InboxIsClosed);
					new Handler().postDelayed(new Runnable() {
						@Override
						public void run() {
							SwipeViewManager.this.removeSwipeLayoutView();
							SwipeViewManager.this.fireOnDrawerClosed();
						}
					}, 30);
				case 4 /* 4 */:
					SwipeViewManager.this.fireOnDrawerOpened();
				default:
				}
			}
		};
	}

	public int getStatusBarHeight() {
		int resourceId = this.context.getResources().getIdentifier("status_bar_height", "dimen", "android");
		return resourceId > 0 ? this.context.getResources().getDimensionPixelSize(resourceId) : 0;
	}
}

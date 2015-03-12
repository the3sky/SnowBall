package blue.stack.snowball.app.swipe;

import java.lang.reflect.Field;
import java.util.Timer;
import java.util.TimerTask;

import blue.stack.snowball.app.R;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Point;
import android.os.Build;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.RelativeLayout;
import blue.stack.snowball.app.inbox.ReadStateManager;
import blue.stack.snowball.app.inbox.ui.InboxViewManager;
import blue.stack.snowball.app.inbox.ui.InboxViewManager.eVisibilityReason;
import blue.stack.snowball.app.logging.EventLoggerManager;
import blue.stack.snowball.app.settings.Settings;
import blue.stack.snowball.app.settings.Settings.SettingChangeListener;
import blue.stack.snowball.app.swipe.SwipeTabViewRelocationController.SwipeTabRelocationListener;
import blue.stack.snowball.app.swipe.ui.YettiTabLayout;
import blue.stack.snowball.app.tools.SwoopBroadcastReceiver;
import blue.stack.snowball.app.tools.UIUtilities;
import blue.stack.snowball.app.ui.anim.AnimationPNGSequence.IAnimationListener;
import blue.stack.snowball.app.ui.anim.BackOutInterpolator;

import com.android.volley.DefaultRetryPolicy;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mobeta.android.dslv.DragSortController;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
@Singleton
public class SwipeTabViewController implements SettingChangeListener {
	public static long AUTO_HIDE_TIMER = 0;
	public static long CHANGE_SIZE_ANIM_DURATION = 0;
	public static final String CMD_SET_TAB_GRAVITY = "set_tab_gravity";
	public static final String CMD_SET_TAB_VERTICAL = "set_tab_vertical";
	public static long DRAG_DISTANCE_OPEN = 0;
	public static final String INTENT_ACTION_CMDS = "blue.stack.snowball.app.swipetabmanager.commands";
	public static final String TAB_DISPLAY_STATE_HIDDEN = "hidden";
	public static final String TAB_DISPLAY_STATE_MESSAGE_ONLY = "message_only";
	public static final String TAB_DISPLAY_STATE_VISIBLE = "visible";
	private static final String TAG = "SwipeTabViewController";
	private TimerTask autoHideTask;
	private Timer autoHideTimer;
	@Inject
	private final Context context;
	@Inject
	EventLoggerManager eventLoggerManager;
	@Inject
	InboxViewManager inboxViewManager;
	@Inject
	ReadStateManager readStateManager;
	private SwipeTabManagerReceiver receiver;
	@Inject
	SwipeTabViewRelocationController relocationManager;
	private int screenHeight;
	private int screenWidth;
	@Inject
	Settings settings;
	private ViewGroup swipeRenderView;
	private View swipeTabMinimizedTouchView;
	private View swipeTabTouchView;
	private YettiTabLayout swipeTabView;
	private Boolean tabAddedToWindow;
	private boolean tabSizeAnimating;
	private eTabSizeState tabSizeState;

	OnTouchHandler touchHandler;
	private int unreadCount;

	class AnonymousClass_3 implements OnTouchListener {
		final/* synthetic */GestureDetector val$gestureDetector;

		AnonymousClass_3(GestureDetector gestureDetector) {
			this.val$gestureDetector = gestureDetector;
		}

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			return SwipeTabViewController.this.relocationManager.onTouchEvent(event).booleanValue() ? true
					: this.val$gestureDetector.onTouchEvent(event);
		}
	}

	class AnonymousClass_7 implements Runnable {
		final/* synthetic */Runnable val$onFinished;

		AnonymousClass_7(Runnable runnable) {
			this.val$onFinished = runnable;
		}

		@Override
		public void run() {
			SwipeTabViewController.this.tabSizeAnimating = false;
			SwipeTabViewController.this.refreshTabIcon();
			if (this.val$onFinished != null) {
				this.val$onFinished.run();
			}
		}
	}

	class AnonymousClass_8 implements Runnable {
		final/* synthetic */Runnable val$onFinished;

		AnonymousClass_8(Runnable runnable) {
			this.val$onFinished = runnable;
		}

		@Override
		public void run() {
			SwipeTabViewController.this.tabSizeAnimating = false;
			if (this.val$onFinished != null) {
				this.val$onFinished.run();
			}
		}
	}

	static/* synthetic */class AnonymousClass_9 {
		static final/* synthetic */int[] $SwitchMap$com$squanda$swoop$app$swipe$SwipeTabViewController$eTabSizeState;

		static {
			$SwitchMap$com$squanda$swoop$app$swipe$SwipeTabViewController$eTabSizeState = new int[eTabSizeState
					.values().length];
			try {
				$SwitchMap$com$squanda$swoop$app$swipe$SwipeTabViewController$eTabSizeState[eTabSizeState.Minimized
						.ordinal()] = 1;
			} catch (NoSuchFieldError e) {
			}
			try {
				$SwitchMap$com$squanda$swoop$app$swipe$SwipeTabViewController$eTabSizeState[eTabSizeState.FullSize
						.ordinal()] = 2;
			} catch (NoSuchFieldError e2) {
			}
		}
	}

	public static interface OnTouchHandler {
		void OnTapTab();
	}

	private class SwipeTabManagerReceiver extends BroadcastReceiver {
		private SwipeTabManagerReceiver() {
		}

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(INTENT_ACTION_CMDS)) {
				String command = intent.getStringExtra(SwoopBroadcastReceiver.TYPE_CMD);
				if (CMD_SET_TAB_GRAVITY.equals(command)) {
					SwipeTabViewController.this.setTabGravity(intent.getIntExtra(SwoopBroadcastReceiver.TYPE_DATA, 0));
				} else if (CMD_SET_TAB_VERTICAL.equals(command)) {
					SwipeTabViewController.this.setTabVerticalOffset((int) intent.getFloatExtra(
							SwoopBroadcastReceiver.TYPE_DATA, 0.0f));
				}
			}
		}
	}

	enum eTabSizeState {
		Minimized,
		FullSize
	}

	static {
		CHANGE_SIZE_ANIM_DURATION = 300;
		DRAG_DISTANCE_OPEN = 50;
		AUTO_HIDE_TIMER = 10000;
	}

	public SwipeTabViewController() {
		this.context = null;
		this.tabAddedToWindow = Boolean.valueOf(false);
		this.relocationManager = null;
		this.screenWidth = -1;
		this.screenHeight = -1;
		this.unreadCount = 0;
		this.tabSizeState = eTabSizeState.Minimized;
		this.tabSizeAnimating = false;
		this.relocationManager = new SwipeTabViewRelocationController();
	}

	private WindowManager getWindowManager() {
		return (WindowManager) this.context.getSystemService("window");
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public void start() throws Exception {
		this.receiver = new SwipeTabManagerReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(INTENT_ACTION_CMDS);
		this.context.registerReceiver(this.receiver, filter);
		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		Point size2 = new Point();
		display.getCurrentSizeRange(size, size2);
		this.screenWidth = size.x;
		this.screenHeight = size2.y;
		LayoutInflater inflater = (LayoutInflater) this.context.getSystemService("layout_inflater");
		setupRelocationManager();
		this.swipeTabTouchView = new View(this.context);
		this.swipeTabMinimizedTouchView = new View(this.context);
		this.swipeRenderView = new RelativeLayout(this.context);
		this.swipeTabView = (YettiTabLayout) inflater.inflate(R.layout.yetti_tab_layout, null);
		this.swipeTabView.setUnreadCount(0, null);
		this.swipeRenderView.addView(this.swipeTabView);
		refreshTabViewState();
		this.settings.registerSettingChangeListener(this);
		this.relocationManager.init();
	}

	public void refreshTabViewState() {
		if (this.tabAddedToWindow.booleanValue()) {
			if (!this.inboxViewManager.isTabVisible().booleanValue() || !isTabEnabled().booleanValue()) {
				removeTabViews();
			}
		} else if (this.inboxViewManager.isTabVisible().booleanValue() && isTabEnabled().booleanValue()) {
			startTabView();
		}
	}

	private void hideRenderView() {
		if (this.tabAddedToWindow.booleanValue()) {
			LayoutParams params = (LayoutParams) this.swipeRenderView.getLayoutParams();
			params.x = 100000;
			getWindowManager().updateViewLayout(this.swipeRenderView, params);
		}
	}

	private void setupRelocationManager() {
		this.relocationManager.setTabRelocationListener(new SwipeTabRelocationListener() {
			@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
			@Override
			public void onBeginRelocation() {
				SwipeTabViewController.this.swipeTabView.clearAnimation();
				SwipeTabViewController.this.swipeTabView.animate().alpha(0.0f).setDuration(0);
				SwipeTabViewController.this.hideRenderView();
			}

			@Override
			public void onFinishRelocation() {
				SwipeTabViewController.this.scheduleAutohideTimer();
				SwipeTabViewController.this.swipeTabView.playAnimation("IDLE");
				SwipeTabViewController.this.swipeTabView.setUnreadCount(SwipeTabViewController.this.unreadCount);
				SwipeTabViewController.this.refreshTabIcon();
				UIUtilities.delayOneFrame(new Runnable() {
					@Override
					public void run() {
						SwipeTabViewController.this.relocationManager.getSwipeTab().animate().alpha(0.0f)
								.setDuration(0).withEndAction(new Runnable() {
									@Override
									public void run() {
										Log.d(TAG, "Relocate: Inivisble");
									}
								});
						SwipeTabViewController.this.swipeTabView.animate()
								.alpha(DefaultRetryPolicy.DEFAULT_BACKOFF_MULT).setDuration(0)
								.withEndAction(new Runnable() {
									@Override
									public void run() {
										Log.d(TAG, "MainView: Visable");
										SwipeTabViewController.this.relocationManager.finishRelocation();
									}
								});
					}
				});
			}

			@Override
			public void onBeginHoverOverTrash() {
				SwipeTabViewController.this.getTabView().performHapticFeedback(3, 2);
			}

			@Override
			public void onBeginTrashing() {
			}

			@Override
			public void onFinishTrashing() {
				Intent intent = new Intent(SwipeViewManager.INTENT_ACTION_CMDS);
				intent.putExtra(SwoopBroadcastReceiver.TYPE_CMD, SwipeViewManager.CMD_TRASH_TAB);
				SwipeTabViewController.this.context.sendBroadcast(intent);
			}
		});
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void startTabView() {
		if (isTabEnabled().booleanValue() && !this.tabAddedToWindow.booleanValue()) {
			int x;
			int w = (int) this.context.getResources().getDimension(R.dimen.tab_maximized_width);
			int h = (int) this.context.getResources().getDimension(R.dimen.tab_height);
			int rightX = this.screenWidth - w;
			if (getTabGravity() == 5) {
				x = rightX;
			} else {
				x = 0;
			}
			addTabViewToWindow(this.swipeTabTouchView, x, getTabVerticalOffset(), w, h, Boolean.valueOf(true));
			addTabViewToWindow(this.swipeRenderView, x, getTabVerticalOffset(), w, h, Boolean.valueOf(false));
			this.swipeRenderView.setAlpha(DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
			w = (int) this.context.getResources().getDimension(R.dimen.tab_minimized_width);
			h = (int) this.context.getResources().getDimension(R.dimen.tab_height);
			rightX = this.screenWidth - w;
			if (getTabGravity() == 5) {
				x = rightX;
			} else {
				x = 0;
			}
			addTabViewToWindow(this.swipeTabMinimizedTouchView, x, getTabVerticalOffset(), w, h, Boolean.valueOf(true));
			this.tabAddedToWindow = Boolean.valueOf(true);
			setupClickHandlers();
			this.unreadCount = 0;
			refreshTabIcon();
		}
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void removeTabViews() {
		if (this.tabAddedToWindow.booleanValue()) {
			this.tabAddedToWindow = Boolean.valueOf(false);
			this.swipeRenderView.setAlpha(0.0f);
			this.relocationManager.finishRelocation();
			getWindowManager().removeView(this.swipeTabMinimizedTouchView);
			getWindowManager().removeView(this.swipeTabTouchView);
			getWindowManager().removeView(this.swipeRenderView);
		}
	}

	public View getTabView() {
		return this.swipeTabView;
	}

	public ViewGroup getRenderView() {
		return this.swipeRenderView;
	}

	public int getTabGravity() {
		return this.settings.getTabGravity();
	}

	public void setTabGravity(int gravity) {
		this.settings.setTabGravity(gravity);
		if (!this.relocationManager.isInProgress()) {
			refreshTabIcon();
		}
	}

	public int getTabVerticalOffset() {
		return this.settings.getTabVerticalOffset();
	}

	public void setTabVerticalOffset(int tabVerticalOffset) {
		this.settings.setTabVerticalOffset(tabVerticalOffset);
		refreshTabIcon();
	}

	private void setupClickHandlers() {
		GestureDetector gestureDetector = new GestureDetector(this.context, new SimpleOnGestureListener() {
			int deltaX;
			int deltaY;
			eTabSizeState tabSizeOnMouseDown;

			{
				this.deltaX = 0;
				this.deltaY = 0;
				this.tabSizeOnMouseDown = eTabSizeState.FullSize;
			}

			@Override
			public boolean onDown(MotionEvent motionEvent) {
				this.deltaX = 0;
				this.deltaY = 0;
				this.tabSizeOnMouseDown = SwipeTabViewController.this.tabSizeState;
				if (SwipeTabViewController.this.tabSizeState == eTabSizeState.Minimized
						&& SwipeTabViewController.this.getTabView() != null) {
					SwipeTabViewController.this.getTabView().performHapticFeedback(3, 2);
					Log.d(TAG, "You found the minimized tab.... vibrating!");
				}
				return true;
			}

			@Override
			public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent2, float v, float v2) {
				if (SwipeTabViewController.this.relocationManager.isInProgress()) {
					return false;
				}
				eTabSizeState lMinimizedState = SwipeTabViewController.this.getTabGravity() == 5 ? v < 0.0f ? eTabSizeState.FullSize
						: eTabSizeState.Minimized
						: v > 0.0f ? eTabSizeState.FullSize : eTabSizeState.Minimized;
				SwipeTabViewController.this.setTabSizeState(lMinimizedState, Boolean.valueOf(true), new Runnable() {
					@Override
					public void run() {
						if (SwipeTabViewController.this.getUnreadCount() == 0) {
							SwipeTabViewController.this.swipeTabView.playRandomAnimationAfterDelay(0);
						}
					}
				});
				return true;
			}

			@Override
			public void onLongPress(MotionEvent e) {
			}

			@Override
			public boolean onSingleTapConfirmed(MotionEvent e) {
				if (SwipeTabViewController.this.tabSizeState == eTabSizeState.Minimized) {
					return false;
				}
				SwipeTabViewController.this.fireOnTap();
				return true;
			}

			@Override
			public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
				this.deltaX = (int) (((SwipeTabViewController.this.getTabGravity() == 5 ? DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
						: -1.0f) * distanceX) + (this.deltaX));
				this.deltaY = (int) ((this.deltaY) + distanceY);
				if ((this.deltaX) > DRAG_DISTANCE_OPEN
						&& SwipeTabViewController.this.tabSizeState == eTabSizeState.Minimized) {
					SwipeTabViewController.this.setTabSizeState(eTabSizeState.FullSize, Boolean.valueOf(true),
							new Runnable() {
								@Override
								public void run() {
									if (SwipeTabViewController.this.getUnreadCount() == 0) {
										SwipeTabViewController.this.swipeTabView.playRandomAnimationAfterDelay(0);
									}
								}
							});
				} else if ((this.deltaX) < (-DRAG_DISTANCE_OPEN)
						&& SwipeTabViewController.this.tabSizeState == eTabSizeState.FullSize) {
					SwipeTabViewController.this.setTabSizeState(eTabSizeState.Minimized);
				} else if (((this.deltaX) > DRAG_DISTANCE_OPEN || (Math.abs(this.deltaY)) > DRAG_DISTANCE_OPEN)
						&& SwipeTabViewController.this.tabSizeState == eTabSizeState.FullSize
						&& !SwipeTabViewController.this.tabSizeAnimating
						&& this.tabSizeOnMouseDown == eTabSizeState.FullSize
						&& !SwipeTabViewController.this.relocationManager.isInProgress()) {
					SwipeTabViewController.this.relocationManager.startRelocation(e2,
							SwipeTabViewController.this.unreadCount);
					SwipeTabViewController.this.clearAutoHideTimer();
				}
				return true;
			}
		});
		OnTouchListener touchListener = new AnonymousClass_3(gestureDetector);
		gestureDetector.setIsLongpressEnabled(false);
		this.swipeTabTouchView.setOnTouchListener(touchListener);
		this.swipeTabMinimizedTouchView.setOnTouchListener(touchListener);
	}

	private void addTabViewToWindow(View view, int x, int y, int w, int h, Boolean touchable) {
		int flags = 16843544;
		if (!touchable.booleanValue()) {
			flags = 16843544 | 16;
		}
		LayoutParams params = new LayoutParams(-2, -2, 2007, flags, -3);
		params.gravity = 51;
		params.width = w;
		params.height = h;
		params.x = x;
		params.y = y;
		try {
			Field privateFlags = params.getClass().getDeclaredField("privateFlags");
			privateFlags.setInt(params, privateFlags.getInt(params) | 64);
		} catch (NoSuchFieldException ex) {
			ex.printStackTrace();
		} catch (IllegalAccessException ex2) {
			ex2.printStackTrace();
		}
		getWindowManager().addView(view, params);
	}

	public void setTouchHandler(OnTouchHandler touchHandler) {
		this.touchHandler = touchHandler;
	}

	public void stop() {
		this.settings.unregisterSettingChangeListener(this);
		this.relocationManager.finishRelocation();
		removeTabViews();
	}

	@Override
	public void onSettingChanged(Settings settings, String key) {
		if (key.equals(Settings.KEY_TAB_DISPLAY_STATE)) {
			refreshTabViewState();
			refreshTabIcon();
		}
	}

	public Boolean isTabEnabled() {
		return Boolean.valueOf(!this.settings.getTabDisplayState().equals(TAB_DISPLAY_STATE_HIDDEN));
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
	public void refreshTabIcon() {
		if (this.tabAddedToWindow.booleanValue()) {
			Log.d("SwipeTab", "refreshTabIcon");
			LayoutParams layoutParamsMaxView = (LayoutParams) this.swipeTabTouchView.getLayoutParams();
			layoutParamsMaxView.y = getTabVerticalOffset();
			if (this.tabSizeState == eTabSizeState.FullSize) {
				layoutParamsMaxView.flags &= -17;
			} else {
				layoutParamsMaxView.flags |= 16;
			}
			layoutParamsMaxView.width = (int) this.context.getResources().getDimension(R.dimen.tab_maximized_width);
			if (getTabGravity() == 5) {
				layoutParamsMaxView.x = this.screenWidth - layoutParamsMaxView.width;
			} else {
				layoutParamsMaxView.x = 0;
			}
			getWindowManager().updateViewLayout(this.swipeTabTouchView, layoutParamsMaxView);
			LayoutParams layoutParamsMinView = (LayoutParams) this.swipeTabMinimizedTouchView.getLayoutParams();
			layoutParamsMinView.y = getTabVerticalOffset();
			if (this.tabSizeState == eTabSizeState.Minimized) {
				layoutParamsMinView.flags &= -17;
			} else {
				layoutParamsMinView.flags |= 16;
			}
			layoutParamsMinView.width = (int) this.context.getResources().getDimension(
					R.dimen.tab_minimized_touch_width);
			if (this.settings.getTabDisplayState().equals(TAB_DISPLAY_STATE_MESSAGE_ONLY)) {
				layoutParamsMinView.width = 0;
			}
			if (getTabGravity() == 5) {
				layoutParamsMinView.x = this.screenWidth - layoutParamsMinView.width;
			} else {
				layoutParamsMinView.x = 0;
			}
			getWindowManager().updateViewLayout(this.swipeTabMinimizedTouchView, layoutParamsMinView);
			float smallWidth = this.context.getResources().getDimension(R.dimen.tab_minimized_width);
			if (this.settings.getTabDisplayState().equals(TAB_DISPLAY_STATE_MESSAGE_ONLY)) {
				smallWidth = 0.0f;
			}
			float largeWidth = this.context.getResources().getDimension(R.dimen.tab_width);
			float renderWidth = this.context.getResources().getDimension(R.dimen.tab_maximized_width);
			float marginFromEdge = renderWidth - largeWidth;
			LayoutParams layoutParamsRenderView = (LayoutParams) this.swipeRenderView.getLayoutParams();
			layoutParamsRenderView.y = getTabVerticalOffset();
			this.swipeTabView.setScaleX(DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
			this.swipeTabView.setScaleY(DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
			if (getTabGravity() == 5) {
				layoutParamsRenderView.x = (int) ((this.screenWidth) - renderWidth);
				if (this.tabSizeState == eTabSizeState.FullSize) {
					this.swipeTabView.setX(marginFromEdge);
				} else {
					this.swipeTabView.setX((largeWidth - smallWidth) + marginFromEdge);
				}
			} else {
				layoutParamsRenderView.x = 0;
				if (this.tabSizeState == eTabSizeState.FullSize) {
					this.swipeTabView.setX(0.0f);
				} else {
					this.swipeTabView.setX((-largeWidth) + smallWidth);
				}
			}
			if (!this.relocationManager.isInProgress()) {
				if (this.inboxViewManager.isTabVisible().booleanValue()) {
					this.swipeTabView.animate().alpha(DefaultRetryPolicy.DEFAULT_BACKOFF_MULT).setDuration(0);
				} else {
					this.swipeTabView.animate().alpha(0.0f).setDuration(0);
				}
			}
			getWindowManager().updateViewLayout(this.swipeRenderView, layoutParamsRenderView);
			setupClickHandlers();
		}
	}

	private void fireOnTap() {
		if (this.touchHandler != null) {
			this.touchHandler.OnTapTab();
		}
	}

	public void setUnreadCount(int count) {
		if (count != this.unreadCount) {
			this.unreadCount = count;
			if (count != 0) {
				this.inboxViewManager.setTabVisibility(Boolean.valueOf(true), eVisibilityReason.Docked);
				if (this.tabSizeState != eTabSizeState.FullSize) {
					setTabSizeState(eTabSizeState.FullSize, Boolean.valueOf(true), new Runnable() {
						@Override
						public void run() {
							if (SwipeTabViewController.this.swipeTabView.isRotated().booleanValue()) {
								SwipeTabViewController.this.swipeTabView
										.setUnreadCount(SwipeTabViewController.this.unreadCount);
							} else {
								SwipeTabViewController.this.swipeTabView.rotateAndShowValue(new IAnimationListener() {
									@Override
									public void onAnimationUpdated(int frame) {
									}

									@Override
									public void onAnimationFinished() {
										SwipeTabViewController.this.swipeTabView
												.setUnreadCount(SwipeTabViewController.this.unreadCount);
									}
								});
							}
						}
					});
					return;
				} else if (this.tabSizeAnimating) {
					Log.d(TAG, "Skipping unread cound animation. Already going to display.");
					return;
				} else if (this.swipeTabView.isRotated().booleanValue()) {
					this.swipeTabView.setUnreadCount(this.unreadCount);
					return;
				} else {
					this.swipeTabView.rotateAndShowValue(new IAnimationListener() {
						@Override
						public void onAnimationUpdated(int frame) {
						}

						@Override
						public void onAnimationFinished() {
							SwipeTabViewController.this.swipeTabView
									.setUnreadCount(SwipeTabViewController.this.unreadCount);
						}
					});
					return;
				}
			}
			if (this.inboxViewManager.isTabVisibleFieldSet(eVisibilityReason.LockScreen).booleanValue()) {
				setTabSizeState(eTabSizeState.Minimized, Boolean.valueOf(false), null);
			} else {
				setTabSizeState(eTabSizeState.Minimized, Boolean.valueOf(true), null);
			}
			this.swipeTabView.playAnimation("IDLE");
			this.swipeTabView.setUnreadCount(this.unreadCount);
		}
	}

	public int getUnreadCount() {
		return this.unreadCount;
	}

	public void setTabSizeState(eTabSizeState state) {
		setTabSizeState(state, Boolean.valueOf(true), null);
	}

	public void scheduleAutohideTimer() {
		clearAutoHideTimer();
		this.autoHideTimer = new Timer();
		this.autoHideTask = new TimerTask() {
			@Override
			public void run() {
				if (SwipeTabViewController.this.inboxViewManager.isTabVisible().booleanValue()
						&& SwipeTabViewController.this.tabSizeState == eTabSizeState.FullSize
						&& SwipeTabViewController.this.getUnreadCount() == 0) {
					Log.d(TAG, "Schedule is Minimizing");
					SwipeTabViewController.this.setTabSizeState(eTabSizeState.Minimized);
				}
			}
		};
		this.autoHideTimer.schedule(this.autoHideTask, AUTO_HIDE_TIMER);
	}

	private void clearAutoHideTimer() {
		if (this.autoHideTimer != null) {
			this.autoHideTimer.cancel();
			this.autoHideTask.cancel();
			this.autoHideTask = null;
			this.autoHideTimer = null;
		}
	}

	public void setTabSizeState(eTabSizeState state, Boolean animate, Runnable onFinished) {
		int sign = 1;
		if (this.tabSizeState != state) {
			Log.d("setTabSizeState", "state : " + state + " animate:" + animate);
			this.tabSizeState = state;
			this.tabSizeAnimating = true;
			float smallWidth = this.context.getResources().getDimension(R.dimen.tab_minimized_width);
			float largeWidth = this.context.getResources().getDimension(R.dimen.tab_width);
			if (getTabGravity() == 3) {
				sign = -1;
			}
			float delta = (largeWidth - smallWidth) * (sign);
			if (this.settings.getTabDisplayState().equals(TAB_DISPLAY_STATE_MESSAGE_ONLY)) {
				delta = largeWidth * (sign);
			}
			if (animate.booleanValue()) {
				switch (AnonymousClass_9.$SwitchMap$com$squanda$swoop$app$swipe$SwipeTabViewController$eTabSizeState[this.tabSizeState
						.ordinal()]) {
				case DragSortController.ON_DRAG /* 1 */:
					clearAutoHideTimer();
					this.swipeTabView.animate().translationXBy(delta).setDuration(CHANGE_SIZE_ANIM_DURATION)
							.setInterpolator(new BackOutInterpolator()).withEndAction(new AnonymousClass_7(onFinished));
					return;
				case DragSortController.ON_LONG_PRESS /* 2 */:
					refreshTabIcon();
					scheduleAutohideTimer();
					this.swipeTabView.setX(this.swipeTabView.getX() + delta);
					this.swipeTabView.animate().translationXBy(-delta).setInterpolator(new BackOutInterpolator())
							.setDuration(CHANGE_SIZE_ANIM_DURATION).withEndAction(new AnonymousClass_8(onFinished));
					return;
				default:
					return;
				}
			}
			refreshTabIcon();
		}
	}
}

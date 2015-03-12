package blue.stack.snowball.app.lockscreen.ui;

import blue.stack.snowball.app.R;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import blue.stack.snowball.app.core.GuiceModule;
import blue.stack.snowball.app.inbox.InboxListener;
import blue.stack.snowball.app.inbox.InboxManager;
import blue.stack.snowball.app.inbox.Message;
import blue.stack.snowball.app.logging.RemoteDiagnostics;
import blue.stack.snowball.app.logging.RemoteDiagnosticsListener;
import blue.stack.snowball.app.logging.RemoteLogger;
import blue.stack.snowball.app.settings.Settings;
import blue.stack.snowball.app.ui.InterceptTouchFrameLayout;

import com.android.volley.DefaultRetryPolicy;
import com.google.inject.Inject;

public class LockScreenWidget implements InboxListener, RemoteDiagnosticsListener {
	private static final int CLEAR_TIME = 200;
	private static final int FADE_TIME = 200;
	private static final int MAX_NUM_PAGES = 5;
	private static final float RELOCATE_WINDOW_SCALE = 1.05f;
	private static final String TAG = "LockScreenWidget";
	@Inject
	Context context;
	WidgetState currentState;
	LinearLayout fullscreenView;
	boolean hasMoveStarted;
	@Inject
	InboxManager inboxManager;
	boolean isPagerBeingDragged;
	boolean isTouchDown;
	float lastY;
	LockscreenWidgetListener listener;
	LockScreenWidgetPagerAdapter pagerAdapter;
	ViewPager pagerView;
	LockscreenWidgetPagesLayout pagesView;
	@Inject
	RemoteLogger remoteLogger;
	InterceptTouchFrameLayout rootView;
	View screenshotView;
	int showSinceLastMessageId;
	View unlockToContinueView;
	View widgetView;

	static/* synthetic */class AnonymousClass_11 {
		static final/* synthetic */int[] $SwitchMap$com$squanda$swoop$app$lockscreen$ui$LockScreenWidget$WidgetAction;
		static final/* synthetic */int[] $SwitchMap$com$squanda$swoop$app$lockscreen$ui$LockScreenWidget$WidgetState;

		static {
			$SwitchMap$com$squanda$swoop$app$lockscreen$ui$LockScreenWidget$WidgetState = new int[WidgetState.values().length];
			try {
				$SwitchMap$com$squanda$swoop$app$lockscreen$ui$LockScreenWidget$WidgetState[WidgetState.stateShown
						.ordinal()] = 1;
			} catch (NoSuchFieldError e) {
			}
			try {
				$SwitchMap$com$squanda$swoop$app$lockscreen$ui$LockScreenWidget$WidgetState[WidgetState.stateUnlockToContinueShown
						.ordinal()] = 2;
			} catch (NoSuchFieldError e2) {
			}
			try {
				$SwitchMap$com$squanda$swoop$app$lockscreen$ui$LockScreenWidget$WidgetState[WidgetState.stateUnlockToContinueHidden
						.ordinal()] = 3;
			} catch (NoSuchFieldError e3) {
			}
			try {
				$SwitchMap$com$squanda$swoop$app$lockscreen$ui$LockScreenWidget$WidgetState[WidgetState.stateCleared
						.ordinal()] = 4;
			} catch (NoSuchFieldError e4) {
			}
			try {
				$SwitchMap$com$squanda$swoop$app$lockscreen$ui$LockScreenWidget$WidgetState[WidgetState.stateMoving
						.ordinal()] = 5;
			} catch (NoSuchFieldError e5) {
			}
			try {
				$SwitchMap$com$squanda$swoop$app$lockscreen$ui$LockScreenWidget$WidgetState[WidgetState.stateStopMoving
						.ordinal()] = 6;
			} catch (NoSuchFieldError e6) {
			}
			try {
				$SwitchMap$com$squanda$swoop$app$lockscreen$ui$LockScreenWidget$WidgetState[WidgetState.stateHidden
						.ordinal()] = 7;
			} catch (NoSuchFieldError e7) {
			}
			try {
				$SwitchMap$com$squanda$swoop$app$lockscreen$ui$LockScreenWidget$WidgetState[WidgetState.stateTerminated
						.ordinal()] = 8;
			} catch (NoSuchFieldError e8) {
			}
			$SwitchMap$com$squanda$swoop$app$lockscreen$ui$LockScreenWidget$WidgetAction = new int[WidgetAction
					.values().length];
			try {
				$SwitchMap$com$squanda$swoop$app$lockscreen$ui$LockScreenWidget$WidgetAction[WidgetAction.actionShow
						.ordinal()] = 1;
			} catch (NoSuchFieldError e9) {
			}
			try {
				$SwitchMap$com$squanda$swoop$app$lockscreen$ui$LockScreenWidget$WidgetAction[WidgetAction.actionShowUnlockToContinue
						.ordinal()] = 2;
			} catch (NoSuchFieldError e10) {
			}
			try {
				$SwitchMap$com$squanda$swoop$app$lockscreen$ui$LockScreenWidget$WidgetAction[WidgetAction.actionHideUnlockToContinue
						.ordinal()] = 3;
			} catch (NoSuchFieldError e11) {
			}
			try {
				$SwitchMap$com$squanda$swoop$app$lockscreen$ui$LockScreenWidget$WidgetAction[WidgetAction.actionClear
						.ordinal()] = 4;
			} catch (NoSuchFieldError e12) {
			}
			try {
				$SwitchMap$com$squanda$swoop$app$lockscreen$ui$LockScreenWidget$WidgetAction[WidgetAction.actionHide
						.ordinal()] = 5;
			} catch (NoSuchFieldError e13) {
			}
			try {
				$SwitchMap$com$squanda$swoop$app$lockscreen$ui$LockScreenWidget$WidgetAction[WidgetAction.actionStartMove
						.ordinal()] = 6;
			} catch (NoSuchFieldError e14) {
			}
			try {
				$SwitchMap$com$squanda$swoop$app$lockscreen$ui$LockScreenWidget$WidgetAction[WidgetAction.actionStopMove
						.ordinal()] = 7;
			} catch (NoSuchFieldError e15) {
			}
			try {
				$SwitchMap$com$squanda$swoop$app$lockscreen$ui$LockScreenWidget$WidgetAction[WidgetAction.actionTerminate
						.ordinal()] = 8;
			} catch (NoSuchFieldError e16) {
			}
		}
	}

	public static interface LockscreenWidgetListener {
		void onWidgetCleared();
	}

	enum WidgetAction {
		actionShow,
		actionHide,
		actionShowUnlockToContinue,
		actionHideUnlockToContinue,
		actionStartMove,
		actionStopMove,
		actionClear,
		actionTerminate
	}

	enum WidgetState {
		stateShown,
		stateInterpolatingToShown,
		stateHidden,
		stateInterpolatingToHidden,
		stateMoving,
		stateInterpolatingToMoving,
		stateStopMoving,
		stateInterpolatingToStopMoving,
		stateCleared,
		stateInterpolatingToCleared,
		stateUnlockToContinueShown,
		stateInterpolatingToUnlockToContinueShown,
		stateUnlockToContinueHidden,
		stateInterpolatingToUnlockToContinueHidden,
		stateTerminated,
		stateInterpolatingToTerminated
	}

	public LockScreenWidget() {
		GuiceModule.get().injectMembers(this);
	}

	public void start(int showSinceLastMessageId, LockscreenWidgetListener listener) {
		this.showSinceLastMessageId = showSinceLastMessageId;
		this.listener = listener;
		this.isPagerBeingDragged = false;
		this.isTouchDown = false;
		this.hasMoveStarted = false;
		this.currentState = WidgetState.stateHidden;
		this.inboxManager.addListener(this);
		this.remoteLogger.addRemoteDiagnosticsListener(this);
		createView();
		setupMoveHandlers();
	}

	public void stop() {
		performAction(WidgetAction.actionTerminate);
		this.remoteLogger.removeRemoteDiagnosticsListener(this);
		this.inboxManager.removeListener(this);
		this.listener = null;
	}

	public void showUnlockToContinue() {
		performAction(WidgetAction.actionShowUnlockToContinue);
	}

	public void hideUnlockToContinue() {
		performAction(WidgetAction.actionHideUnlockToContinue);
	}

	public void setShowSinceLastMessageId(int showSinceLastMessageId) {
		this.showSinceLastMessageId = showSinceLastMessageId;
		this.pagerAdapter.setShowSinceMessageId(showSinceLastMessageId);
	}

	void createView() {
		LayoutInflater inflater = (LayoutInflater) this.context.getSystemService("layout_inflater");
		this.unlockToContinueView = inflater.inflate(R.layout.lockscreen_unlock_to_continue_view, null);
		this.widgetView = inflater.inflate(R.layout.lockscreen_widget, null);
		this.rootView = new InterceptTouchFrameLayout(this.context);
		LayoutParams params = new LayoutParams(-1, -2, 2010, 16777496, -3);
		WindowManager windowManager = (WindowManager) this.context.getSystemService("window");
		params.gravity = 51;
		params.y = 0;
		windowManager.addView(this.rootView, params);
		this.rootView.addView(this.widgetView);
		this.widgetView.setAlpha(0.0f);
		this.fullscreenView = new LinearLayout(this.context);
		params = new LayoutParams(-1, -2, 2010, 16777496, -3);
		params.gravity = 51;
		params.width = -1;
		params.height = -1;
		windowManager.addView(this.fullscreenView, params);
		this.fullscreenView.setAlpha(DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
		this.pagerView = (ViewPager) this.widgetView.findViewById(R.id.lockscreen_widget_pager);
		this.pagesView = (LockscreenWidgetPagesLayout) this.widgetView.findViewById(R.id.lockscreen_widget_pages);
		this.pagerAdapter = new LockScreenWidgetPagerAdapter();
		this.pagerAdapter.start(this.context, this.showSinceLastMessageId, MAX_NUM_PAGES);
		this.pagerView.setAdapter(this.pagerAdapter);
		this.pagerView.setOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
			}

			@Override
			public void onPageSelected(int position) {
				LockScreenWidget.this.pagesView.setSelectedPage(position);
			}

			@Override
			public void onPageScrollStateChanged(int state) {
				if (state == 0) {
					Log.d(TAG, "Pager is NOT being dragged");
					LockScreenWidget.this.isPagerBeingDragged = false;
					return;
				}
				Log.d(TAG, "Pager is being dragged");
				LockScreenWidget.this.isPagerBeingDragged = true;
			}
		});
		resetPageAdapter();
	}

	void setupMoveHandlers() {
		this.rootView.setOnInterceptTouchListener(new OnTouchListener() {
			GestureDetector gestureDetector;

			{
				this.gestureDetector = new GestureDetector(LockScreenWidget.this.context, new OnGestureListener() {
					@Override
					public boolean onDown(MotionEvent motionEvent) {
						return false;
					}

					@Override
					public void onShowPress(MotionEvent motionEvent) {
					}

					@Override
					public boolean onSingleTapUp(MotionEvent motionEvent) {
						return false;
					}

					@Override
					public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent2, float v, float v2) {
						return false;
					}

					@Override
					public void onLongPress(MotionEvent motionEvent) {
					}

					@Override
					public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent2, float v, float v2) {
						if (v2 >= -1000.0f || LockScreenWidget.this.isPagerBeingDragged) {
							return false;
						}
						LockScreenWidget.this.performAction(WidgetAction.actionClear);
						return true;
					}
				});
			}

			@Override
			public boolean onTouch(View view, MotionEvent motionEvent) {
				this.gestureDetector.onTouchEvent(motionEvent);
				boolean moved;
				switch (motionEvent.getAction()) {
				case 1/* 1 */:
				case 3 /* 3 */:
					moved = false;
					if (LockScreenWidget.this.hasMoveStarted) {
						moved = true;
						LockScreenWidget.this.performAction(WidgetAction.actionStopMove);
					}
					LockScreenWidget.this.isTouchDown = false;
					LockScreenWidget.this.hasMoveStarted = false;
					return moved;
				case 2 /* 2 */:
					moved = false;
					if (!LockScreenWidget.this.isTouchDown || LockScreenWidget.this.isPagerBeingDragged) {
						return false;
					}
					if (LockScreenWidget.this.hasMoveStarted && LockScreenWidget.this.screenshotView != null) {
						float newY = LockScreenWidget.this.screenshotView.getY()
								+ (motionEvent.getRawY() - LockScreenWidget.this.lastY);
						int maxY = LockScreenWidget.this.fullscreenView.getHeight()
								- LockScreenWidget.this.screenshotView.getHeight();
						if (newY < 0.0f) {
							Log.d(TAG, "Hit top edge with newY = " + newY + "  |   setting newY to 0");
							newY = 0.0f;
						} else if (newY > (maxY)) {
							Log.d(TAG, "Hit bottom edge with newY = " + newY + "  | setting newY " + maxY);
							newY = maxY;
						}
						LockScreenWidget.this.screenshotView.setY(newY);
						moved = true;
					}
					LockScreenWidget.this.lastY = motionEvent.getRawY();
					return moved;
				default:
					return false;
				}
			}
		});
	}

	void performAction(WidgetAction action) {
		switch (AnonymousClass_11.$SwitchMap$com$squanda$swoop$app$lockscreen$ui$LockScreenWidget$WidgetAction[action
				.ordinal()]) {
		case 1 /* 1 */:
			if (this.currentState == WidgetState.stateCleared
					|| this.currentState == WidgetState.stateInterpolatingToCleared
					|| this.currentState == WidgetState.stateHidden
					|| this.currentState == WidgetState.stateInterpolatingToHidden) {
				changeState(WidgetState.stateShown);
			} else if (this.currentState == WidgetState.stateUnlockToContinueShown
					|| this.currentState == WidgetState.stateInterpolatingToUnlockToContinueShown) {
				changeState(WidgetState.stateUnlockToContinueHidden);
			}
		case 2 /* 2 */:
			if (this.currentState == WidgetState.stateShown
					|| this.currentState == WidgetState.stateInterpolatingToShown) {
				changeState(WidgetState.stateUnlockToContinueShown);
			}
		case 3 /* 3 */:
			if (this.currentState == WidgetState.stateUnlockToContinueShown
					|| this.currentState == WidgetState.stateInterpolatingToUnlockToContinueShown) {
				changeState(WidgetState.stateUnlockToContinueHidden);
			}
		case 4 /* 4 */:
			if (this.currentState == WidgetState.stateShown) {
				changeState(WidgetState.stateCleared);
			}
		case 5:// MAX_NUM_PAGES /*5*/:
			if (this.currentState == WidgetState.stateShown
					|| this.currentState == WidgetState.stateInterpolatingToShown) {
				changeState(WidgetState.stateHidden);
			}
		case 6:// Textifier.TYPE_DECLARATION /*6*/:
			if (this.currentState == WidgetState.stateShown) {
				changeState(WidgetState.stateMoving);
			}
		case 7:// Textifier.CLASS_DECLARATION /*7*/:
			if (this.currentState == WidgetState.stateMoving
					|| this.currentState == WidgetState.stateInterpolatingToMoving) {
				changeState(WidgetState.stateStopMoving);
			}
		case 8:// SyslogAppender.LOG_USER /*8*/:
			if (this.currentState != WidgetState.stateTerminated
					&& this.currentState != WidgetState.stateInterpolatingToTerminated) {
				changeState(WidgetState.stateTerminated);
			}
		default:
		}
	}

	void changeState(WidgetState nextState) {
		Log.d(TAG, "Switching state from: " + this.currentState + " to: " + nextState);
		LayoutParams layoutParams;
		switch (AnonymousClass_11.$SwitchMap$com$squanda$swoop$app$lockscreen$ui$LockScreenWidget$WidgetState[nextState
				.ordinal()]) {
		case 1:// DragSortController.ON_DRAG /*1*/:
			this.widgetView.clearAnimation();
			this.widgetView.setX(0.0f);
			this.widgetView.setAlpha(0.0f);
			this.widgetView.animate().alpha(0.0f).setDuration(0).withEndAction(new Runnable() {
				@Override
				public void run() {
					LockScreenWidget.this.widgetView.animate().alpha(DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
							.setDuration(200).setInterpolator(new DecelerateInterpolator())
							.withEndAction(new Runnable() {
								@Override
								public void run() {
									LockScreenWidget.this.enableTouchOnRootView(true);
									LockScreenWidget.this.currentState = WidgetState.stateShown;
								}
							});
				}
			});
			this.currentState = WidgetState.stateInterpolatingToShown;
		case 2:// DragSortController.ON_LONG_PRESS /*2*/:
			enableTouchOnRootView(false);
			this.unlockToContinueView = ((LayoutInflater) this.context.getSystemService("layout_inflater")).inflate(
					R.layout.lockscreen_unlock_to_continue_view, null);
			addToFullscreenView(this.unlockToContinueView);
			ViewGroup.LayoutParams layoutParams2 = this.unlockToContinueView.getLayoutParams();
			layoutParams2.width = -1;
			this.unlockToContinueView.setLayoutParams(layoutParams2);
			this.unlockToContinueView.setAlpha(0.0f);
			this.widgetView.clearAnimation();
			this.widgetView.animate().alpha(0.0f).setDuration(200).setInterpolator(new AccelerateInterpolator())
					.withEndAction(new Runnable() {
						@Override
						public void run() {
							LockScreenWidget.this.unlockToContinueView.animate()
									.alpha(DefaultRetryPolicy.DEFAULT_BACKOFF_MULT).setDuration(200).setStartDelay(100)
									.setInterpolator(new DecelerateInterpolator()).withEndAction(new Runnable() {
										@Override
										public void run() {
											LockScreenWidget.this.currentState = WidgetState.stateUnlockToContinueShown;
										}
									});
						}
					});
			this.currentState = WidgetState.stateInterpolatingToUnlockToContinueShown;
		case 3:// Textifier.METHOD_DESCRIPTOR /*3*/:
			enableTouchOnRootView(false);
			this.widgetView.clearAnimation();
			this.unlockToContinueView.clearAnimation();
			this.unlockToContinueView.animate().alpha(0.0f).setDuration(200)
					.setInterpolator(new AccelerateInterpolator()).withEndAction(new Runnable() {
						@Override
						public void run() {
							LockScreenWidget.this.widgetView.animate().alpha(DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
									.setDuration(200).setStartDelay(100).setInterpolator(new DecelerateInterpolator())
									.withEndAction(new Runnable() {
										@Override
										public void run() {
											LockScreenWidget.this.currentState = WidgetState.stateShown;
											LockScreenWidget.this.clearFullscreenView();
											LockScreenWidget.this.unlockToContinueView = null;
											LockScreenWidget.this.enableTouchOnRootView(true);
											LockScreenWidget.this.currentState = WidgetState.stateShown;
										}
									});
						}
					});
			this.currentState = WidgetState.stateInterpolatingToUnlockToContinueHidden;
		case 4:// Textifier.METHOD_SIGNATURE /*4*/:
			enableTouchOnRootView(false);
			this.widgetView.clearAnimation();
			this.widgetView.animate().alpha(0.0f).setDuration(200).setInterpolator(new AccelerateInterpolator())
					.withEndAction(new Runnable() {
						@Override
						public void run() {
							if (LockScreenWidget.this.listener != null) {
								LockScreenWidget.this.listener.onWidgetCleared();
							}
							LockScreenWidget.this.currentState = WidgetState.stateCleared;
						}
					});
			this.currentState = WidgetState.stateInterpolatingToCleared;
		case MAX_NUM_PAGES /* 5 */:
			this.widgetView.clearAnimation();
			layoutParams = (LayoutParams) this.rootView.getLayoutParams();
			this.screenshotView = duplicateView(this.widgetView);
			this.screenshotView.setY(layoutParams.y);
			Log.d(TAG, "Setting widget view screenshot to Y position = " + layoutParams.y);
			addToFullscreenView(this.screenshotView);
			this.widgetView.animate().alpha(0.0f).setDuration(0).setStartDelay(100);
			this.screenshotView.animate().scaleX(RELOCATE_WINDOW_SCALE).scaleY(RELOCATE_WINDOW_SCALE).setDuration(200)
					.withEndAction(new Runnable() {
						@Override
						public void run() {
							LockScreenWidget.this.currentState = WidgetState.stateMoving;
						}
					});
			this.screenshotView.performHapticFeedback(3, 2);
			this.currentState = WidgetState.stateInterpolatingToMoving;
		case 6:// Textifier.TYPE_DECLARATION /*6*/:
			WindowManager windowManager = (WindowManager) this.context.getSystemService("window");
			layoutParams = (LayoutParams) this.rootView.getLayoutParams();
			layoutParams.y = (int) this.screenshotView.getY();
			windowManager.updateViewLayout(this.rootView, layoutParams);
			this.screenshotView.setScaleY(RELOCATE_WINDOW_SCALE);
			this.screenshotView.setScaleX(RELOCATE_WINDOW_SCALE);
			this.screenshotView.animate().scaleY(DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
					.scaleX(DefaultRetryPolicy.DEFAULT_BACKOFF_MULT).setDuration(200).withEndAction(new Runnable() {
						@Override
						public void run() {
							LockScreenWidget.this.widgetView.animate().alpha(DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
									.setDuration(0).withEndAction(new Runnable() {
										@Override
										public void run() {
											LockScreenWidget.this.clearFullscreenView();
											LockScreenWidget.this.screenshotView = null;
											LockScreenWidget.this.currentState = WidgetState.stateShown;
										}
									});
						}
					});
			GuiceModule.get().getInstance(Settings.class).updateLockscreenWidgetY(this.screenshotView.getY());
			this.currentState = WidgetState.stateInterpolatingToStopMoving;
			Log.d(TAG, "Restoring widget view at Y position = " + layoutParams.y + " with height = "
					+ layoutParams.height);
		case 7:// Textifier.CLASS_DECLARATION /*7*/:
			enableTouchOnRootView(false);
			this.widgetView.clearAnimation();
			this.widgetView.animate().alpha(0.0f).setDuration(200).setInterpolator(new AccelerateInterpolator())
					.withEndAction(new Runnable() {
						@Override
						public void run() {
							LockScreenWidget.this.enableTouchOnRootView(false);
							LockScreenWidget.this.currentState = WidgetState.stateHidden;
						}
					});
			this.currentState = WidgetState.stateInterpolatingToHidden;
		case 8:// SyslogAppender.LOG_USER /*8*/:
			enableTouchOnRootView(false);
			this.widgetView.clearAnimation();
			this.fullscreenView.clearAnimation();
			if (this.unlockToContinueView != null) {
				this.unlockToContinueView.clearAnimation();
				this.unlockToContinueView.animate().alpha(0.0f).setDuration(200)
						.setInterpolator(new AccelerateInterpolator());
			}
			this.widgetView.animate().alpha(0.0f).setDuration(200).setInterpolator(new AccelerateInterpolator())
					.withEndAction(new Runnable() {
						@Override
						public void run() {
							WindowManager wm = (WindowManager) LockScreenWidget.this.context.getSystemService("window");
							try {
								wm.removeView(LockScreenWidget.this.fullscreenView);
							} catch (Exception e) {
							}
							try {
								wm.removeView(LockScreenWidget.this.rootView);
							} catch (Exception e2) {
							}
							LockScreenWidget.this.widgetView = null;
							LockScreenWidget.this.screenshotView = null;
							LockScreenWidget.this.unlockToContinueView = null;
							LockScreenWidget.this.fullscreenView = null;
							LockScreenWidget.this.rootView = null;
							LockScreenWidget.this.context = null;
							LockScreenWidget.this.currentState = WidgetState.stateTerminated;
						}
					});
			this.currentState = WidgetState.stateInterpolatingToTerminated;
		default:
		}
	}

	@Override
	public void onInboxUpdated() {
	}

	@Override
	public void onInboxMessageAdded(Message message) {
		resetPageAdapter();
	}

	@Override
	public void onInboxCleared() {
	}

	@Override
	public void onRemoteDiagnosticsRequested(RemoteDiagnostics diagnostics) {
		boolean z;
		boolean z2 = true;
		diagnostics.addDiagnostic(TAG, "hashCode", hashCode());
		diagnostics.addDiagnostic(TAG, "currentState", this.currentState.ordinal());
		diagnostics.addDiagnostic(TAG, "showSinceLastMessageId", this.showSinceLastMessageId);
		diagnostics.addDiagnostic(TAG, "isPagerBeingDragged", this.isPagerBeingDragged);
		diagnostics.addDiagnostic(TAG, "isTouchDown", this.isTouchDown);
		diagnostics.addDiagnostic(TAG, "hasMoveStarted", this.hasMoveStarted);
		diagnostics.addDiagnostic(TAG, "isRootViewNull", this.rootView == null);
		String str = TAG;
		String str2 = "isWidgetViewNull";
		if (this.widgetView == null) {
			z = true;
		} else {
			z = false;
		}
		diagnostics.addDiagnostic(str, str2, z);
		str = TAG;
		str2 = "isFullscreenViewNull";
		if (this.fullscreenView == null) {
			z = true;
		} else {
			z = false;
		}
		diagnostics.addDiagnostic(str, str2, z);
		str = TAG;
		str2 = "isScreenshotViewNull";
		if (this.screenshotView == null) {
			z = true;
		} else {
			z = false;
		}
		diagnostics.addDiagnostic(str, str2, z);
		String str3 = TAG;
		str = "isUnlockToContinueViewNull";
		if (this.unlockToContinueView != null) {
			z2 = false;
		}
		diagnostics.addDiagnostic(str3, str, z2);
		if (this.rootView != null) {
			diagnostics.addDiagnostic(TAG, "rootView.hashCode()", this.rootView.hashCode());
		}
		if (this.widgetView != null) {
			diagnostics.addDiagnostic(TAG, "widgetView.hashCode()", this.widgetView.hashCode());
		}
		if (this.fullscreenView != null) {
			diagnostics.addDiagnostic(TAG, "fullscreenView.hashCode()", this.fullscreenView.hashCode());
		}
		if (this.screenshotView != null) {
			diagnostics.addDiagnostic(TAG, "screenshotView.hashCode()", this.screenshotView.hashCode());
		}
		if (this.unlockToContinueView != null) {
			diagnostics.addDiagnostic(TAG, "unlockToContinueView.hashCode()", this.unlockToContinueView.hashCode());
		}
	}

	void clearFullscreenView() {
		this.fullscreenView.removeAllViews();
		this.fullscreenView.addView(new View(this.context));
	}

	void addToFullscreenView(View view) {
		this.fullscreenView.removeAllViews();
		this.fullscreenView.addView(view);
	}

	void enableTouchOnRootView(boolean enableTouch) {
		WindowManager wm = (WindowManager) this.context.getSystemService("window");
		LayoutParams layoutParams = (LayoutParams) this.rootView.getLayoutParams();
		boolean isTouchEnabled = (layoutParams.flags & 16) == 0;
		if (isTouchEnabled && !enableTouch) {
			layoutParams.flags |= 16;
			wm.updateViewLayout(this.rootView, layoutParams);
		} else if (!isTouchEnabled && enableTouch) {
			layoutParams.flags &= -17;
			wm.updateViewLayout(this.rootView, layoutParams);
		}
	}

	void resetPageAdapter() {
		this.pagerAdapter.reset();
		int count = this.pagerAdapter.getCount();
		this.pagesView.setNumPages(count);
		this.pagesView.setSelectedPage(0);
		this.pagerView.setCurrentItem(0);
		if (count > 0) {
			performAction(WidgetAction.actionShow);
		} else {
			performAction(WidgetAction.actionHide);
		}
	}

	View duplicateView(View view) {
		Bitmap b = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Config.ARGB_8888);
		view.draw(new Canvas(b));
		ImageView imageView = new ImageView(this.context);
		imageView.setImageBitmap(b);
		return imageView;
	}

	void safeSetVisibility(View view, int visibility) {
		if (visibility == 0) {
			view.setAlpha(DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
		} else {
			view.setAlpha(0.0f);
		}
	}
}

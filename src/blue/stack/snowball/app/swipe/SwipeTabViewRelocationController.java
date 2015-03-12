package blue.stack.snowball.app.swipe;

import blue.stack.snowball.app.R;
import android.animation.TimeAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Build;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.LinearInterpolator;
import android.widget.RelativeLayout;
import blue.stack.snowball.app.logging.EventLogger;
import blue.stack.snowball.app.logging.EventLoggerManager;
import blue.stack.snowball.app.settings.Settings;
import blue.stack.snowball.app.swipe.ui.YettiTabLayout;
import blue.stack.snowball.app.tools.UIUtilities;
import blue.stack.snowball.app.ui.anim.GeometryUtils;
import blue.stack.snowball.app.ui.anim.LinearTranslateTimeListener;
import blue.stack.snowball.app.ui.anim.SpringTranslateTimeListener;

import com.android.volley.DefaultRetryPolicy;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mobeta.android.dslv.DragSortController;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
@Singleton
public class SwipeTabViewRelocationController {
	private static final long FADE_OUT_DURATION = 400;
	private static final float GRAVITY_FORCE = 20000.0f;
	private static final long MERGE_WITH_MOTION_DURATION = 150;
	private int FAR_AWAY;
	private final float TRASHCAN_ENLARGE_SCALE;
	@Inject
	Context context;
	private float currentX;
	private float currentY;
	@Inject
	EventLoggerManager eventLoggerManager;
	private GestureDetector gestureDetector;
	private SwipeTabRelocationListener listener;
	private Boolean overTrashCan;
	private float previousTouchX;
	private float previousTouchY;
	private eRelocationState relocationState;
	private RelativeLayout renderView;
	private int screenHeight;
	private int screenWidth;
	@Inject
	Settings settings;
	private int statusBarHeight;
	private YettiTabLayout swipeTab;
	TimeAnimator translateTimeAnimator;
	LinearTranslateTimeListener translateTimeListener;
	private View trashCan;
	private ViewGroup trashFooter;

	class AnonymousClass_5 implements AnimationListener {
		final/* synthetic */TimeAnimator val$timeAnimator;

		AnonymousClass_5(TimeAnimator timeAnimator) {
			this.val$timeAnimator = timeAnimator;
		}

		@Override
		public void onAnimationStart(Animation animation) {
		}

		@Override
		public void onAnimationEnd(Animation animation) {
			Log.d("springTimeListener", "onAnimationEnd");
			this.val$timeAnimator.cancel();
			if (SwipeTabViewRelocationController.this.listener != null) {
				SwipeTabViewRelocationController.this.listener.onFinishRelocation();
			}
		}

		@Override
		public void onAnimationRepeat(Animation animation) {
		}
	}

	public static interface SwipeTabRelocationListener {
		void onBeginHoverOverTrash();

		void onBeginRelocation();

		void onBeginTrashing();

		void onFinishRelocation();

		void onFinishTrashing();
	}

	enum eRelocationState {
		Relocating,
		Finishing,
		Trashing,
		None
	}

	public SwipeTabViewRelocationController() {
		this.TRASHCAN_ENLARGE_SCALE = 1.25f;
		this.FAR_AWAY = 100000;
		this.relocationState = eRelocationState.None;
		this.overTrashCan = Boolean.valueOf(false);
	}

	void init() {
		WindowManager windowManager = (WindowManager) this.context.getSystemService("window");
		Display display = windowManager.getDefaultDisplay();
		Point size = new Point();
		Point size2 = new Point();
		display.getCurrentSizeRange(size, size2);
		this.screenWidth = size.x;
		this.screenHeight = size2.x;
		this.statusBarHeight = size2.x - size2.y;
		LayoutParams params = new LayoutParams(-2, -2, 2007, 16843544, -3);
		params.gravity = 51;
		params.width = -1;
		params.height = -1;
		params.x = this.FAR_AWAY;
		params.y = 0;
		LayoutInflater inflater = (LayoutInflater) this.context.getSystemService("layout_inflater");
		this.renderView = new RelativeLayout(this.context);
		this.trashFooter = (ViewGroup) inflater.inflate(R.layout.swipe_relocation_trash_footer, null);
		this.trashCan = this.trashFooter.findViewById(R.id.swipe_relocator_trash);
		this.swipeTab = (YettiTabLayout) inflater.inflate(R.layout.yetti_tab_layout, null);
		this.swipeTab.animate().setDuration(0).alpha(0.0f);
		windowManager.addView(this.renderView, params);
		this.renderView.addView(this.swipeTab, -2, -2);
		this.renderView.addView(this.trashFooter, -1, -1);
		resetTrash();
		setupGestureDetector();
	}

	public void stop() {
		((WindowManager) this.context.getSystemService("window")).removeView(this.renderView);
		this.renderView = null;
		this.swipeTab = null;
		this.trashFooter = null;
	}

	private void setupGestureDetector() {
		this.gestureDetector = new GestureDetector(this.context, new OnGestureListener() {
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
				if (SwipeTabViewRelocationController.this.relocationState == eRelocationState.Relocating
						&& !SwipeTabViewRelocationController.this.overTrashCan.booleanValue()) {
					SwipeTabViewRelocationController.this.endRelocateTab(new PointF(v, v2));
				}
				return false;
			}
		});
	}

	public Boolean onTouchEvent(MotionEvent event) {
		this.gestureDetector.onTouchEvent(event);
		this.previousTouchX = this.currentX;
		this.previousTouchY = this.currentY;
		this.currentX = event.getRawX();
		this.currentY = event.getRawY();
		switch (event.getActionMasked()) {
		case DragSortController.ON_DRAG /* 1 */:
		case 3/* 3 */:
			if (this.relocationState == eRelocationState.Relocating) {
				if (this.overTrashCan.booleanValue()) {
					beginTrashingView();
				} else {
					endRelocateTab(new PointF(0.0f, 0.0f));
				}
				return Boolean.valueOf(true);
			}
			break;
		case DragSortController.ON_LONG_PRESS /* 2 */:
			if (this.relocationState == eRelocationState.Relocating) {
				updateRelocateTab(this.currentX, this.currentY);
				return Boolean.valueOf(true);
			}
			break;
		}
		return Boolean.valueOf(false);
	}

	public YettiTabLayout getSwipeTab() {
		return this.swipeTab;
	}

	public RelativeLayout getRenderView() {
		return this.renderView;
	}

	public void setTabRelocationListener(SwipeTabRelocationListener listener) {
		this.listener = listener;
	}

	void beginHoverOverTrash() {
		if (!this.overTrashCan.booleanValue()) {
			this.overTrashCan = Boolean.valueOf(true);
			this.trashCan.animate().scaleX(1.25f).scaleY(1.25f).setInterpolator(new LinearInterpolator())
					.setDuration(100);
			if (this.listener != null) {
				this.listener.onBeginHoverOverTrash();
			}
		}
	}

	void endHoverOverTrash() {
		if (this.overTrashCan.booleanValue()) {
			this.overTrashCan = Boolean.valueOf(false);
			this.trashCan.animate().scaleX(DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
					.scaleY(DefaultRetryPolicy.DEFAULT_BACKOFF_MULT).setDuration(100);
		}
	}

	private void beginTrashingView() {
		EventLogger eventLogger = this.eventLoggerManager.getEventLogger();
		this.relocationState = eRelocationState.Trashing;
		this.overTrashCan = Boolean.valueOf(false);
		if (this.listener != null) {
			this.listener.onBeginTrashing();
		}
		this.trashCan.animate().scaleY(0.0f).setInterpolator(new LinearInterpolator());
		this.trashCan.animate().scaleX(0.0f).setInterpolator(new LinearInterpolator()).withEndAction(new Runnable() {
			@Override
			public void run() {
				SwipeTabViewRelocationController.this.finishRelocation();
				if (SwipeTabViewRelocationController.this.listener != null) {
					SwipeTabViewRelocationController.this.listener.onFinishTrashing();
				}
			}
		});
	}

	private PointF getTargetFromMotionEvent(float rawX, float rawY) {
		float iconSize = this.context.getResources().getDimension(R.dimen.tab_width);
		return new PointF(rawX - (iconSize / 2.0f), rawY - iconSize);
	}

	private void setVisibility(Boolean visible) {
		WindowManager windowManager = (WindowManager) this.context.getSystemService("window");
		LayoutParams params = (LayoutParams) this.renderView.getLayoutParams();
		if (visible.booleanValue()) {
			params.x = 0;
		} else {
			params.x = this.FAR_AWAY;
		}
		windowManager.updateViewLayout(this.renderView, params);
	}

	public void finishRelocation() {
		this.overTrashCan = Boolean.valueOf(false);
		this.relocationState = eRelocationState.None;
		this.swipeTab.animate().setDuration(0).alpha(0.0f);
		setVisibility(Boolean.valueOf(false));
		resetTrash();
	}

	private void resetTrash() {
		this.trashCan.setScaleX(0.0f);
		this.trashCan.setScaleY(0.0f);
		this.trashFooter.findViewById(R.id.gradiant_footer).setAlpha(0.0f);
	}

	public void startRelocation(MotionEvent motionEvent, int unreadCount) {
		if (!isInProgress()) {
			Log.d("startRelocation", "startRelocation");
			this.relocationState = eRelocationState.Relocating;
			setVisibility(Boolean.valueOf(true));
			this.swipeTab.playAnimation("IDLE");
			this.swipeTab.setUnreadCount(unreadCount);
			int iconSize = (int) this.context.getResources().getDimension(R.dimen.tab_width);
			int top = this.settings.getTabVerticalOffset()
					- ((iconSize - ((int) this.context.getResources().getDimension(R.dimen.tab_height))) / 2);
			int left = this.settings.getTabGravity() == 3 ? 0 : this.screenWidth - iconSize;
			float rawX = motionEvent.getRawX();
			float rawY = motionEvent.getRawY();
			this.translateTimeAnimator = new TimeAnimator();
			this.translateTimeListener = new LinearTranslateTimeListener(this.swipeTab, new PointF(left, top),
					getTargetFromMotionEvent(rawX, rawY), 150.0f);
			this.translateTimeListener.setAnimationListener(new AnimationListener() {
				@Override
				public void onAnimationStart(Animation animation) {
				}

				@Override
				public void onAnimationEnd(Animation animation) {
					Log.d("translateTimeListener", "onAnimationEnd");
					SwipeTabViewRelocationController.this.translateTimeAnimator.cancel();
					SwipeTabViewRelocationController.this.translateTimeListener = null;
				}

				@Override
				public void onAnimationRepeat(Animation animation) {
				}
			});
			this.translateTimeAnimator.setTimeListener(this.translateTimeListener);
			this.translateTimeAnimator.start();
			resetTrash();
			this.trashCan.animate().scaleY(DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
					.scaleX(DefaultRetryPolicy.DEFAULT_BACKOFF_MULT).setDuration(300);
			this.trashFooter.findViewById(R.id.gradiant_footer).animate()
					.alpha(DefaultRetryPolicy.DEFAULT_BACKOFF_MULT).setDuration(300);
			this.swipeTab.clearAnimation();
			UIUtilities.delayOneFrame(new Runnable() {
				@Override
				public void run() {
					SwipeTabViewRelocationController.this.swipeTab.animate().setDuration(0)
							.alpha(DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
					if (SwipeTabViewRelocationController.this.listener != null) {
						SwipeTabViewRelocationController.this.listener.onBeginRelocation();
					}
				}
			});
		}
	}

	private void updateRelocateTab(float x, float y) {
		PointF targetPoint = getTargetFromMotionEvent(x, y);
		if (this.translateTimeListener != null) {
			this.translateTimeListener.setTarget(targetPoint);
		} else {
			this.swipeTab.setX(targetPoint.x);
			this.swipeTab.setY(targetPoint.y);
		}
		Rect hitRect = new Rect();
		this.trashCan.getHitRect(hitRect);
		boolean tempOverTrashCan = hitRect.contains((int) x, (int) y);
		if (!this.overTrashCan.booleanValue() && tempOverTrashCan) {
			beginHoverOverTrash();
		} else if (this.overTrashCan.booleanValue() && !tempOverTrashCan) {
			endHoverOverTrash();
		}
	}

	private void endRelocateTab(PointF veloctiy) {
		Log.d("SwipeTabRelocator", "endRelocateTab");
		if (this.translateTimeAnimator != null) {
			this.translateTimeAnimator.cancel();
			Log.d("SwipeTabRelocator", "Canceling translate anim");
		}
		this.relocationState = eRelocationState.Finishing;
		int finalGravity = -1;
		float tabSize = this.context.getResources().getDimension(R.dimen.tab_height);
		PointF pointOnScreenRim = new PointF();
		PointF position = new PointF(this.swipeTab.getX(), this.swipeTab.getY());
		float height = (veloctiy.x * veloctiy.x) / 40000.0f;
		if (position.x < (this.screenWidth) / 2.0f) {
			finalGravity = 3;
			if (veloctiy.x > 0.0f && height + position.x > (this.screenWidth) / 2.0f) {
				finalGravity = 5;
			}
		} else if (position.x >= (this.screenWidth) / 2.0f) {
			finalGravity = 5;
			if (veloctiy.x < 0.0f && position.x - height < (this.screenWidth) / 2.0f) {
				finalGravity = 3;
			}
		}
		float Y0;
		if (finalGravity == 3) {
			Y0 = position.x;
			if (Y0 < 0.0f) {
				Y0 = 0.0f;
			}
			pointOnScreenRim.y = position.y
					+ (veloctiy.y * ((float) (((veloctiy.x) + Math.sqrt((veloctiy.x * veloctiy.x)
							+ ((2.0f * Y0) * GRAVITY_FORCE))) / 20000.0d)));
			pointOnScreenRim.x = 0.0f;
		} else {
			Y0 = ((this.screenWidth) - position.x) - tabSize;
			if (Y0 < 0.0f) {
				Y0 = 0.0f;
			}
			pointOnScreenRim.y = position.y
					+ (veloctiy.y * ((float) ((((-veloctiy.x)) + Math.sqrt((veloctiy.x * veloctiy.x)
							+ ((2.0f * Y0) * GRAVITY_FORCE))) / 20000.0d)));
			pointOnScreenRim.x = (this.screenWidth) - tabSize;
		}
		veloctiy = GeometryUtils.scalePoint(veloctiy, 0.01f);
		int verticalOffset = this.settings.getTabVerticalOffset();
		if (pointOnScreenRim == null) {
			if (finalGravity == 3) {
				pointOnScreenRim = new PointF(0.0f, verticalOffset);
			} else {
				pointOnScreenRim = new PointF(this.screenWidth, verticalOffset);
			}
			Log.d("SwipeTabRelocation", "failed to find point on rim");
		}
		if (pointOnScreenRim.y > (this.screenHeight) - tabSize) {
			pointOnScreenRim.y = (this.screenHeight) - tabSize;
		}
		if (pointOnScreenRim.y < (this.statusBarHeight)) {
			pointOnScreenRim.y = this.statusBarHeight + 1;
		}
		this.settings.setTabGravity(finalGravity);
		this.settings.setTabVerticalOffset((int) pointOnScreenRim.y);
		TimeAnimator timeAnimator = new TimeAnimator();
		SpringTranslateTimeListener springTimeListener = new SpringTranslateTimeListener(this.swipeTab, position,
				veloctiy, pointOnScreenRim);
		springTimeListener.setAnimationListener(new AnonymousClass_5(timeAnimator));
		timeAnimator.setTimeListener(springTimeListener);
		timeAnimator.start();
		this.trashCan.animate().scaleY(0.0f).scaleX(0.0f).setDuration(FADE_OUT_DURATION);
		this.trashFooter.findViewById(R.id.gradiant_footer).animate().alpha(0.0f).setDuration(FADE_OUT_DURATION)
				.setInterpolator(new LinearInterpolator());
	}

	public boolean isInProgress() {
		return this.relocationState != eRelocationState.None;
	}
}

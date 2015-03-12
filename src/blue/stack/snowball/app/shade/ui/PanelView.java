package blue.stack.snowball.app.shade.ui;

import java.util.ArrayDeque;
import java.util.Iterator;
//import org.apache.log4j.net.SyslogAppender;
//import org.objectweb.asm.util.Textifier;

import blue.stack.snowball.app.R;
import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ObjectAnimator;
import android.animation.TimeAnimator;
import android.animation.TimeAnimator.TimeListener;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.widget.ExploreByTouchHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import com.android.volley.DefaultRetryPolicy;
//import com.google.inject.internal.guava.primitives._$Ints;
import com.mobeta.android.dslv.DragSortController;

public class PanelView extends RelativeLayout {
	public static final boolean BRAKES = false;
	public static final boolean DEBUG = false;
	public static final boolean DEBUG_NAN = true;
	static float FRACTION_FULLY_EXPANDED = 0.0f;
	public static final String PEEK_COMPLETE = "blue.stack.snowball.app.shade.ui.PanelView.PEEK_COMPLETE";
	public static final String TAG;
	Runnable closePanelsRunnable;
	Handler handler;
	private int[] mAbsPos;
	private float mAccel;
	TimeListener mAnimationCallback;
	PanelBar mBar;
	private float mBrakingSpeedPx;
	private boolean mClosing;
	private float mCollapseAccelPx;
	private float mCollapseBrakingDistancePx;
	private float mCollapseMinDisplayFraction;
	private float mExpandAccelPx;
	private float mExpandBrakingDistancePx;
	private float mExpandMinDisplayFraction;
	private float mExpandedFraction;
	private float mExpandedHeight;
	protected float mFinalTouchY;
	private float mFlingCollapseMinVelocityPx;
	private float mFlingExpandMinVelocityPx;
	private float mFlingGestureMaxOutputVelocityPx;
	private float mFlingGestureMaxXVelocityPx;
	private float mFlingGestureMinDistPx;
	private int mFullHeight;
	private View mHandleView;
	protected float mInitialTouchY;
	private boolean mJustPeeked;
	private ObjectAnimator mPeekAnimator;
	private float mPeekHeight;
	private boolean mRubberbanding;
	private boolean mRubberbandingEnabled;
	private float mSelfCollapseVelocityPx;
	private float mSelfExpandVelocityPx;
	Runnable mStopAnimator;
	private TimeAnimator mTimeAnimator;
	private float mTouchOffset;
	private boolean mTracking;
	private float mVel;
	private FlingTracker mVelocityTracker;
	private String mViewName;

	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	private static class FlingTracker {
		static final boolean DEBUG = false;
		static FlingTracker sTracker;
		final float DECAY;
		final int MAX_EVENTS;
		ArrayDeque<MotionEventCopy> mEventBuf;
		float mVX;
		float mVY;

		private static class MotionEventCopy {
			public long t;
			public float x;
			public float y;

			public MotionEventCopy(float x2, float y2, long eventTime) {
				this.x = x2;
				this.y = y2;
				this.t = eventTime;
			}
		}

		public FlingTracker() {
			this.MAX_EVENTS = 8;
			this.DECAY = 0.75f;
			this.mEventBuf = new ArrayDeque(8);
			this.mVY = 0.0f;
		}

		public void addMovement(MotionEvent event) {
			if (this.mEventBuf.size() == 8) {
				this.mEventBuf.remove();
			}
			this.mEventBuf.add(new MotionEventCopy(event.getX(), event.getY(), event.getEventTime()));
		}

		public void computeCurrentVelocity(long timebase) {
			this.mVY = 0.0f;
			this.mVX = 0.0f;
			MotionEventCopy last = null;
			int i = 0;
			float totalweight = 0.0f;
			float weight = 10.0f;
			Iterator<MotionEventCopy> iter = this.mEventBuf.descendingIterator();
			while (iter.hasNext()) {
				MotionEventCopy event = iter.next();
				if (last != null) {
					float dt = ((float) (event.t - last.t)) / ((float) timebase);
					float dy = event.y - last.y;
					this.mVX += (weight * (event.x - last.x)) / dt;
					this.mVY += (weight * dy) / dt;
					totalweight += weight;
					weight *= 0.75f;
				}
				last = event;
				i++;
			}
			if (totalweight > 0.0f) {
				this.mVX /= totalweight;
				this.mVY /= totalweight;
				return;
			}
			Log.v("FlingTracker", "computeCurrentVelocity warning: totalweight=0", new Throwable());
			this.mVY = 0.0f;
			this.mVX = 0.0f;
		}

		public float getXVelocity() {
			if (Float.isNaN(this.mVX)) {
				Log.v("FlingTracker", "warning: vx=NaN");
				this.mVX = 0.0f;
			}
			return this.mVX;
		}

		public float getYVelocity() {
			if (Float.isNaN(this.mVY)) {
				Log.v("FlingTracker", "warning: vx=NaN");
				this.mVY = 0.0f;
			}
			return this.mVY;
		}

		public void recycle() {
			this.mEventBuf.clear();
		}

		static FlingTracker obtain() {
			if (sTracker == null) {
				sTracker = new FlingTracker();
			}
			return sTracker;
		}
	}

	static {
		TAG = PanelView.class.getSimpleName();
		FRACTION_FULLY_EXPANDED = 0.97f;
	}

	public final void LOG(String fmt, Object... args) {
	}

	public PanelView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.mRubberbandingEnabled = DEBUG_NAN;
		this.mCollapseBrakingDistancePx = 200.0f;
		this.mExpandBrakingDistancePx = 150.0f;
		this.mBrakingSpeedPx = 150.0f;
		this.mExpandedFraction = 0.0f;
		this.mExpandedHeight = 0.0f;
		this.mAbsPos = new int[2];
		this.mFullHeight = 0;
		this.closePanelsRunnable = new Runnable() {
			@Override
			public void run() {
				PanelView.this.mExpandedHeight = 0.0f;
				PanelView.this.mBar.closePanels();
			}
		};
		if (!isInEditMode()) {
			init();
		}
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	void init() {
		this.handler = new Handler(Looper.getMainLooper());
		this.mAnimationCallback = new TimeListener() {
			@Override
			public void onTimeUpdate(TimeAnimator animation, long totalTime, long deltaTime) {
				PanelView.this.animationTick(deltaTime);
			}
		};
		this.mStopAnimator = new Runnable() {
			@Override
			public void run() {
				if (PanelView.this.mTimeAnimator != null && PanelView.this.mTimeAnimator.isStarted()) {
					PanelView.this.LOG("Stop Animator : stopping", new Object[0]);
					PanelView.this.mTimeAnimator.end();
					PanelView.this.mRubberbanding = DEBUG;
					PanelView.this.mClosing = DEBUG;
				}
			}
		};
		this.mTimeAnimator = new TimeAnimator();
		this.mTimeAnimator.setTimeListener(this.mAnimationCallback);
	}

	public void setRubberbandingEnabled(boolean enable) {
		this.mRubberbandingEnabled = enable;
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private void runPeekAnimation() {
		if (!this.mTimeAnimator.isStarted()) {
			if (this.mPeekAnimator == null) {
				this.mPeekAnimator = ObjectAnimator.ofFloat(this, "expandedHeight", new float[] { this.mPeekHeight })
						.setDuration(250);
				this.mPeekAnimator.addListener(new AnimatorListener() {
					@Override
					public void onAnimationStart(Animator animation) {
					}

					@Override
					public void onAnimationEnd(Animator animation) {
						PanelView.this.getContext().sendBroadcast(new Intent(PEEK_COMPLETE));
					}

					@Override
					public void onAnimationCancel(Animator animation) {
					}

					@Override
					public void onAnimationRepeat(Animator animation) {
					}
				});
			}
			this.mPeekAnimator.start();
		}
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private void animationTick(long dtms) {
		boolean z = DEBUG_NAN;
		if (!this.mTimeAnimator.isStarted()) {
			this.mTimeAnimator = new TimeAnimator();
			this.mTimeAnimator.setTimeListener(this.mAnimationCallback);
			if (this.mPeekAnimator != null) {
				this.mPeekAnimator.cancel();
			}
			this.mTimeAnimator.start();
			boolean z2 = (!this.mRubberbandingEnabled || this.mExpandedHeight <= getFullHeight() || this.mVel < (-this.mFlingGestureMinDistPx)) ? DEBUG
					: DEBUG_NAN;
			this.mRubberbanding = z2;
			if (this.mRubberbanding) {
				this.mClosing = DEBUG_NAN;
			} else if (this.mVel == 0.0f) {
				if (this.mFinalTouchY / getFullHeight() >= 0.5f) {
					z = DEBUG;
				}
				this.mClosing = z;
			} else {
				if (this.mExpandedHeight <= 0.0f || this.mVel >= 0.0f) {
					z = DEBUG;
				}
				this.mClosing = z;
			}
		} else if (dtms > 0) {
			float dt = (dtms) * 0.001f;
			float fh = getFullHeight();
			this.mAccel = this.mClosing ? -this.mCollapseAccelPx : this.mExpandAccelPx;
			this.mVel += this.mAccel * dt;
			if (DEBUG) {// TODO notive debug!=null
				if (this.mClosing && this.mVel > (-this.mBrakingSpeedPx)) {
					this.mVel = -this.mBrakingSpeedPx;
				} else if (!this.mClosing && this.mVel < this.mBrakingSpeedPx) {
					this.mVel = this.mBrakingSpeedPx;
				}
			} else if (this.mClosing && this.mVel > (-this.mFlingCollapseMinVelocityPx)) {
				this.mVel = -this.mFlingCollapseMinVelocityPx;
			} else if (!this.mClosing && this.mVel > this.mFlingGestureMaxOutputVelocityPx) {
				this.mVel = this.mFlingGestureMaxOutputVelocityPx;
			}
			float h = this.mExpandedHeight + (this.mVel * dt);
			if (this.mRubberbanding && h < fh) {
				h = fh;
			}
			setExpandedHeightInternal(h);
			this.mBar.panelExpansionChanged(this, this.mExpandedFraction);
			if (this.mVel == 0.0f
					|| ((this.mClosing && getTargetExpandedHeight() == 0) || ((this.mRubberbanding || !this.mClosing) && this.mExpandedHeight == fh))) {
				this.handler.post(this.mStopAnimator);
				if (this.mClosing && getTargetExpandedHeight() == 0) {
					this.handler.post(this.closePanelsRunnable);
				}
			}
		} else {
			Log.v(TAG, "animationTick called with dtms=" + dtms + "; nothing to do (h=" + this.mExpandedHeight + " v="
					+ this.mVel + ")");
		}
	}

	private void loadDimens() {
		Resources res = getContext().getResources();
		this.mSelfExpandVelocityPx = res.getDimension(R.dimen.self_expand_velocity);
		this.mSelfCollapseVelocityPx = res.getDimension(R.dimen.self_collapse_velocity);
		this.mFlingExpandMinVelocityPx = res.getDimension(R.dimen.fling_expand_min_velocity);
		this.mFlingCollapseMinVelocityPx = res.getDimension(R.dimen.fling_collapse_min_velocity);
		this.mFlingGestureMinDistPx = res.getDimension(R.dimen.fling_gesture_min_dist);
		this.mCollapseMinDisplayFraction = res.getFraction(R.fraction.collapse_min_display_fraction, 1, 1);
		this.mExpandMinDisplayFraction = res.getFraction(R.fraction.expand_min_display_fraction, 1, 1);
		this.mExpandAccelPx = res.getDimension(R.dimen.expand_accel);
		this.mCollapseAccelPx = res.getDimension(R.dimen.collapse_accel);
		this.mFlingGestureMaxXVelocityPx = res.getDimension(R.dimen.fling_gesture_max_x_velocity);
		this.mFlingGestureMaxOutputVelocityPx = res.getDimension(R.dimen.fling_gesture_max_output_velocity);
		this.mPeekHeight = ((getPaddingBottom()) + res.getDimension(R.dimen.peek_height))
				- (this.mHandleView == null ? 0 : this.mHandleView.getPaddingTop());
	}

	private void trackMovement(MotionEvent event) {
		float deltaX = event.getRawX() - event.getX();
		float deltaY = event.getRawY() - event.getY();
		event.offsetLocation(deltaX, deltaY);
		if (this.mVelocityTracker != null) {
			this.mVelocityTracker.addMovement(event);
		}
		event.offsetLocation(-deltaX, -deltaY);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return this.mHandleView.dispatchTouchEvent(event);
	}

	public void setHandleView(View handleView) {
		this.mHandleView = handleView;
		loadDimens();
		if (this.mHandleView != null) {
			this.mHandleView.setOnTouchListener(new OnTouchListener() {
				@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					float y = event.getY();
					float rawY = event.getRawY();
					PanelView.this.getLocationOnScreen(PanelView.this.mAbsPos);
					switch (event.getAction()) {
					case 0 /* 0 */:
						PanelView.this.mTracking = DEBUG_NAN;
						PanelView.this.mHandleView.setPressed(DEBUG_NAN);
						PanelView.this.postInvalidate();
						PanelView.this.mInitialTouchY = y;
						PanelView.this.mVelocityTracker = FlingTracker.obtain();
						PanelView.this.trackMovement(event);
						PanelView.this.mTimeAnimator.cancel();
						PanelView.this.mBar.onTrackingStarted(PanelView.this);
						PanelView.this.mTouchOffset = (rawY - (PanelView.this.mAbsPos[1]))
								- PanelView.this.getExpandedHeight();
						if (PanelView.this.mExpandedHeight == 0.0f) {
							PanelView.this.mJustPeeked = DEBUG_NAN;
							PanelView.this.runPeekAnimation();
							break;
						}
						break;
					case DragSortController.ON_DRAG /* 1 */:
					case 3 /* 3 */:
						PanelView.this.mFinalTouchY = y;
						PanelView.this.mTracking = DEBUG;
						PanelView.this.mHandleView.setPressed(DEBUG);
						PanelView.this.postInvalidate();
						PanelView.this.mBar.onTrackingStopped(PanelView.this);
						PanelView.this.trackMovement(event);
						float vel = 0.0f;
						boolean negative = DEBUG;
						if (PanelView.this.mVelocityTracker != null) {
							PanelView.this.mVelocityTracker.computeCurrentVelocity(1000);
							float yVel = PanelView.this.mVelocityTracker.getYVelocity();
							negative = yVel < 0.0f ? DEBUG_NAN : DEBUG;
							float xVel = PanelView.this.mVelocityTracker.getXVelocity();
							if (xVel < 0.0f) {
								xVel = -xVel;
							}
							if (xVel > PanelView.this.mFlingGestureMaxXVelocityPx) {
								xVel = PanelView.this.mFlingGestureMaxXVelocityPx;
							}
							vel = (float) Math.hypot(yVel, xVel);
							if (vel > PanelView.this.mFlingGestureMaxOutputVelocityPx) {
								vel = PanelView.this.mFlingGestureMaxOutputVelocityPx;
							}
							PanelView.this.mVelocityTracker.recycle();
							PanelView.this.mVelocityTracker = null;
						}
						if (Math.abs(PanelView.this.mFinalTouchY - PanelView.this.mInitialTouchY) < PanelView.this.mFlingGestureMinDistPx
								|| vel < PanelView.this.mFlingExpandMinVelocityPx) {
							vel = 0.0f;
						}
						if (negative) {
							vel = -vel;
						}
						PanelView.this.fling(vel, DEBUG_NAN);
						break;
					case DragSortController.ON_LONG_PRESS /* 2 */:
						float h = (rawY - (PanelView.this.mAbsPos[1])) - PanelView.this.mTouchOffset;
						if (h > PanelView.this.mPeekHeight) {
							if (PanelView.this.mPeekAnimator != null && PanelView.this.mPeekAnimator.isStarted()) {
								PanelView.this.mPeekAnimator.cancel();
							}
							PanelView.this.mJustPeeked = DEBUG;
						}
						if (!PanelView.this.mJustPeeked) {
							PanelView.this.setExpandedHeightInternal(h);
							PanelView.this.mBar.panelExpansionChanged(PanelView.this, PanelView.this.mExpandedFraction);
						}
						PanelView.this.trackMovement(event);
						break;
					}
					return DEBUG_NAN;
				}
			});
		}
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
	}

	public void fling(float vel, boolean always) {
		this.mVel = vel;
		if (always || this.mVel != 0.0f) {
			animationTick(0);
		}
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		this.mViewName = "PanelView";
	}

	public String getName() {
		return this.mViewName;
	}

	public View getHandle() {
		return this.mHandleView;
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		int newHeight = getMeasuredHeight();
		if (newHeight != this.mFullHeight) {
			this.mFullHeight = newHeight;
			if (!(this.mTracking || this.mRubberbanding || this.mTimeAnimator.isStarted()
					|| this.mExpandedHeight <= 0.0f || this.mExpandedHeight == (this.mFullHeight))) {
				this.mExpandedHeight = this.mFullHeight;
			}
		}
		setMeasuredDimension(widthMeasureSpec,
				MeasureSpec.makeMeasureSpec(getTargetExpandedHeight(), ExploreByTouchHelper.INVALID_ID));
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public void setExpandedHeight(float height) {
		this.mRubberbanding = DEBUG;
		if (this.mTimeAnimator.isStarted()) {
			this.handler.post(this.mStopAnimator);
		}
		setExpandedHeightInternal(height);
		this.mBar.panelExpansionChanged(this, this.mExpandedFraction);
	}

	private int getTargetExpandedHeight() {
		int targetHeight = (int) this.mExpandedHeight;
		if (this.mTouchOffset < 0.0f) {
			targetHeight = (int) (this.mExpandedHeight + this.mTouchOffset);
		}
		if (this.mExpandedHeight == (this.mFullHeight)) {
			targetHeight = this.mFullHeight;
		}
		return targetHeight < 0 ? 0 : targetHeight;
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
	}

	public void setExpandedHeightInternal(float h) {
		float f = 0.0f;
		if (Float.isNaN(h)) {
			Log.v(TAG, "setExpandedHeightInternal: warning: h=NaN, using 0 instead", new Throwable());
			h = 0.0f;
		}
		float fh = getFullHeight();
		if (fh == 0.0f) {
		}
		if (h < 0.0f) {
			h = 0.0f;
		}
		if (!(this.mRubberbandingEnabled && (this.mTracking || this.mRubberbanding)) && h > fh) {
			h = fh;
		}
		this.mExpandedHeight = h;
		((PanelHolder) getParent()).setExpandedHeight(getTargetExpandedHeight());
		requestLayout();
		if (fh != 0.0f) {
			f = h / fh;
		}
		this.mExpandedFraction = Math.min(DefaultRetryPolicy.DEFAULT_BACKOFF_MULT, f);
	}

	private float getFullHeight() {
		if (this.mFullHeight <= 0) {
			measure(MeasureSpec.makeMeasureSpec(-2, 1073741824), MeasureSpec.makeMeasureSpec(-2, 1073741824));
		}
		return this.mFullHeight;
	}

	public void setExpandedFraction(float frac) {
		if (Float.isNaN(frac)) {
			Log.v(TAG, "setExpandedFraction: frac=NaN, using 0 instead", new Throwable());
			frac = 0.0f;
		}
		setExpandedHeight(getFullHeight() * frac);
	}

	public float getExpandedHeight() {
		return this.mExpandedHeight;
	}

	public float getExpandedFraction() {
		return this.mExpandedFraction;
	}

	public boolean isFullyExpanded() {
		return this.mExpandedHeight >= getFullHeight() ? DEBUG_NAN : DEBUG;
	}

	public boolean isNearlyFullyExpanded() {
		return this.mExpandedFraction >= FRACTION_FULLY_EXPANDED ? DEBUG_NAN : DEBUG;
	}

	public boolean isFullyCollapsed() {
		return this.mExpandedHeight <= 0.0f ? DEBUG_NAN : DEBUG;
	}

	public boolean isCollapsing() {
		return this.mClosing;
	}

	public void setBar(PanelBar panelBar) {
		this.mBar = panelBar;
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public void collapse() {
		if (!isFullyCollapsed()) {
			this.mTimeAnimator.cancel();
			this.mClosing = DEBUG_NAN;
			this.mRubberbanding = DEBUG;
			fling(-this.mSelfCollapseVelocityPx, DEBUG_NAN);
		}
	}

	public void expand() {
		if (isFullyCollapsed()) {
			this.mBar.startOpeningPanel(this);
			fling(this.mSelfExpandVelocityPx, DEBUG_NAN);
		}
	}

	public void cancelClosePanelCalls() {
		this.handler.removeCallbacks(this.closePanelsRunnable);
		this.handler.removeCallbacks(this.mStopAnimator);
	}

	// public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
	// String str;
	// String str2 =
	// "[PanelView(%s): expandedHeight=%f fullHeight=%f closing=%s tracking=%s rubberbanding=%s justPeeked=%s peekAnim=%s%s timeAnim=%s%s]";
	// Object[] objArr = new Object[11];
	// objArr[0] = getClass().getSimpleName();
	// objArr[1] = Float.valueOf(getExpandedHeight());
	// objArr[2] = Float.valueOf(getFullHeight());
	// if (this.mClosing) {
	// str = "T";
	// } else {
	// str = "f";
	// }
	// objArr[3] = str;
	// if (this.mTracking) {
	// str = "T";
	// } else {
	// str = "f";
	// }
	// objArr[4] = str;
	// if (this.mRubberbanding) {
	// str = "T";
	// } else {
	// str = "f";
	// }
	// objArr[5] = str;
	// if (this.mJustPeeked) {
	// str = "T";
	// } else {
	// str = "f";
	// }
	// objArr[6] = str;
	// objArr[7] = this.mPeekAnimator;
	// str = (this.mPeekAnimator == null || !this.mPeekAnimator.isStarted()) ?
	// BuildConfig.FLAVOR : " (started)";
	// objArr[8] = str;
	// objArr[9] = this.mTimeAnimator;
	// str = (this.mTimeAnimator == null || !this.mTimeAnimator.isStarted()) ?
	// BuildConfig.FLAVOR : " (started)";
	// objArr[10] = str;
	// pw.println(String.format(str2, objArr));
	// }
}

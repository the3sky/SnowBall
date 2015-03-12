package blue.stack.snowball.app.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.annotation.TargetApi;
import android.graphics.Rect;
import android.os.Build;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewConfiguration;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;

import com.android.volley.DefaultRetryPolicy;
import com.mobeta.android.dslv.DragSortController;

@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class SwipeDismissListViewTouchListener implements OnTouchListener {
	private static final int INVALID_RESOURCE_ID = -1;
	static float SPEED_OF_X_INTERPOLATION = 0.0f;
	private static final String TAG = "SwipeDismissListViewTouchListener";
	private GestureDetector gestureDetector;
	private long mAnimationTime;
	private DismissCallbacks mCallbacks;
	private int mDismissAnimationRefCount;
	private int mDownPosition;
	private View mDownView;
	private float mDownX;
	private float mDownY;
	private ListView mListView;
	private boolean mPaused;
	private List<PendingDismissData> mPendingDismisses;
	private int mSlopX;
	private int mSlopY;
	private int mSwipeToDismissViewResourceId;
	private boolean mSwiping;
	private int mViewWidth;

	class AnonymousClass_2 extends AnimatorListenerAdapter {
		final/* synthetic */int val$position;
		final/* synthetic */View val$view;

		AnonymousClass_2(View view, int i) {
			this.val$view = view;
			this.val$position = i;
		}

		@Override
		public void onAnimationEnd(Animator animation) {
			SwipeDismissListViewTouchListener.this.performDismiss(this.val$view, this.val$position);
		}
	}

	class AnonymousClass_3 extends AnimatorListenerAdapter {
		final/* synthetic */int val$lpHeight;

		AnonymousClass_3(int i) {
			this.val$lpHeight = i;
		}

		@Override
		public void onAnimationEnd(Animator animation) {
			SwipeDismissListViewTouchListener.access$606(SwipeDismissListViewTouchListener.this);
			if (SwipeDismissListViewTouchListener.this.mDismissAnimationRefCount == 0) {
				Collections.sort(SwipeDismissListViewTouchListener.this.mPendingDismisses);
				int[] dismissPositions = new int[SwipeDismissListViewTouchListener.this.mPendingDismisses.size()];
				for (int i = SwipeDismissListViewTouchListener.this.mPendingDismisses.size() - 1; i >= 0; i--) {
					dismissPositions[i] = SwipeDismissListViewTouchListener.this.mPendingDismisses.get(i).position;
				}
				SwipeDismissListViewTouchListener.this.mCallbacks.onDismiss(
						SwipeDismissListViewTouchListener.this.mListView, dismissPositions);
				for (PendingDismissData pendingDismiss : SwipeDismissListViewTouchListener.this.mPendingDismisses) {
					pendingDismiss.view.setAlpha(DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
					pendingDismiss.view.setTranslationX(0.0f);
					LayoutParams lp = pendingDismiss.view.getLayoutParams();
					lp.height = this.val$lpHeight;
					pendingDismiss.view.setLayoutParams(lp);
				}
				SwipeDismissListViewTouchListener.this.mPendingDismisses.clear();
			}
		}
	}

	class AnonymousClass_4 implements AnimatorUpdateListener {
		final/* synthetic */View val$dismissView;
		final/* synthetic */LayoutParams val$lp;

		AnonymousClass_4(LayoutParams layoutParams, View view) {
			this.val$lp = layoutParams;
			this.val$dismissView = view;
		}

		@Override
		public void onAnimationUpdate(ValueAnimator valueAnimator) {
			this.val$lp.height = ((Integer) valueAnimator.getAnimatedValue()).intValue();
			this.val$dismissView.setLayoutParams(this.val$lp);
		}
	}

	public static interface DismissCallbacks {
		boolean canDismiss(int i);

		void onDismiss(ListView listView, int[] iArr);
	}

	private class GestureRecognizer extends SimpleOnGestureListener {
		static final float flingVelocityX = 8000.0f;

		private GestureRecognizer() {
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
			if (Math.abs(velocityX) > flingVelocityX && SwipeDismissListViewTouchListener.this.mDownView != null
					&& SwipeDismissListViewTouchListener.this.mSwiping) {
				boolean dismissRight = velocityX > 0.0f;
				if ((dismissRight && SwipeDismissListViewTouchListener.this.mDownView.getTranslationX() > 0.0f)
						|| (!dismissRight && SwipeDismissListViewTouchListener.this.mDownView.getTranslationX() < 0.0f)) {
					SwipeDismissListViewTouchListener.this.dismiss(SwipeDismissListViewTouchListener.this.mDownView,
							SwipeDismissListViewTouchListener.this.mDownPosition, dismissRight);
					return true;
				}
			}
			return false;
		}
	}

	class PendingDismissData implements Comparable<PendingDismissData> {
		public int position;
		public View view;

		public PendingDismissData(int position, View view) {
			this.position = position;
			this.view = view;
		}

		@Override
		public int compareTo(PendingDismissData other) {
			return other.position - this.position;
		}
	}

	static/* synthetic */int access$606(SwipeDismissListViewTouchListener x0) {
		int i = x0.mDismissAnimationRefCount - 1;
		x0.mDismissAnimationRefCount = i;
		return i;
	}

	public SwipeDismissListViewTouchListener(ListView listView, DismissCallbacks callbacks) {
		this.mViewWidth = 1;
		this.mPendingDismisses = new ArrayList();
		this.mDismissAnimationRefCount = 0;
		ViewConfiguration vc = ViewConfiguration.get(listView.getContext());
		this.mSlopX = vc.getScaledTouchSlop() * 2;
		this.mSlopY = vc.getScaledTouchSlop();
		this.mAnimationTime = listView.getContext().getResources().getInteger(17694720);
		this.mListView = listView;
		this.mCallbacks = callbacks;
		this.mSwipeToDismissViewResourceId = INVALID_RESOURCE_ID;
		this.gestureDetector = new GestureDetector(listView.getContext(), new GestureRecognizer());
	}

	public void setEnabled(boolean enabled) {
		this.mPaused = !enabled;
	}

	public OnScrollListener makeScrollListener() {
		return new OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView absListView, int scrollState) {
				boolean z = true;
				SwipeDismissListViewTouchListener swipeDismissListViewTouchListener = SwipeDismissListViewTouchListener.this;
				if (scrollState == 1) {
					z = false;
				}
				swipeDismissListViewTouchListener.setEnabled(z);
			}

			@Override
			public void onScroll(AbsListView absListView, int i, int i1, int i2) {
			}
		};
	}

	public void dismiss(int position) {
		dismiss(getViewForPosition(position), position, true);
	}

	public void setSwipeToDismissViewResourceId(int swipeToDismissViewResourceId) {
		this.mSwipeToDismissViewResourceId = swipeToDismissViewResourceId;
	}

	@Override
	public boolean onTouch(View view, MotionEvent motionEvent) {
		if (this.gestureDetector.onTouchEvent(motionEvent)) {
			return true;
		}
		int i = this.mViewWidth;

		if (i < 2) {
			this.mViewWidth = this.mListView.getWidth();
		}
		float deltaX;
		switch (motionEvent.getActionMasked()) {
		case 0 /* 0 */:
			if (this.mPaused) {
				return false;
			}
			Rect rect = new Rect();
			int childCount = this.mListView.getChildCount();
			int[] listViewCoords = new int[2];
			this.mListView.getLocationOnScreen(listViewCoords);
			int x = ((int) motionEvent.getRawX()) - listViewCoords[0];
			int y = ((int) motionEvent.getRawY()) - listViewCoords[1];
			int i2 = 0;
			while (i2 < childCount) {
				View child = this.mListView.getChildAt(i2);
				child.getHitRect(rect);
				if (rect.contains(x, y)) {
					View viewToSwipe = null;
					i = this.mSwipeToDismissViewResourceId;
					if (i != -1) {// r0
						viewToSwipe = child.findViewById(this.mSwipeToDismissViewResourceId);
					}
					if (viewToSwipe != null) {
						this.mDownView = viewToSwipe;
					} else {
						this.mDownView = child;
					}
					if (this.mDownView != null) {
						this.mDownX = motionEvent.getRawX();
						this.mDownY = motionEvent.getRawY();
						this.mDownPosition = this.mListView.getPositionForView(this.mDownView);
						if (!this.mCallbacks.canDismiss(this.mDownPosition)) {
							this.mDownView = null;
						}
					}
					return this.mDownView == null ? true : view.onTouchEvent(motionEvent);
				} else {
					i2++;
				}
			}
			if (this.mDownView != null) {
				this.mDownX = motionEvent.getRawX();
				this.mDownY = motionEvent.getRawY();
				this.mDownPosition = this.mListView.getPositionForView(this.mDownView);
				if (this.mCallbacks.canDismiss(this.mDownPosition)) {
					this.mDownView = null;
				}
			}
			if (this.mDownView == null) {
			}
		case DragSortController.ON_DRAG /* 1 */:
			if (this.mDownView != null) {
				deltaX = motionEvent.getRawX() - this.mDownX;
				boolean dismiss = false;
				boolean dismissRight = false;
				if (Math.abs(deltaX) > (this.mViewWidth / 2)) {
					dismiss = true;
					dismissRight = deltaX > 0.0f;
				}
				if (dismiss) {
					dismiss(this.mDownView, this.mDownPosition, dismissRight);
				} else {
					this.mDownView.animate().translationX(0.0f).alpha(DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
							.setDuration(this.mAnimationTime).setListener(null);
				}
			}
			this.mDownX = 0.0f;
			this.mDownView = null;
			this.mDownPosition = INVALID_RESOURCE_ID;
			this.mSwiping = false;
			break;
		case DragSortController.ON_LONG_PRESS /* 2 */:
			if (!(this.mDownView == null || this.mPaused)) {
				deltaX = motionEvent.getRawX() - this.mDownX;
				float deltaY = motionEvent.getRawY() - this.mDownY;
				if (Math.abs(deltaX) > (this.mSlopX)) {
					if (Math.abs(deltaY) < (this.mSlopY)) {
						this.mSwiping = true;
						this.mListView.requestDisallowInterceptTouchEvent(true);
						MotionEvent cancelEvent = MotionEvent.obtain(motionEvent);
						cancelEvent.setAction((motionEvent.getActionIndex() << 8) | 3);
						this.mListView.onTouchEvent(cancelEvent);
						cancelEvent.recycle();
					}
				}
				if (this.mSwiping) {
					int translationX = (int) this.mDownView.getTranslationX();
					int deltaTranslationX = (int) (deltaX - (translationX));
					float interpolationDuration = (Math.abs(deltaTranslationX)) * SPEED_OF_X_INTERPOLATION;
					this.mDownView.setTranslationX(((deltaTranslationX) * 0.2f) + (translationX));
					this.mDownView.animate().setListener(null);
					this.mDownView.animate().cancel();
					this.mDownView.animate().translationX(deltaX).setDuration((long) interpolationDuration);
					this.mDownView.setAlpha(Math.max(
							0.15f,
							Math.min(DefaultRetryPolicy.DEFAULT_BACKOFF_MULT, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
									- ((2.0f * Math.abs(deltaX)) / (this.mViewWidth)))));
					return true;
				}
			}
			break;
		case 3/* 3 */:
			if (this.mDownView != null) {
				this.mDownView.animate().translationX(0.0f).alpha(DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
						.setDuration(this.mAnimationTime).setListener(null);
			}
			this.mDownX = 0.0f;
			this.mDownView = null;
			this.mDownPosition = INVALID_RESOURCE_ID;
			this.mSwiping = false;
			break;
		}
		return false;
	}

	static {
		SPEED_OF_X_INTERPOLATION = 15.0f;
	}

	private void dismiss(View view, int position, boolean dismissRight) {
		this.mDismissAnimationRefCount++;
		if (view == null) {
			this.mCallbacks.onDismiss(this.mListView, new int[] { position });
		} else {
			view.animate().translationX(dismissRight ? (float) this.mViewWidth : (float) (-this.mViewWidth))
					.alpha(0.0f).setDuration(this.mAnimationTime).setListener(new AnonymousClass_2(view, position));
		}
	}

	private View getViewForPosition(int position) {
		int index = position - (this.mListView.getFirstVisiblePosition() - this.mListView.getHeaderViewsCount());
		return (index < 0 || index >= this.mListView.getChildCount()) ? null : this.mListView.getChildAt(index);
	}

	private void performDismiss(View dismissView, int dismissPosition) {
		LayoutParams lp = dismissView.getLayoutParams();
		int originalHeight = dismissView.getHeight();
		int lpHeight = lp.height;
		ValueAnimator animator = ValueAnimator.ofInt(new int[] { originalHeight, 1 }).setDuration(this.mAnimationTime);
		animator.addListener(new AnonymousClass_3(lpHeight));
		animator.addUpdateListener(new AnonymousClass_4(lp, dismissView));
		this.mPendingDismisses.add(new PendingDismissData(dismissPosition, dismissView));
		animator.start();
	}
}

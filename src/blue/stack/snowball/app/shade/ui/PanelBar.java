package blue.stack.snowball.app.shade.ui;

import java.util.ArrayList;
import java.util.Iterator;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import com.android.volley.DefaultRetryPolicy;

public class PanelBar extends RelativeLayout {
	public static final boolean DEBUG = false;
	public static final int STATE_CLOSED = 0;
	public static final int STATE_OPEN = 2;
	public static final int STATE_OPENING = 1;
	public static final String TAG;
	PanelBarListener listener;
	float mPanelExpandedFractionSum;
	PanelHolder mPanelHolder;
	ArrayList<PanelView> mPanels;
	private int mState;
	PanelView mTouchingPanel;
	private boolean mTracking;
	private boolean openPanelCalled;
	private boolean touchable;

	static {
		TAG = PanelBar.class.getSimpleName();
	}

	public static final void LOG(String fmt, Object... args) {
	}

	public void go(int state) {
		this.mState = state;
		if (this.listener != null) {
			switch (this.mState) {
			case STATE_CLOSED /* 0 */:
				this.listener.onPanelClosed();
			case STATE_OPEN /* 2 */:
				this.listener.onPanelOpened();
			default:
			}
		}
	}

	public PanelBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.openPanelCalled = DEBUG;
		this.touchable = true;
		this.mPanels = new ArrayList();
		this.mState = STATE_CLOSED;
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
	}

	public void setListener(PanelBarListener listener) {
		this.listener = listener;
	}

	public void addPanel(PanelView pv) {
		this.mPanels.add(pv);
		pv.setBar(this);
	}

	public void setPanelHolder(PanelHolder ph) {
		if (ph == null) {
			Log.e(TAG, "setPanelHolder: null PanelHolder", new Throwable());
			return;
		}
		ph.setBar(this);
		this.mPanelHolder = ph;
		int N = ph.getChildCount();
		for (int i = STATE_CLOSED; i < N; i++) {
			View v = ph.getChildAt(i);
			if (v != null && (v instanceof PanelView)) {
				addPanel((PanelView) v);
			}
		}
	}

	public float getBarHeight() {
		return getMeasuredHeight();
	}

	public PanelView selectPanelForTouch(MotionEvent touch) {
		if (this.mPanels.size() == STATE_OPENING) {
			return this.mPanels.get(STATE_CLOSED);
		}
		return this.mPanels.get((int) (((this.mPanels.size()) * touch.getX()) / (getMeasuredWidth())));
	}

	public boolean panelsEnabled() {
		return this.touchable;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		boolean z = true;
		String str;
		if (panelsEnabled()) {
			if (event.getAction() == 0) {
				PanelView panel = selectPanelForTouch(event);
				if (panel == null) {
					str = TAG;
					Object[] objArr = new Object[STATE_OPEN];
					objArr[STATE_CLOSED] = Integer.valueOf((int) event.getX());
					objArr[STATE_OPENING] = Integer.valueOf((int) event.getY());
					Log.v(str, String.format("onTouch: no panel for touch at (%d,%d)", objArr));
					this.mTouchingPanel = null;
					return true;
				} else if (panel.isEnabled()) {
					startOpeningPanel(panel);
				} else {
					Log.v(TAG,
							String.format("onTouch: panel (%s) is disabled, ignoring touch at (%d,%d)", new Object[] {
									panel, Integer.valueOf((int) event.getX()), Integer.valueOf((int) event.getY()) }));
					this.mTouchingPanel = null;
					return true;
				}
			}
			if (event.getAction() == STATE_OPENING || event.getAction() == 3) {
				str = TAG;
				Object[] objArr2 = new Object[STATE_OPENING];
				objArr2[STATE_CLOSED] = "break";
				LOG(str, objArr2);
			}
			if (this.mTouchingPanel != null) {
				z = this.mTouchingPanel.onTouchEvent(event);
			}
			return z;
		}
		if (event.getAction() == 0) {
			str = TAG;
			Object[] objArr = new Object[STATE_OPEN];
			objArr[STATE_CLOSED] = Integer.valueOf((int) event.getX());
			objArr[STATE_OPENING] = Integer.valueOf((int) event.getY());
			Log.v(str, String.format("onTouch: all panels disabled, ignoring touch at (%d,%d)", objArr));
		}
		return DEBUG;
	}

	public void startOpeningPanel(PanelView panel) {
		this.mTouchingPanel = panel;
		this.mPanelHolder.setSelectedPanel(this.mTouchingPanel);
		Iterator i$ = this.mPanels.iterator();
		while (i$.hasNext()) {
			PanelView pv = (PanelView) i$.next();
			if (pv != panel) {
				pv.collapse();
			}
		}
		this.openPanelCalled = true;
		if (this.listener != null) {
			this.listener.onBeginOpeningPanel();
		}
		i$ = this.mPanels.iterator();
		while (i$.hasNext()) {
			((PanelView) i$.next()).cancelClosePanelCalls();
		}
	}

	public void panelExpansionChanged(PanelView panel, float frac) {
		boolean fullyClosed = true;
		PanelView fullyOpenedPanel = null;
		this.mPanelExpandedFractionSum = 0.0f;
		Iterator i$ = this.mPanels.iterator();
		while (i$.hasNext()) {
			boolean visible;
			PanelView pv = (PanelView) i$.next();
			if (pv.getVisibility() == 0) {
				visible = true;
			} else {
				visible = DEBUG;
			}
			if (pv.getExpandedHeight() > 0.0f) {
				float f;
				if (this.mState == 0) {
					go(STATE_OPENING);
					onPanelPeeked();
				}
				fullyClosed = DEBUG;
				float thisFrac = pv.getExpandedFraction();
				float f2 = this.mPanelExpandedFractionSum;
				if (visible) {
					f = thisFrac;
				} else {
					f = 0.0f;
				}
				this.mPanelExpandedFractionSum = f + f2;
				if (panel == pv && thisFrac == DefaultRetryPolicy.DEFAULT_BACKOFF_MULT) {
					fullyOpenedPanel = panel;
				}
			}
			if (pv.getExpandedHeight() > 0.0f) {
				if (!visible) {
					pv.setVisibility(STATE_CLOSED);
				}
			} else if (visible) {
				pv.setVisibility(8);
			}
		}
		this.mPanelExpandedFractionSum /= this.mPanels.size();
		if (fullyOpenedPanel != null && !this.mTracking) {
			go(STATE_OPEN);
			onPanelFullyOpened(fullyOpenedPanel);
		} else if (fullyClosed && !this.mTracking && this.mState != 0) {
			go(STATE_CLOSED);
			onAllPanelsCollapsed();
		}
	}

	public void collapseAllPanels(boolean animate) {
		boolean waiting = DEBUG;
		Iterator i$ = this.mPanels.iterator();
		while (i$.hasNext()) {
			PanelView pv = (PanelView) i$.next();
			if (!animate || pv.isFullyCollapsed()) {
				pv.setExpandedFraction(0.0f);
				pv.setVisibility(8);
			} else {
				pv.collapse();
				waiting = true;
			}
		}
		if (!waiting && this.mState != 0) {
			go(STATE_CLOSED);
			onAllPanelsCollapsed();
		}
	}

	public void onPanelPeeked() {
		if (this.listener != null) {
			this.listener.onPanelPeeked();
		}
	}

	public void onAllPanelsCollapsed() {
	}

	public void closePanels() {
		if (this.openPanelCalled) {
			this.openPanelCalled = DEBUG;
			if (this.listener != null) {
				this.listener.onPanelClosed();
			}
		}
	}

	public void onPanelFullyOpened(PanelView openPanel) {
	}

	public void onTrackingStarted(PanelView panel) {
		this.mTracking = true;
	}

	public void onTrackingStopped(PanelView panel) {
		this.mTracking = DEBUG;
		panelExpansionChanged(panel, panel.getExpandedFraction());
	}

	public void setTouchable(boolean touchable) {
		this.touchable = touchable;
	}
}

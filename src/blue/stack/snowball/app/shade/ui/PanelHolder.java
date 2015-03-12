package blue.stack.snowball.app.shade.ui;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import blue.stack.snowball.app.inbox.ui.InboxViewManager;

public class PanelHolder extends RelativeLayout {
	public static final boolean DEBUG_GESTURES = true;
	protected float expandedHeight;
	protected PanelBar mBar;
	protected ArrayList<PanelView> mPanels;
	private int mSelectedPanelIndex;

	public PanelHolder(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.mSelectedPanelIndex = -1;
		this.mPanels = new ArrayList();
		setChildrenDrawingOrderEnabled(DEBUG_GESTURES);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		setChildrenDrawingOrderEnabled(DEBUG_GESTURES);
	}

	public int getPanelIndex(PanelView pv) {
		int N = this.mPanels.size();
		for (int i = 0; i < N; i++) {
			if (pv == (this.mPanels.get(i))) {
				return i;
			}
		}
		return -1;
	}

	public void setSelectedPanel(PanelView pv) {
		this.mSelectedPanelIndex = getPanelIndex(pv);
	}

	@Override
	public void addView(View child) {
		super.addView(child);
		if (child instanceof PanelView) {
			this.mPanels.add((PanelView) child);
		}
	}

	@Override
	protected int getChildDrawingOrder(int childCount, int i) {
		if (this.mSelectedPanelIndex == -1) {
			return i;
		}
		if (i == childCount - 1) {
			return this.mSelectedPanelIndex;
		}
		return i >= this.mSelectedPanelIndex ? i + 1 : i;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case 0 /* 0 */:
			PanelBar.LOG("PanelHolder got touch in open air, closing panels", new Object[0]);
			this.mBar.collapseAllPanels(DEBUG_GESTURES);
			break;
		}
		return false;
	}

	public void setBar(PanelBar panelBar) {
		this.mBar = panelBar;
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getKeyCode() == 4) {
			getContext().sendBroadcast(new Intent(InboxViewManager.ACTION_BACKBUTTON));
		}
		return super.dispatchKeyEvent(event);
	}

	public void setExpandedHeight(float expandedHeight) {
		this.expandedHeight = expandedHeight;
	}
}

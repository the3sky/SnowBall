package blue.stack.snowball.app.swipe;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

public class InboxLayout extends RelativeLayout {
    OnDrawerListener drawerListener;
    private boolean touchable;

    public static interface OnDrawerListener {
        void onBackButton();

        void onHomeButton();
    }

    public InboxLayout(Context context) {
        super(context);
        this.drawerListener = null;
        this.touchable = true;
    }

    public InboxLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.drawerListener = null;
        this.touchable = true;
    }

    public InboxLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.drawerListener = null;
        this.touchable = true;
    }

    public void onCloseSystemDialogs(String reason) {
        if (this.drawerListener != null) {
            this.drawerListener.onHomeButton();
        }
    }

    public void setDrawerListener(OnDrawerListener _drawerListener) {
        this.drawerListener = _drawerListener;
    }

    public void setTouchable(boolean touchable) {
        this.touchable = touchable;
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return this.touchable ? super.onInterceptTouchEvent(ev) : true;
    }

    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == 4 && this.drawerListener != null) {
            this.drawerListener.onBackButton();
        }
        return super.dispatchKeyEvent(event);
    }
}

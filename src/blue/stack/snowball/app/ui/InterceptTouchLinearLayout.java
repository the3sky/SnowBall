package blue.stack.snowball.app.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;

public class InterceptTouchLinearLayout extends LinearLayout {
    OnTouchListener interceptTouchListener;

    public InterceptTouchLinearLayout(Context context) {
        super(context);
    }

    public InterceptTouchLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public InterceptTouchLinearLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public boolean onFilterTouchEventForSecurity(MotionEvent event) {
        boolean handled = false;
        if (this.interceptTouchListener != null) {
            handled = this.interceptTouchListener.onTouch(this, event);
        }
        return handled ? false : super.onFilterTouchEventForSecurity(event);
    }

    public void setOnInterceptTouchListener(OnTouchListener listener) {
        this.interceptTouchListener = listener;
    }
}

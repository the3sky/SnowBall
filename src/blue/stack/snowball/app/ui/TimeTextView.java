package blue.stack.snowball.app.ui;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;
import blue.stack.snowball.app.core.GuiceModule;
import blue.stack.snowball.app.ui.TimerTextRefreshManager.TimerTextRefreshListener;

import com.google.inject.Inject;

public class TimeTextView extends TextView implements TimerTextRefreshListener {
    @Inject
    private TimerTextRefreshManager timerTextRefreshManager;

    public TimeTextView(Context context) {
        super(context);
        init();
    }

    public TimeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TimeTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        if (!isInEditMode()) {
            GuiceModule.get().injectMembers(this);
            updateTime();
        }
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!isInEditMode()) {
            updateTime();
            this.timerTextRefreshManager.addListener(this);
        }
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (!isInEditMode()) {
            this.timerTextRefreshManager.removeListener(this);
        }
    }

    public void onRefreshTime(TimerTextRefreshManager timerTextRefreshManager) {
        updateTime();
    }

    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if (!isInEditMode()) {
            updateTime();
        }
    }

    void updateTime() {
        DateFormat df;
        if (android.text.format.DateFormat.is24HourFormat(getContext())) {
            df = new SimpleDateFormat("H:mm");
        } else {
            df = new SimpleDateFormat("h:mm a");
        }
        setText(df.format(new Date()));
    }
}

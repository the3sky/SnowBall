package blue.stack.snowball.app.ui;

import java.util.Date;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;
import blue.stack.snowball.app.core.GuiceModule;
import blue.stack.snowball.app.tools.UIUtilities;
import blue.stack.snowball.app.ui.TimerTextRefreshManager.TimerTextRefreshListener;

import com.google.inject.Inject;

public class TimerTextRefreshTextView extends TextView implements TimerTextRefreshListener {
    private static final long TIME_BETWEEN_UPDATES_FAST = 10;
    private static final long TIME_BETWEEN_UPDATES_SLOW = 10;
    @Inject
    private TimerTextRefreshManager timerTextRefreshManager;
    private long timestamp;

    public TimerTextRefreshTextView(Context context) {
        super(context);
        init();
    }

    public TimerTextRefreshTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TimerTextRefreshTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        if (!isInEditMode()) {
            GuiceModule.get().injectMembers(this);
        }
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
        setText(UIUtilities.formatTimestamp(timestamp));
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!isInEditMode()) {
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
        Date timeDate = new Date(this.timestamp);
        setText(UIUtilities.formatTimestamp(this.timestamp));
    }

    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if (!isInEditMode()) {
            setText(UIUtilities.formatTimestamp(this.timestamp));
        }
    }
}

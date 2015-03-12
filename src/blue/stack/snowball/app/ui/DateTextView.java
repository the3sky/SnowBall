package blue.stack.snowball.app.ui;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;
import blue.stack.snowball.app.core.GuiceModule;
import blue.stack.snowball.app.ui.TimerTextRefreshManager.TimerTextRefreshListener;

import com.google.inject.Inject;

public class DateTextView extends TextView implements TimerTextRefreshListener {
    @Inject
    private TimerTextRefreshManager timerTextRefreshManager;

    public DateTextView(Context context) {
        super(context);
        init();
    }

    public DateTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DateTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        if (!isInEditMode()) {
            GuiceModule.get().injectMembers(this);
            updateDate();
        }
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!isInEditMode()) {
            updateDate();
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
        updateDate();
    }

    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if (!isInEditMode()) {
            updateDate();
        }
    }

    void updateDate() {
        setText(new SimpleDateFormat("EEEE, MMMM d").format(new Date()));
    }
}

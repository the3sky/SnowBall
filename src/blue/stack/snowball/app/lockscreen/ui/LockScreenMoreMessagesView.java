package blue.stack.snowball.app.lockscreen.ui;

import blue.stack.snowball.app.R;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

public class LockScreenMoreMessagesView extends LinearLayout {
    private final String TAG;

    public LockScreenMoreMessagesView(Context context) {
        super(context);
        this.TAG = "LockScreenMoreMessagesView";
        init(null, 0);
    }

    public LockScreenMoreMessagesView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.TAG = "LockScreenMoreMessagesView";
        init(attrs, 0);
    }

    public LockScreenMoreMessagesView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.TAG = "LockScreenMoreMessagesView";
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        ((LayoutInflater) getContext().getSystemService("layout_inflater")).inflate(R.layout.lockscreen_more_inbox_entry_view, this, true);
    }

    public void setMoreCount(int moreCount) {
        String text;
        if (moreCount == 1) {
            text = getResources().getString(R.string.lockscreen_more_messages_one);
        } else {
            text = String.format(getResources().getString(R.string.lockscreen_more_messages), new Object[]{Integer.valueOf(moreCount)});
        }
        ((TextView) findViewById(R.id.lockscreen_more_messages)).setText(text);
    }
}

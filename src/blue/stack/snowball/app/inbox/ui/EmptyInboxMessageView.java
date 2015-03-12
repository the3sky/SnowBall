package blue.stack.snowball.app.inbox.ui;

import blue.stack.snowball.app.R;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

public class EmptyInboxMessageView extends LinearLayout {
    private final String TAG;

    public EmptyInboxMessageView(Context context) {
        super(context);
        this.TAG = "EmptyInboxMessageView";
        init(null, 0);
    }

    public EmptyInboxMessageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.TAG = "EmptyInboxMessageView";
        init(attrs, 0);
    }

    public EmptyInboxMessageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.TAG = "EmptyInboxMessageView";
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        ((LayoutInflater) getContext().getSystemService("layout_inflater")).inflate(R.layout.empty_inbox_entry_view, this, true);
    }
}

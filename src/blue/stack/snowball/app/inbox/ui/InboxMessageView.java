package blue.stack.snowball.app.inbox.ui;

import blue.stack.snowball.app.R;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import blue.stack.snowball.app.ui.TimerTextRefreshTextView;

public class InboxMessageView extends LinearLayout {
	private final String TAG;
	TextView message;
	View swipeToDismiss;
	TimerTextRefreshTextView timestamp;

	public InboxMessageView(Context context) {
		super(context);
		this.TAG = "InboxMessageView";
		init(null, 0);
	}

	public InboxMessageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.TAG = "InboxMessageView";
		init(attrs, 0);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public InboxMessageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.TAG = "InboxMessageView";
		init(attrs, defStyle);
	}

	private void init(AttributeSet attrs, int defStyle) {
		View view = ((LayoutInflater) getContext().getSystemService("layout_inflater")).inflate(
				R.layout.inbox_entry_view, this, true);
		this.swipeToDismiss = view.findViewById(R.id.swipe_to_dismiss_view);
		this.message = (TextView) view.findViewById(R.id.inbox_entry_message);
		this.timestamp = (TimerTextRefreshTextView) view.findViewById(R.id.inbox_entry_timestamp);
	}

	public void setMessage(String message) {
		if (message != null) {
			this.message.setText(message);
		}
	}

	public void setTimestamp(long timestamp) {
		this.timestamp.setTimestamp(timestamp);
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public void setShowMessageConversationSeperator(boolean show) {
		RelativeLayout.LayoutParams layoutParams;
		if (show) {
			this.swipeToDismiss.setBackground(getContext().getResources().getDrawable(R.drawable.card_message_bottom));
			layoutParams = (RelativeLayout.LayoutParams) this.swipeToDismiss.getLayoutParams();
			layoutParams.bottomMargin = (int) getContext().getResources().getDimension(R.dimen.card_spacing);
			this.swipeToDismiss.setLayoutParams(layoutParams);
			return;
		}
		this.swipeToDismiss.setBackground(getContext().getResources().getDrawable(R.drawable.card_message_middle));
		layoutParams = (RelativeLayout.LayoutParams) this.swipeToDismiss.getLayoutParams();
		layoutParams.bottomMargin = 0;
		this.swipeToDismiss.setLayoutParams(layoutParams);
	}
}

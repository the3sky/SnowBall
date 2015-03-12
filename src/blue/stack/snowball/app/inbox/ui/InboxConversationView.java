package blue.stack.snowball.app.inbox.ui;

import blue.stack.snowball.app.R;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import blue.stack.snowball.app.photos.ProfilePhoto;
import blue.stack.snowball.app.photos.ProfilePhoto.ImageType;
import blue.stack.snowball.app.ui.TimerTextRefreshTextView;
import blue.stack.snowball.app.ui.transform.CircleTransform;

import com.squareup.picasso.Picasso;

public class InboxConversationView extends LinearLayout {
	private final String TAG;
	String appId;
	String appSpecificSenderId;
	View containerView;
	View expanderDivider;
	TextView from;
	ImageView icon;
	boolean isExpanded;
	TextView messagePreview;
	int numUnreadMessages;
	ImageView profilePhoto;
	View swipeToDismiss;
	TimerTextRefreshTextView timestamp;

	class AnonymousClass_1 implements OnClickListener {
		final/* synthetic */OnConversationViewExpandedListener val$listener;

		AnonymousClass_1(OnConversationViewExpandedListener onConversationViewExpandedListener) {
			this.val$listener = onConversationViewExpandedListener;
		}

		@Override
		public void onClick(View view) {
			this.val$listener.onConversationViewExpanded(InboxConversationView.this);
		}
	}

	public static interface OnConversationViewExpandedListener {
		void onConversationViewExpanded(InboxConversationView inboxConversationView);
	}

	public InboxConversationView(Context context) {
		super(context);
		this.TAG = "InboxConversationView";
		init(null, 0);
	}

	public InboxConversationView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.TAG = "InboxConversationView";
		init(attrs, 0);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public InboxConversationView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.TAG = "InboxConversationView";
		init(attrs, defStyle);
	}

	private void init(AttributeSet attrs, int defStyle) {
		View view = ((LayoutInflater) getContext().getSystemService("layout_inflater")).inflate(
				R.layout.inbox_thread_view, this, true);
		this.containerView = view.findViewById(R.id.inbox_thread_container_view);
		this.swipeToDismiss = view.findViewById(R.id.swipe_to_dismiss_view);
		this.icon = (ImageView) view.findViewById(R.id.inbox_thread_icon);
		this.profilePhoto = (ImageView) view.findViewById(R.id.inbox_thread_profile_photo);
		this.from = (TextView) view.findViewById(R.id.inbox_thread_from);
		this.messagePreview = (TextView) view.findViewById(R.id.inbox_thread_message);
		this.timestamp = (TimerTextRefreshTextView) view.findViewById(R.id.inbox_thread_timestamp);
		this.expanderDivider = findViewById(R.id.inbox_thread_expander_divider_bottom);
		this.appSpecificSenderId = null;
		this.appId = null;
		this.numUnreadMessages = 0;
		this.isExpanded = false;
	}

	public String getAppId() {
		return this.appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

	public String getAppSpecificSenderId() {
		return this.appSpecificSenderId;
	}

	public void setAppSpecificSenderId(String appSpecificSenderId) {
		this.appSpecificSenderId = appSpecificSenderId;
	}

	public String getFrom() {
		CharSequence fromCS = this.from.getText();
		return fromCS != null ? fromCS.toString() : null;
	}

	public void setFrom(String from) {
		if (from != null) {
			this.from.setText(from);
		}
	}

	public void setIcon(Drawable icon) {
		if (icon != null) {
			this.icon.setImageDrawable(icon);
		}
	}

	public void setProfilePhoto(Context context, ProfilePhoto photo) {
		if (photo != null && photo.getPreferredImageType() == ImageType.BITMAP) {
			setProfilePhotoBitmap(photo.getBitmap());
		} else if (photo == null || photo.getPreferredImageType() != ImageType.URI) {
			setDefaultProfilePhoto(context);
		} else {
			setProfilePhotoUri(context, photo.getImageUri());
		}
	}

	public void setMessagePreview(String messagePreview) {
		if (messagePreview != null) {
			this.messagePreview.setText(messagePreview);
		}
	}

	public void setTimestamp(long timestamp) {
		this.timestamp.setTimestamp(timestamp);
	}

	public void setOnConversationExpandedListener(OnConversationViewExpandedListener listener) {
		findViewById(R.id.inbox_thread_expander).setOnClickListener(new AnonymousClass_1(listener));
	}

	public void setExpanded(boolean isExpanded) {
		this.isExpanded = isExpanded;
		updateConversationExpander();
	}

	public void showAllMessagesDivider(boolean show) {
		View allMessagesDivider = findViewById(R.id.all_messages_divider);
		if (show) {
			allMessagesDivider.setVisibility(0);
		} else {
			allMessagesDivider.setVisibility(8);
		}
	}

	public void setNumUnreadMessages(int size) {
		this.numUnreadMessages = size;
		updateConversationExpander();
	}

	public int getNumUnreadMessages() {
		return this.numUnreadMessages;
	}

	@SuppressLint("NewApi")
	public void setShowConversationMessageSeperator(boolean show) {
		if (show) {
			this.swipeToDismiss.setBackground(getContext().getResources().getDrawable(R.drawable.card_message_top));
			LayoutParams layoutParams = (LayoutParams) this.containerView.getLayoutParams();
			layoutParams.bottomMargin = 0;
			this.containerView.setLayoutParams(layoutParams);
			this.expanderDivider.setVisibility(0);
			return;
		}
		if (this.numUnreadMessages == 0) {
			this.swipeToDismiss.setBackground(getContext().getResources().getDrawable(R.drawable.card_message_read));
		} else {
			this.swipeToDismiss.setBackground(getContext().getResources().getDrawable(R.drawable.card_message));
		}
		LayoutParams layoutParams = (LayoutParams) this.containerView.getLayoutParams();
		layoutParams.bottomMargin = (int) getContext().getResources().getDimension(R.dimen.card_spacing);
		this.containerView.setLayoutParams(layoutParams);
		this.expanderDivider.setVisibility(8);
	}

	public void setTopMargin(int margin) {
		LayoutParams layoutParams = (LayoutParams) this.containerView.getLayoutParams();
		layoutParams.topMargin = margin;
		this.containerView.setLayoutParams(layoutParams);
	}

	void setDefaultProfilePhoto(Context context) {
		if (this.profilePhoto != null) {
			Picasso.with(context).load(R.drawable.profile_default).transform(new CircleTransform())
					.into(this.profilePhoto);
		}
	}

	void setProfilePhotoUri(Context context, Uri profilePhotoUri) {
		if (this.profilePhoto != null) {
			Picasso.with(context).load(profilePhotoUri).placeholder(R.drawable.profile_default)
					.error(R.drawable.profile_default).transform(new CircleTransform()).into(this.profilePhoto);
		}
	}

	void setProfilePhotoBitmap(Bitmap bitmap) {
		if (this.profilePhoto != null) {
			this.profilePhoto.setImageBitmap(ProfilePhoto.getRoundedRectBitmap(bitmap));
		}
	}

	void updateConversationExpander() {
		String text = "";
		TextView expanderText = (TextView) findViewById(R.id.inbox_thread_expander_text);
		View expander = findViewById(R.id.inbox_thread_expander);
		boolean showExpander = true;
		if (this.isExpanded) {
			text = getContext().getResources().getString(R.string.inbox_thread_collapse);
		} else if (this.numUnreadMessages == 2) {
			text = getContext().getResources().getString(R.string.inbox_thread_expand_one);
		} else if (this.numUnreadMessages > 2) {
			text = String.format(getContext().getResources().getString(R.string.inbox_thread_expand),
					new Object[] { Integer.valueOf(this.numUnreadMessages - 1) });
		} else {
			showExpander = false;
		}
		expanderText.setText(text);
		if (showExpander) {
			expander.setVisibility(0);
			expanderText.setVisibility(0);
			return;
		}
		expander.setVisibility(8);
		expanderText.setVisibility(8);
	}
}

package blue.stack.snowball.app.ui;

import blue.stack.snowball.app.R;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import blue.stack.snowball.app.photos.ProfilePhoto;
import blue.stack.snowball.app.photos.ProfilePhoto.ImageType;
import blue.stack.snowball.app.ui.transform.CircleTransform;

import com.squareup.picasso.Picasso;

public class LockScreenMessageView extends LinearLayout {
    private final String TAG;
    String appId;
    TextView from;
    ImageView icon;
    TextView message;
    int messageId;
    ImageView profilePhoto;
    String senderId;
    TimerTextRefreshTextView timestamp;

    public LockScreenMessageView(Context context) {
        super(context);
        this.TAG = "LockScreenMessageView";
        init(null, 0);
    }

    public LockScreenMessageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.TAG = "LockScreenMessageView";
        init(attrs, 0);
    }

    public LockScreenMessageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.TAG = "LockScreenMessageView";
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        ((LayoutInflater) getContext().getSystemService("layout_inflater")).inflate(R.layout.lockscreen_inbox_entry_view, this, true);
        this.icon = (ImageView) findViewById(R.id.inbox_thread_icon);
        this.profilePhoto = (ImageView) findViewById(R.id.inbox_thread_profile_photo);
        this.from = (TextView) findViewById(R.id.inbox_thread_from);
        this.message = (TextView) findViewById(R.id.inbox_thread_message);
        this.timestamp = (TimerTextRefreshTextView) findViewById(R.id.inbox_thread_timestamp);
        this.appId = null;
        this.senderId = null;
        this.messageId = -1;
    }

    public int getMessageId() {
        return this.messageId;
    }

    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }

    public String getAppId() {
        return this.appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getSenderId() {
        return this.senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
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

    public void setMessage(String message) {
        if (message != null) {
            this.message.setText(message);
        }
    }

    public void setTimestamp(long timestamp) {
        this.timestamp.setTimestamp(timestamp);
    }

    void setDefaultProfilePhoto(Context context) {
        Picasso.with(context).load((int) R.drawable.profile_default).transform(new CircleTransform()).into(this.profilePhoto);
    }

    void setProfilePhotoUri(Context context, Uri profilePhotoUri) {
        Picasso.with(context).load(profilePhotoUri).placeholder((int) R.drawable.profile_default).error((int) R.drawable.profile_default).transform(new CircleTransform()).into(this.profilePhoto);
    }

    void setProfilePhotoBitmap(Bitmap bitmap) {
        this.profilePhoto.setImageBitmap(ProfilePhoto.getRoundedRectBitmap(bitmap));
    }
}

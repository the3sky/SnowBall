package blue.stack.snowball.app.overlay.ui;

import java.util.Iterator;
import java.util.List;

import blue.stack.snowball.app.R;
import android.animation.Animator;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.LightingColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import blue.stack.snowball.app.apps.App;
import blue.stack.snowball.app.apps.AppManager;
import blue.stack.snowball.app.core.GuiceModule;
import blue.stack.snowball.app.core.ListenerHandler;
import blue.stack.snowball.app.core.ListenerSource;
import blue.stack.snowball.app.inbox.Message;
import blue.stack.snowball.app.notifications.Action;
import blue.stack.snowball.app.oob.OOBTutorialActivity;
import blue.stack.snowball.app.overlay.ui.OverlayNotificationBarView.NotificationViewListener;
import blue.stack.snowball.app.photos.ProfilePhoto;
import blue.stack.snowball.app.photos.ProfilePhoto.ImageType;
import blue.stack.snowball.app.settings.Settings;
import blue.stack.snowball.app.ui.anim.AnimationFinishListener;
import blue.stack.snowball.app.ui.transform.CircleTransform;

import com.google.inject.Inject;
import com.squareup.picasso.Picasso;

public class OverlayNotificationBarView extends RelativeLayout implements ListenerSource<NotificationViewListener> {
    private static final long READ_DELAY = 5000;
    private static int SLIDE_IN_TIME = 0;
    private static int SLIDE_OUT_TIME = 0;
    private static final String TAG = "OverlayNotificationBarView";
    @Inject
    AppManager appManager;
    @Inject
    Context context;
    private ListenerHandler<NotificationViewListener> listenerHandler;
    @Inject
    Settings settings;

    class AnonymousClass_2 implements Runnable {
        final /* synthetic */ AnimationFinishListener val$animationListener;

        AnonymousClass_2(AnimationFinishListener animationFinishListener) {
            this.val$animationListener = animationFinishListener;
        }

        public void run() {
            if (this.val$animationListener != null) {
                this.val$animationListener.onAnimationEnd(null);
            }
        }
    }

    public static interface NotificationViewListener {
        void onNotificationViewClosed();
    }

    public class DefaultOverlayNotificationListener implements NotificationViewListener {
        public void onNotificationViewClosed() {
        }
    }

    static {
        SLIDE_IN_TIME = 100;
        SLIDE_OUT_TIME = OOBTutorialActivity.PULSE_FADE_TIME;
    }

    public OverlayNotificationBarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.listenerHandler = new ListenerHandler();
        if (!isInEditMode()) {
            GuiceModule.get().injectMembers(this);
        }
    }

    public void setupView(Message message) {
        App app = this.appManager.getAppById(message.getAppId());
        TextView body = (TextView) findViewById(R.id.inbox_thread_message);
        ImageView appIcon = (ImageView) findViewById(R.id.inbox_thread_icon);
        ((TextView) findViewById(R.id.inbox_thread_from)).setText(message.getSender().getDisplayName());
        body.setText(message.getBody());
        appIcon.setImageDrawable(app.getAppIcon());
        setProfilePhoto((ImageView) findViewById(R.id.inbox_thread_profile_photo), app.getProfilePhotoForMessage(message));
        View actionBarView = findViewById(R.id.inbox_action_bar);
        List<Action> actions = message.getActions();
        if (actions == null || actions.size() == 0) {
            actionBarView.setVisibility(8);
            Log.d(TAG, "No actions found");
            return;
        }
        Button buttonTwo = (Button) findViewById(R.id.inbox_action_button_two);
        setButton((Button) findViewById(R.id.inbox_action_button_one), message, 0);
        setButton(buttonTwo, message, 1);
        actionBarView.setVisibility(0);
        Log.d(TAG, actions.size() + " actions found");
    }

    public void performHeadsUpPopup() {
        openPopup(new AnimationFinishListener() {
            public void onAnimationEnd(Animator animation) {
                OverlayNotificationBarView.this.closePopup(READ_DELAY);
            }
        });
    }

    private void openPopup(AnimationFinishListener animationListener) {
        View translationLayer = findViewById(R.id.translating_layer);
        cancelTranslation();
        translationLayer.setTranslationY((float) (-getHeight()));
        translationLayer.animate().setDuration((long) SLIDE_IN_TIME).translationYBy((float) getHeight()).withEndAction(new AnonymousClass_2(animationListener));
    }

    private void cancelTranslation() {
        findViewById(R.id.translating_layer).animate().cancel();
    }

    public void closePopup() {
        closePopup(0);
    }

    public void closePopup(long startDelay) {
        View translationLayer = findViewById(R.id.translating_layer);
        cancelTranslation();
        translationLayer.animate().setStartDelay(startDelay).setDuration((long) SLIDE_OUT_TIME).translationYBy((float) (-getHeight())).withEndAction(new Runnable() {
            public void run() {
                OverlayNotificationBarView.this.fireOnNotificationViewClosed();
            }
        });
    }

    public void stop() {
        this.listenerHandler.clearAll();
    }

    private void setButton(Button button, Message message, int index) {
        List<Action> actions = message.getActions();
        if (index >= actions.size()) {
            button.setVisibility(8);
            return;
        }
        Action action = (Action) actions.get(index);
        button.setOnClickListener(null);
        button.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        button.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0);
        button.setVisibility(0);
        Drawable icon = getActionIcon(message.getAppId(), action.icon);
        icon.setColorFilter(new LightingColorFilter(ViewCompat.MEASURED_STATE_MASK, 6914181));
        button.setCompoundDrawablesRelativeWithIntrinsicBounds(icon, null, null, null);
        button.setText(action.title);
    }

    private Drawable getActionIcon(String appId, int resourceId) {
        try {
            return this.context.getPackageManager().getResourcesForApplication(this.appManager.getAppById(appId).getAppPackageName()).getDrawable(resourceId);
        } catch (NameNotFoundException nnfe) {
            Log.d(TAG, "Caught PackageManager.NameNotFoundException when loading resource: " + Log.getStackTraceString(nnfe));
            return null;
        } catch (Exception e) {
            Log.d(TAG, "Caught exception when loading resource: " + Log.getStackTraceString(e));
            return null;
        }
    }

    private Drawable normalizeIcon(Drawable icon) {
        return new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(((BitmapDrawable) icon).getBitmap(), 80, 80, false));
    }

    private void setProfilePhoto(ImageView imageView, ProfilePhoto photo) {
        if (photo != null && photo.getPreferredImageType() == ImageType.BITMAP) {
            setProfilePhotoBitmap(imageView, photo.getBitmap());
        } else if (photo == null || photo.getPreferredImageType() != ImageType.URI) {
            setDefaultProfilePhoto(imageView);
        } else {
            setProfilePhotoUri(imageView, photo.getImageUri());
        }
    }

    private void setDefaultProfilePhoto(ImageView imageView) {
        Picasso.with(this.context).load((int) R.drawable.profile_default).transform(new CircleTransform()).into(imageView);
    }

    private void setProfilePhotoUri(ImageView imageView, Uri profilePhotoUri) {
        Picasso.with(this.context).load(profilePhotoUri).placeholder((int) R.drawable.profile_default).error((int) R.drawable.profile_default).transform(new CircleTransform()).into(imageView);
    }

    private void setProfilePhotoBitmap(ImageView imageView, Bitmap bitmap) {
        imageView.setImageBitmap(ProfilePhoto.getRoundedRectBitmap(bitmap));
    }

    public void addListener(Object handle, NotificationViewListener listener) {
        this.listenerHandler.addListener(handle, listener);
    }

    public void addListener(NotificationViewListener listener) {
        this.listenerHandler.addListener(listener);
    }

    public void removeListener(Object handle) {
        this.listenerHandler.removeListener(handle);
    }

    private void fireOnNotificationViewClosed() {
        Iterator i$ = this.listenerHandler.iterator();
        while (i$.hasNext()) {
            ((NotificationViewListener) i$.next()).onNotificationViewClosed();
        }
    }
}

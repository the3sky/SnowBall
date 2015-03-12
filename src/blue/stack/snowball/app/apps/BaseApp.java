package blue.stack.snowball.app.apps;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.util.Log;
import blue.stack.snowball.app.core.GuiceModule;
import blue.stack.snowball.app.inbox.Message;
import blue.stack.snowball.app.photos.ProfilePhoto;
import blue.stack.snowball.app.photos.ProfilePhotoManager;

public abstract class BaseApp implements App {
    private static final String TAG = "BaseApp";
    Context context;

    public abstract String getAppId();

    public void start(Context context) {
        this.context = context;
    }

    public void stop() {
        this.context = null;
    }

    public void restart() {
        Context savedContext = this.context;
        if (savedContext == null) {
            Log.d(TAG, "Failed to restart because we have a NULL context!!");
            return;
        }
        stop();
        start(savedContext);
    }

    public boolean isAppInstalled() {
        try {
            this.context.getPackageManager().getPackageInfo(getAppPackageName(), 1);
            return true;
        } catch (NameNotFoundException e) {
            return false;
        }
    }

    public Intent getLaunchIntent() {
        Intent launchIntent = this.context.getPackageManager().getLaunchIntentForPackage(getAppPackageName());
        if (launchIntent != null) {
            launchIntent.addFlags(AccessibilityNodeInfoCompat.ACTION_PASTE);
        }
        return launchIntent;
    }

    public Intent getLaunchIntentForMessage(Message message) {
        return getLaunchIntent();
    }

    public String getAppName() {
        try {
            PackageManager pm = this.context.getPackageManager();
            return pm.getApplicationInfo(getAppId(), 0).loadLabel(pm).toString();
        } catch (NameNotFoundException e) {
            Log.d(TAG, "NameNotFoundException when getting app info for package: " + getAppPackageName());
            return getAppPackageName();
        }
    }

    public Drawable getAppIcon() {
        try {
            return this.context.getPackageManager().getApplicationIcon(getAppPackageName());
        } catch (NameNotFoundException e) {
            return null;
        }
    }

    public String getAppPackageName() {
        return getAppId();
    }

    public ProfilePhoto getProfilePhotoForMessage(Message message) {
        return ((ProfilePhotoManager) GuiceModule.get().getInstance(ProfilePhotoManager.class)).getProfilePhoto(message.getAppId(), message.getSender().getAppSpecificUserId(), message.getSender().getDisplayName());
    }

    public boolean shouldRemoveDuplicates() {
        return false;
    }

    boolean shouldUseAsProfilePhoto(Bitmap bitmap) {
        if (bitmap == null) {
            return false;
        }
        boolean shouldUseAsProfilePhoto = false;
        try {
            if ((bitmap.getPixel(0, 0) & ViewCompat.MEASURED_STATE_MASK) == ViewCompat.MEASURED_STATE_MASK) {
                shouldUseAsProfilePhoto = true;
            } else {
                shouldUseAsProfilePhoto = false;
            }
        } catch (IllegalArgumentException e) {
        }
        return shouldUseAsProfilePhoto;
    }
}

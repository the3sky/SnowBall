package blue.stack.snowball.app.apps;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import blue.stack.snowball.app.inbox.Message;
import blue.stack.snowball.app.photos.ProfilePhoto;

public interface App {
    Drawable getAppIcon();

    String getAppId();

    String getAppName();

    String getAppPackageName();

    Intent getLaunchIntent();

    Intent getLaunchIntentForMessage(Message message);

    ProfilePhoto getProfilePhotoForMessage(Message message);

    boolean isAppInstalled();

    void restart();

    boolean shouldRemoveDuplicates();

    void start(Context context);

    void stop();
}

package blue.stack.snowball.app.inbox;

import java.util.List;

import android.app.PendingIntent;
import android.graphics.Bitmap;
import blue.stack.snowball.app.notifications.Action;

public class RawMessage {
    public static final String TAG = "RawMessage";
    List<Action> actions;
    String appId;
    String body;
    PendingIntent pendingLaunchIntent;
    Bitmap profilePhoto;
    String senderAppSpecificUserId;
    String senderDisplayName;
    int state;
    long timestamp;

    public RawMessage(String appId, String senderAppSpecificUserId, String senderDisplayName, String body, long timestamp, Bitmap profilePhoto, PendingIntent pendingLaunchIntent, List<Action> actions) {
        this.appId = appId;
        this.senderAppSpecificUserId = senderAppSpecificUserId;
        this.senderDisplayName = senderDisplayName;
        this.body = body;
        this.timestamp = timestamp;
        this.profilePhoto = profilePhoto;
        this.state = 0;
        this.pendingLaunchIntent = pendingLaunchIntent;
        this.actions = actions;
    }

    public String getAppId() {
        return this.appId;
    }

    public String getSenderAppSpecificUserId() {
        return this.senderAppSpecificUserId;
    }

    public String getSenderDisplayName() {
        return this.senderDisplayName;
    }

    public String getBody() {
        return this.body;
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public Bitmap getProfilePhoto() {
        return this.profilePhoto;
    }

    public int getState() {
        return this.state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public PendingIntent getPendingLaunchIntent() {
        return this.pendingLaunchIntent;
    }

    public List<Action> getActions() {
        return this.actions;
    }
}

package blue.stack.snowball.app.notifications;

import java.util.ArrayList;
import java.util.List;

import android.annotation.TargetApi;
import android.app.Notification.Action;
import android.app.PendingIntent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class Notification implements Parcelable {
    public static final Creator<Notification> CREATOR;
    private static final String TAG = "Notification";
    List<Action> actions;
    String contentInfoText;
    String contentText;
    String contentTitle;
    String expandedContentText;
    String expandedContentTitle;
    List<String> expandedInboxText;
    Bitmap expandedLargeIconBig;
    int id;
    String key;
    Bitmap largeIcon;
    String packageName;
    PendingIntent pendingLaunchIntent;
    List<Uri> people;
    Bitmap picture;
    int priority;
    int smallIcon;
    String tag;
    String tickerText;
    long when;

    @TargetApi(19)
    public Notification(String packageName, int id, String tag, String key, android.app.Notification n) {
        this.packageName = packageName;
        this.id = id;
        this.tag = tag;
        this.key = key;
        this.when = n.when;
        this.smallIcon = n.extras.getInt(NotificationCompat.EXTRA_SMALL_ICON, 0);
        this.largeIcon = (Bitmap) n.extras.getParcelable(NotificationCompat.EXTRA_LARGE_ICON);
        if (n.tickerText != null) {
            this.tickerText = n.tickerText.toString();
        }
        this.contentTitle = getStringFromNotificationCharSequence(n, NotificationCompat.EXTRA_TITLE);
        this.contentText = getStringFromNotificationCharSequence(n, NotificationCompat.EXTRA_TEXT);
        this.contentInfoText = getStringFromNotificationCharSequence(n, NotificationCompat.EXTRA_INFO_TEXT);
        this.expandedLargeIconBig = (Bitmap) n.extras.getParcelable(NotificationCompat.EXTRA_LARGE_ICON_BIG);
        this.expandedContentTitle = getStringFromNotificationCharSequence(n, NotificationCompat.EXTRA_TITLE_BIG);
        this.expandedContentText = getStringFromNotificationCharSequence(n, NotificationCompat.EXTRA_SUMMARY_TEXT);
        this.expandedInboxText = getStringArrayFromNotificationCharSequenceArray(n, NotificationCompat.EXTRA_TEXT_LINES);
        this.people = getUriArrayFromNotificationParcelableArray(n, NotificationCompat.EXTRA_PEOPLE);
        this.picture = null;
        this.priority = n.priority;
        this.pendingLaunchIntent = n.contentIntent;
        this.actions = new ArrayList();
        if (n.actions != null) {
            for (Action a : n.actions) {
                String title = null;
                if (a.title != null) {
                    title = a.title.toString();
                }
                this.actions.add(new Action(a.icon, title, a.actionIntent));
            }
        }
    }

    public Notification(Parcel in) {
        this.expandedInboxText = new ArrayList();
        this.people = new ArrayList();
        this.actions = new ArrayList();
        this.packageName = in.readString();
        this.id = in.readInt();
        this.tag = in.readString();
        this.key = in.readString();
        this.when = in.readLong();
        this.smallIcon = in.readInt();
        this.largeIcon = (Bitmap) in.readParcelable(Bitmap.class.getClassLoader());
        this.tickerText = in.readString();
        this.contentTitle = in.readString();
        this.contentText = in.readString();
        this.contentInfoText = in.readString();
        this.expandedLargeIconBig = (Bitmap) in.readParcelable(Bitmap.class.getClassLoader());
        this.expandedContentTitle = in.readString();
        this.expandedContentText = in.readString();
        in.readStringList(this.expandedInboxText);
        in.readList(this.people, Uri.class.getClassLoader());
        this.picture = (Bitmap) in.readParcelable(Bitmap.class.getClassLoader());
        this.priority = in.readInt();
        this.pendingLaunchIntent = (PendingIntent) in.readParcelable(PendingIntent.class.getClassLoader());
        in.readList(this.actions, Action.class.getClassLoader());
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(this.packageName);
        out.writeInt(this.id);
        out.writeString(this.tag);
        out.writeString(this.key);
        out.writeLong(this.when);
        out.writeInt(this.smallIcon);
        out.writeParcelable(this.largeIcon, 0);
        out.writeString(this.tickerText);
        out.writeString(this.contentTitle);
        out.writeString(this.contentText);
        out.writeString(this.contentInfoText);
        out.writeParcelable(this.expandedLargeIconBig, 0);
        out.writeString(this.expandedContentTitle);
        out.writeString(this.expandedContentText);
        out.writeStringList(this.expandedInboxText);
        out.writeList(this.people);
        out.writeParcelable(this.picture, 0);
        out.writeInt(this.priority);
        out.writeParcelable(this.pendingLaunchIntent, 0);
        out.writeList(this.actions);
        logParcelSize(out);
    }

    static {
        CREATOR = new Creator<Notification>() {
            public Notification createFromParcel(Parcel in) {
                return new Notification(in);
            }

            public Notification[] newArray(int size) {
                return new Notification[size];
            }
        };
    }

    public String getPackageName() {
        return this.packageName;
    }

    public int getId() {
        return this.id;
    }

    public String getTag() {
        return this.tag;
    }

    public String getKey() {
        return this.key;
    }

    public long getWhen() {
        return this.when;
    }

    public int getSmallIcon() {
        return this.smallIcon;
    }

    public Bitmap getLargeIcon() {
        return this.largeIcon;
    }

    public String getTickerText() {
        return this.tickerText;
    }

    public String getContentTitle() {
        return this.contentTitle;
    }

    public String getContentText() {
        return this.contentText;
    }

    public String getContentInfoText() {
        return this.contentInfoText;
    }

    public Bitmap getExpandedLargeIconBig() {
        return this.expandedLargeIconBig;
    }

    public String getExpandedContentTitle() {
        return this.expandedContentTitle;
    }

    public String getExpandedContentText() {
        return this.expandedContentText;
    }

    public List<String> getExpandedInboxText() {
        return this.expandedInboxText;
    }

    public List<Uri> getPeople() {
        return this.people;
    }

    public int getPriority() {
        return this.priority;
    }

    public PendingIntent getPendingLaunchIntent() {
        return this.pendingLaunchIntent;
    }

    public List<Action> getActions() {
        return this.actions;
    }

    public String generateNotificationString() {
        int i;
        StringBuilder sb = new StringBuilder();
        sb.append("Notification {");
        sb.append("    packageName equals " + this.packageName);
        sb.append("    id equals " + this.id);
        sb.append("    tag equals " + this.tag);
        sb.append("    key equals " + this.key);
        sb.append("    when equals " + this.when);
        sb.append("    smallIcon equals " + this.smallIcon);
        if (this.largeIcon != null) {
            sb.append("    largeIcon equals found");
        } else {
            sb.append("    largeIcon equals null");
        }
        sb.append("    tickerText equals " + this.tickerText);
        sb.append("    contentTitle equals " + this.contentTitle);
        sb.append("    contentText equals " + this.contentText);
        sb.append("    contentInfoText equals " + this.contentInfoText);
        if (this.expandedLargeIconBig != null) {
            sb.append("    expandedLargeIconBig equals found");
        } else {
            sb.append("    expandedLargeIconBig equals null");
        }
        sb.append("    expandedContentTitle equals " + this.expandedContentTitle);
        if (this.expandedInboxText.size() == 0) {
            sb.append("    expandedContentTitle equals is empty");
        } else {
            for (i = 0; i < this.expandedInboxText.size(); i++) {
                sb.append("    expandedInboxText[" + i + "] equals " + ((String) this.expandedInboxText.get(i)));
            }
        }
        sb.append("    expandedContentText equals " + this.expandedContentText);
        if (this.people.size() == 0) {
            sb.append("    people equals is empty");
        } else {
            for (i = 0; i < this.people.size(); i++) {
                sb.append("    people[" + i + "] equals " + this.people.get(i));
            }
        }
        if (this.picture != null) {
            sb.append("    picture equals found");
        } else {
            sb.append("    picture equals null");
        }
        if (this.actions.size() == 0) {
            sb.append("    actions is empty");
        } else {
            for (i = 0; i < this.actions.size(); i++) {
                sb.append("    actions[" + i + "] equals " + this.actions.get(i));
            }
        }
        sb.append("}");
        return sb.toString();
    }

    void logParcelSize(Parcel p) {
        Log.d(TAG, "Notification {");
        Log.d(TAG, "    packageName = " + this.packageName);
        Log.d(TAG, "    id = " + this.id);
        Log.d(TAG, "    tag = " + this.tag);
        Log.d(TAG, "    when = " + this.when);
        Log.d(TAG, "}  parcel size in bytes = " + p.dataSize());
    }

    public void logNotification() {
        int i;
        Log.d(TAG, "Notification {");
        Log.d(TAG, "    packageName = " + this.packageName);
        Log.d(TAG, "    id = " + this.id);
        Log.d(TAG, "    tag = " + this.tag);
        Log.d(TAG, "    key = " + this.key);
        Log.d(TAG, "    when = " + this.when);
        Log.d(TAG, "    smallIcon = " + this.smallIcon);
        if (this.largeIcon != null) {
            Log.d(TAG, "    largeIcon = found");
        } else {
            Log.d(TAG, "    largeIcon = null");
        }
        Log.d(TAG, "    tickerText = " + this.tickerText);
        Log.d(TAG, "    contentTitle = " + this.contentTitle);
        Log.d(TAG, "    contentText = " + this.contentText);
        Log.d(TAG, "    contentInfoText = " + this.contentInfoText);
        if (this.expandedLargeIconBig != null) {
            Log.d(TAG, "    expandedLargeIconBig = found");
        } else {
            Log.d(TAG, "    expandedLargeIconBig = null");
        }
        Log.d(TAG, "    expandedContentTitle = " + this.expandedContentTitle);
        if (this.expandedInboxText.size() == 0) {
            Log.d(TAG, "    expandedContentTitle = is empty");
        } else {
            for (i = 0; i < this.expandedInboxText.size(); i++) {
                Log.d(TAG, "    expandedInboxText[" + i + "] = " + ((String) this.expandedInboxText.get(i)));
            }
        }
        Log.d(TAG, "    expandedContentText = " + this.expandedContentText);
        if (this.people.size() == 0) {
            Log.d(TAG, "    people = is empty");
        } else {
            for (i = 0; i < this.people.size(); i++) {
                Log.d(TAG, "    people[" + i + "] = " + this.people.get(i));
            }
        }
        if (this.picture != null) {
            Log.d(TAG, "    picture = found");
        } else {
            Log.d(TAG, "    picture = null");
        }
        if (this.actions.size() == 0) {
            Log.d(TAG, "    actions is empty");
        } else {
            for (i = 0; i < this.actions.size(); i++) {
                Log.d(TAG, "    actions[" + i + "] equals " + this.actions.get(i));
            }
        }
        Log.d(TAG, "}");
    }

    @TargetApi(19)
    String getStringFromNotificationCharSequence(android.app.Notification n, String extra) {
        CharSequence cs = n.extras.getCharSequence(extra);
        return cs != null ? cs.toString() : null;
    }

    @TargetApi(19)
    List<String> getStringArrayFromNotificationCharSequenceArray(android.app.Notification n, String extra) {
        CharSequence[] csa = n.extras.getCharSequenceArray(extra);
        try {
            if (csa == null) {
                return new ArrayList();
            }
            List<String> stra = new ArrayList(csa.length);
            for (CharSequence cs : csa) {
                if (cs != null) {
                    stra.add(cs.toString());
                }
            }
            return stra;
        } catch (Exception e) {
            return new ArrayList();
        }
    }

    @TargetApi(19)
    List<Uri> getUriArrayFromNotificationParcelableArray(android.app.Notification n, String extra) {
        Parcelable[] pa = n.extras.getParcelableArray(extra);
        try {
            if (pa == null) {
                return new ArrayList();
            }
            List<Uri> uris = new ArrayList(pa.length);
            for (Parcelable p : pa) {
                uris.add((Uri) p);
            }
            return uris;
        } catch (Exception e) {
            return new ArrayList();
        }
    }
}

package blue.stack.snowball.app.notifications;

import android.app.PendingIntent;
import android.os.Parcel;
import android.os.Parcelable;

public class Action implements Parcelable {
    public static final Creator<Action> CREATOR;
    public PendingIntent actionIntent;
    public int icon;
    public String title;

    public Action(int icon, String title, PendingIntent actionIntent) {
        this.icon = icon;
        this.title = title;
        this.actionIntent = actionIntent;
    }

    public Action(Parcel in) {
        this.icon = in.readInt();
        this.title = in.readString();
        this.actionIntent = (PendingIntent) in.readParcelable(PendingIntent.class.getClassLoader());
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(this.icon);
        out.writeString(this.title);
        out.writeParcelable(this.actionIntent, 0);
    }

    static {
        CREATOR = new Creator<Action>() {
            public Action createFromParcel(Parcel in) {
                return new Action(in);
            }

            public Action[] newArray(int size) {
                return new Action[size];
            }
        };
    }
}

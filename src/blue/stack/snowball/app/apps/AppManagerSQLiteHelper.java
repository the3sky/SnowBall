package blue.stack.snowball.app.apps;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class AppManagerSQLiteHelper extends SQLiteOpenHelper {
    public static final String COLUMN_APP_ID = "_app_id";
    public static final String COLUMN_LAST_LAUNCHED = "_last_launched";
    public static final String COLUMN_LAUNCH_COUNT = "_launch_count";
    private static final String DATABASE_CREATE = "create table apps(_app_id text primary key not null, _last_launched integer, _launch_count integer);";
    private static final String DATABASE_NAME = "apps.db";
    private static final int DATABASE_VERSION = 1;
    public static final String TABLE_APP_LAUNCH_STATE = "apps";
    private static final String TAG = "AppManagerSQLiteHelper";

    public AppManagerSQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS apps");
        onCreate(db);
    }
}

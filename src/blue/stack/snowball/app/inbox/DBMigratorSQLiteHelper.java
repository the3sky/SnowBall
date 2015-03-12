package blue.stack.snowball.app.inbox;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBMigratorSQLiteHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "inbox.db";
    private static final int DATABASE_VERSION = 5;
    private static final String ENTRIES_APPID_STATE_CREATE_INDEX = "create index inbox_entries_appid_state_index ON inbox_entries(_app_id, _state);";
    private static final String ENTRIES_DATABASE_CREATE = "create table inbox_entries(_id integer primary key autoincrement, _thread_id integer, _app_id text not null, _sender_id text, _from text, _message text, _timestamp integer, _state integer default 0, _extra text);";
    private static final String ENTRIES_STATE_CREATE_INDEX = "create index inbox_entries_state_index ON inbox_entries(_thread_id, _state);";
    private static final String ENTRIES_TIMESTAMP_CREATE_INDEX = "create index inbox_entries_timestamp_index ON inbox_entries(_timestamp, _state);";
    public static final String INBOX_ENTRIES_COLUMN_APP_ID = "_app_id";
    public static final String INBOX_ENTRIES_COLUMN_EXTRA = "_extra";
    public static final String INBOX_ENTRIES_COLUMN_FROM = "_from";
    public static final String INBOX_ENTRIES_COLUMN_ID = "_id";
    public static final String INBOX_ENTRIES_COLUMN_MESSAGE = "_message";
    public static final String INBOX_ENTRIES_COLUMN_SENDER_ID = "_sender_id";
    public static final String INBOX_ENTRIES_COLUMN_STATE = "_state";
    public static final String INBOX_ENTRIES_COLUMN_THREAD_ID = "_thread_id";
    public static final String INBOX_ENTRIES_COLUMN_TIMESTAMP = "_timestamp";
    public static final int INBOX_ENTRY_STATE_ARCHIVED = 2;
    public static final int INBOX_ENTRY_STATE_DEFAULT = 0;
    public static final int INBOX_ENTRY_STATE_MARKED_READ = 1;
    public static final String INBOX_THREADS_COLUMN_APP_ID = "_app_id";
    public static final String INBOX_THREADS_COLUMN_EXTRA = "_extra";
    public static final String INBOX_THREADS_COLUMN_FROM = "_from";
    public static final String INBOX_THREADS_COLUMN_ID = "_id";
    public static final String INBOX_THREADS_COLUMN_NUM_ENTRIES = "_num_entries";
    public static final String INBOX_THREADS_COLUMN_PREVIEW = "_preview";
    public static final String INBOX_THREADS_COLUMN_SENDER_ID = "_sender_id";
    public static final String INBOX_THREADS_COLUMN_STATE = "_state";
    public static final String INBOX_THREADS_COLUMN_TIMESTAMP = "_timestamp";
    public static final String INDEX_APPID_STATE_INBOX_ENTRIES = "inbox_entries_appid_state_index";
    public static final String INDEX_APPID_STATE_INBOX_THREADS = "inbox_threads_appid_state_index";
    public static final String INDEX_STATE_INBOX_ENTRIES = "inbox_entries_state_index";
    public static final String INDEX_STATE_INBOX_THREADS = "inbox_threads_state_index";
    public static final String INDEX_TIMESTAMP_INBOX_ENTRIES = "inbox_entries_timestamp_index";
    public static final String TABLE_INBOX_ENTRIES = "inbox_entries";
    public static final String TABLE_INBOX_THREADS = "inbox_threads";
    private static final String TAG = "InboxSQLiteHelper";
    private static final String THREADS_APPID_STATE_CREATE_INDEX = "create index inbox_threads_appid_state_index ON inbox_threads(_app_id, _state);";
    private static final String THREADS_DATABASE_CREATE = "create table inbox_threads(_id integer primary key autoincrement, _app_id text not null, _sender_id text, _from text, _preview text, _timestamp integer, _num_entries integer, _state integer default 0, _extra text);";
    private static final String THREADS_STATE_CREATE_INDEX = "create index inbox_threads_state_index ON inbox_threads(_state);";

    public DBMigratorSQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(THREADS_DATABASE_CREATE);
        db.execSQL(THREADS_STATE_CREATE_INDEX);
        db.execSQL(THREADS_APPID_STATE_CREATE_INDEX);
        db.execSQL(ENTRIES_DATABASE_CREATE);
        db.execSQL(ENTRIES_TIMESTAMP_CREATE_INDEX);
        db.execSQL(ENTRIES_STATE_CREATE_INDEX);
        db.execSQL(ENTRIES_APPID_STATE_CREATE_INDEX);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion + ", which may destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS inbox_entries");
        db.execSQL("DROP TABLE IF EXISTS inbox_threads");
        onCreate(db);
    }

    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
    }
}

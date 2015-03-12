package blue.stack.snowball.app.inbox;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class InboxSQLiteHelper extends SQLiteOpenHelper {
    public static final String COLUMN_CONVERSATIONS_APP_ID = "_app_id";
    public static final String COLUMN_CONVERSATIONS_ID = "_conversation_id";
    public static final String COLUMN_CONVERSATIONS_LAST_MESSAGE_ID = "_last_message_id";
    public static final String COLUMN_CONVERSATIONS_NUM_UNREAD_MESSAGES = "_num_unread_messages";
    public static final String COLUMN_CONVERSATIONS_SENDER_USER_ID = "_sender_user_id";
    public static final String COLUMN_MESSAGES_APP_ID = "_app_id";
    public static final String COLUMN_MESSAGES_BODY = "_body";
    public static final String COLUMN_MESSAGES_ID = "_message_id";
    public static final String COLUMN_MESSAGES_SENDER_USER_ID = "_sender_user_id";
    public static final String COLUMN_MESSAGES_STATE = "_state";
    public static final String COLUMN_MESSAGES_TIMESTAMP = "_timestamp";
    public static final String COLUMN_USERS_APPSPECIFIC_USER_ID = "_appspecific_user_id";
    public static final String COLUMN_USERS_APP_ID = "_app_id";
    public static final String COLUMN_USERS_DISPLAY_NAME = "_display_name";
    public static final String COLUMN_USERS_ID = "_user_id";
    public static final String COLUMN_USERS_PHOTO = "_photo";
    public static final String COLUMN_USERS_TYPE = "_type";
    public static final String CONVERSATIONS_QUERY = "SELECT c.*, u.*, m.*, c._conversation_id as _id FROM conversations c INNER JOIN messages m ON c._last_message_id=m._message_id INNER JOIN users u ON c._sender_user_id=u._user_id ORDER BY + m._timestamp";
    private static final String CREATE_TABLE_CONVERSATIONS = "create table conversations(_conversation_id integer primary key autoincrement, _app_id text, _sender_user_id integer, _last_message_id integer, _num_unread_messages integer);";
    private static final String CREATE_TABLE_MESSAGES = "create table messages(_message_id integer primary key autoincrement, _app_id text, _sender_user_id integer, _body text, _state integer default 0, _timestamp integer);";
    private static final String CREATE_TABLE_USERS = "create table users(_user_id integer primary key autoincrement, _app_id text, _appspecific_user_id text, _display_name text, _type integer default 0, _photo blob);";
    private static final String CREATE_UNIQUE_INDEX_CONVERSATIONS = "create unique index unique_index_conversations on users(_app_id, _appspecific_user_id)";
    private static final String CREATE_UNIQUE_INDEX_USERS = "create unique index unique_index_users on users(_app_id, _appspecific_user_id)";
    private static final String CREATE_VIEW_CONVERSATIONS = "CREATE VIEW conversations_view AS SELECT c.*, u.*, m.*, c._conversation_id as _id FROM conversations c INNER JOIN messages m ON c._last_message_id=m._message_id INNER JOIN users u ON c._sender_user_id=u._user_id ORDER BY + m._timestamp";
    private static final String CREATE_VIEW_MESSAGES = "CREATE VIEW messages_view AS SELECT u.*, m.*, m._message_id as _id FROM messages m INNER JOIN users u ON m._sender_user_id=u._user_id ORDER BY + m._timestamp";
    private static final String DATABASE_NAME = "inbox2.db";
    private static final int DATABASE_VERSION = 1;
    private static final String MESSAGES_QUERY = "SELECT u.*, m.*, m._message_id as _id FROM messages m INNER JOIN users u ON m._sender_user_id=u._user_id ORDER BY + m._timestamp";
    public static final int MESSAGE_STATE_READ = 1;
    public static final int MESSAGE_STATE_UNREAD = 0;
    public static final String TABLE_CONVERSATIONS = "conversations";
    public static final String TABLE_MESSAGES = "messages";
    public static final String TABLE_USERS = "users";
    private static final String TAG = "InboxSQLiteHelper";
    private static final String UNIQUE_INDEX_CONVERSATIONS = "unique_index_conversations";
    private static final String UNIQUE_INDEX_USERS = "unique_index_users";
    public static final int USER_TYPE_GROUP = 2;
    public static final int USER_TYPE_SINGLE = 1;
    public static final int USER_TYPE_UNKNOWN = 0;
    public static final String VIEW_CONVERSATIONS = "conversations_view";
    public static final String VIEW_MESSAGES = "messages_view";

    public InboxSQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, USER_TYPE_SINGLE);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_USERS);
        db.execSQL(CREATE_TABLE_MESSAGES);
        db.execSQL(CREATE_TABLE_CONVERSATIONS);
        db.execSQL(CREATE_UNIQUE_INDEX_USERS);
        db.execSQL(CREATE_UNIQUE_INDEX_CONVERSATIONS);
        db.execSQL(CREATE_VIEW_MESSAGES);
        db.execSQL(CREATE_VIEW_CONVERSATIONS);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
    }
}

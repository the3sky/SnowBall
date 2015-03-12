package blue.stack.snowball.app.inbox;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import blue.stack.snowball.app.photos.ProfilePhotoManager;

public class DBMigrator {

    static class AnonymousClass_1 extends AsyncTask<Void, Void, Void> {
        final /* synthetic */ Context val$context;
        final /* synthetic */ InboxManager val$inboxManager;

        AnonymousClass_1(Context context, InboxManager inboxManager) {
            this.val$context = context;
            this.val$inboxManager = inboxManager;
        }

        protected Void doInBackground(Void[] objects) {
            if (!isCancelled()) {
                DBMigrator.doMigration(this.val$context, this.val$inboxManager);
            }
            return null;
        }

        protected void onPostExecute(Void object) {
        }
    }

    public static void migrate(Context context, InboxManager inboxManager) {
        new AnonymousClass_1(context, inboxManager).execute(new Void[0]);
    }

    static void doMigration(Context context, InboxManager inboxManager) {
        try {
            if (doesDatabaseExist(context, DBMigratorSQLiteHelper.DATABASE_NAME)) {
                DBMigratorSQLiteHelper dbHelper = new DBMigratorSQLiteHelper(context);
                if (dbHelper != null) {
                    Cursor cursor = getTopEntryCursor(dbHelper.getWritableDatabase());
                    while (cursor.moveToNext()) {
                        insertMessageFromEntry(inboxManager, cursor);
                    }
                    cursor.close();
                    dbHelper.close();
                    try {
                        context.deleteDatabase(DBMigratorSQLiteHelper.DATABASE_NAME);
                    } catch (Exception e) {
                    }
                }
            }
        } catch (Exception e2) {
        }
    }

    private static Cursor getTopEntryCursor(SQLiteDatabase database) {
        return database.query(DBMigratorSQLiteHelper.TABLE_INBOX_ENTRIES, new String[]{ProfilePhotoManager.CONTACTS_ID_COLUMN, DBMigratorSQLiteHelper.INBOX_ENTRIES_COLUMN_THREAD_ID, InboxSQLiteHelper.COLUMN_USERS_APP_ID, DBMigratorSQLiteHelper.INBOX_THREADS_COLUMN_SENDER_ID, DBMigratorSQLiteHelper.INBOX_THREADS_COLUMN_FROM, DBMigratorSQLiteHelper.INBOX_ENTRIES_COLUMN_MESSAGE, InboxSQLiteHelper.COLUMN_MESSAGES_TIMESTAMP, InboxSQLiteHelper.COLUMN_MESSAGES_STATE, DBMigratorSQLiteHelper.INBOX_THREADS_COLUMN_EXTRA}, "_state != 2", null, null, null, "_timestamp ASC");
    }

    private static void insertMessageFromEntry(InboxManager inboxManager, Cursor entryCursor) {
        int id = entryCursor.getInt(entryCursor.getColumnIndex(ProfilePhotoManager.CONTACTS_ID_COLUMN));
        int threadId = entryCursor.getInt(entryCursor.getColumnIndex(DBMigratorSQLiteHelper.INBOX_ENTRIES_COLUMN_THREAD_ID));
        String appId = entryCursor.getString(entryCursor.getColumnIndex(InboxSQLiteHelper.COLUMN_USERS_APP_ID));
        String senderId = entryCursor.getString(entryCursor.getColumnIndex(DBMigratorSQLiteHelper.INBOX_THREADS_COLUMN_SENDER_ID));
        String from = entryCursor.getString(entryCursor.getColumnIndex(DBMigratorSQLiteHelper.INBOX_THREADS_COLUMN_FROM));
        String message = entryCursor.getString(entryCursor.getColumnIndex(DBMigratorSQLiteHelper.INBOX_ENTRIES_COLUMN_MESSAGE));
        long timestamp = entryCursor.getLong(entryCursor.getColumnIndex(InboxSQLiteHelper.COLUMN_MESSAGES_TIMESTAMP));
        int state = entryCursor.getInt(entryCursor.getColumnIndex(InboxSQLiteHelper.COLUMN_MESSAGES_STATE));
        String extra = entryCursor.getString(entryCursor.getColumnIndex(DBMigratorSQLiteHelper.INBOX_THREADS_COLUMN_EXTRA));
        RawMessage rawMessage = new RawMessage(appId, senderId, from, message, timestamp, null, null, null);
        rawMessage.setState(1);
        inboxManager.injectMessage(rawMessage);
    }

    private static boolean doesDatabaseExist(Context context, String dbName) {
        return context.getDatabasePath(dbName).exists();
    }
}

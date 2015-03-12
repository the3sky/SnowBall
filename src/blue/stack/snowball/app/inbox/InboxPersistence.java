package blue.stack.snowball.app.inbox;

import java.util.ArrayList;
import java.util.List;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.util.Log;
import android.util.LruCache;
import blue.stack.snowball.app.notifications.Action;

public class InboxPersistence {
	private static final int ACTIONS_CACHE_SIZE = 10;
	private static final int PENDING_INTENT_CACHE_SIZE = 50;
	private static final String TAG = "InboxPersistence";
	LruCache<Integer, List<Action>> actionsCache;
	private SQLiteDatabase database;
	private InboxSQLiteHelper dbHelper;
	LruCache<Integer, PendingIntent> pendingLaunchIntentCache;

	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
	public int saveMessage(RawMessage message) {
		int v3;
		boolean v2 = false;
		int v10 = -1;
		if (message.getState() == 0) {
			v2 = true;
			// goto label_5;
		}

		try {
			this.database.beginTransaction();
			int v4 = this.getUserId(message.getAppId(), message.getSenderAppSpecificUserId(), message
					.getSenderDisplayName());
			if (v4 == v10) {
				v4 = this.insertUser(message.getAppId(), message.getSenderAppSpecificUserId(), message
						.getSenderDisplayName());
			}

			if (v4 == v10) {
				throw new SQLException("Failed to find or create user");
			}

			v3 = this.insertMessage(v4, message);
			if (v3 == v10) {
				throw new SQLException("Failed to create message");
			}

			int v0 = this.getConversationId(v4);
			if (v0 == v10) {
				if (this.insertConversation(message.getAppId(), v4, v3, v2) != v10) {
					this.database.setTransactionSuccessful();
					this.updatePendingIntent(v4, message.getPendingLaunchIntent());
					if (message.getActions() == null) {
						this.database.endTransaction();
						return v3;
					}

					this.actionsCache.put(Integer.valueOf(v3), message.getActions());
				} else {
					throw new SQLException("Failed to insert conversation");
				}

			} else {
				this.database.setTransactionSuccessful();
			}

			int v1 = 0;
			if (v2) {
				v1 = 1;
			}

			if (!this.updateConversation(v0, v3, v1)) {
				throw new SQLException("Failed to update conversation");
			}

		}

		catch (SQLException v5) {
			v5.printStackTrace();

			v3 = -1;
			this.database.endTransaction();
			return v3;
		}
		// this.database.setTransactionSuccessful();
		this.database.endTransaction();
		return v3;
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
	public InboxPersistence(Context context) {
		this.dbHelper = new InboxSQLiteHelper(context);
		this.pendingLaunchIntentCache = new LruCache<Integer, PendingIntent>(PENDING_INTENT_CACHE_SIZE);
		this.actionsCache = new LruCache<Integer, List<Action>>(ACTIONS_CACHE_SIZE);
	}

	public void open() throws SQLException {
		this.database = this.dbHelper.getWritableDatabase();
	}

	public void close() {
		this.dbHelper.close();
	}

	public Message getMessage(int messageId) {
		Message message = null;
		Cursor cursor = this.database.query(InboxSQLiteHelper.VIEW_MESSAGES, null, "_message_id = " + messageId, null,
				null, null, null);
		if (cursor.moveToFirst()) {
			message = getMessageFromViewCursor(cursor);
		}
		cursor.close();
		return message;
	}

	public List<Message> getUnreadMessages(int limit) {
		List<Message> messages = new ArrayList<Message>();
		Cursor cursor = this.database.query(InboxSQLiteHelper.VIEW_MESSAGES, null, "_state = 0", null, null, null,
				"_message_id DESC", Integer.toString(limit));
		while (cursor.moveToNext()) {
			Message message = getMessageFromViewCursor(cursor);
			if (message != null) {
				messages.add(message);
			}
		}
		cursor.close();
		return messages;
	}

	public boolean deleteConversation(Conversation conversation) {
		boolean success;
		try {
			this.database.beginTransaction();
			if (this.database.delete(InboxSQLiteHelper.TABLE_MESSAGES, "_sender_user_id="
					+ conversation.getSender().getId(), null) == 0) {
				throw new SQLException("Failed to delete message from DB");
			} else if (this.database.delete(InboxSQLiteHelper.TABLE_CONVERSATIONS, "_sender_user_id="
					+ conversation.getSender().getId(), null) != 1) {
				throw new SQLException("Failed to delete message from DB");
			} else {
				this.database.setTransactionSuccessful();
				success = true;
				return success;
			}
		} catch (SQLException e) {
			Log.d(TAG, "Error trying to delete a message");
			success = false;
		} finally {
			this.database.endTransaction();
		}
		return success;
	}

	public boolean deleteMessage(Message message) {
		boolean success;
		try {
			this.database.beginTransaction();
			if (this.database.delete(InboxSQLiteHelper.TABLE_MESSAGES, "_message_id=" + message.getId(), null) != 1) {
				throw new SQLException("Failed to delete message from DB");
			}
			int conversationId = getConversationId(message.getSenderUserId());
			if (conversationId == -1) {
				throw new SQLException(
						"Failed while deleting message because we couldn't find a matching conversation id");
			} else if (updateConversationAfterMessageDelete(conversationId, message.getSenderUserId())) {
				this.database.setTransactionSuccessful();
				success = true;
				return success;
			} else {
				throw new SQLException("Failed while deleting message because we couldn't update the conversation");
			}
		} catch (SQLException e) {
			Log.d(TAG, "Error trying to delete a message");
			success = false;
		} finally {
			this.database.endTransaction();
		}

		return success;
	}

	public boolean deleteAllMessages() {
		boolean success;
		try {
			this.database.beginTransaction();
			this.database.delete(InboxSQLiteHelper.TABLE_CONVERSATIONS, null, null);
			this.database.delete(InboxSQLiteHelper.TABLE_MESSAGES, null, null);
			this.database.setTransactionSuccessful();
			success = true;
		} catch (SQLException e) {
			Log.d(TAG, "Error trying to clear inbox");
			success = false;
		} finally {
			this.database.endTransaction();
		}
		return success;
	}

	public boolean markAppAsRead(String appId) {
		boolean success;
		try {
			this.database.beginTransaction();
			ContentValues values = new ContentValues();
			values.put(InboxSQLiteHelper.COLUMN_MESSAGES_STATE, Integer.valueOf(1));
			this.database
					.update(InboxSQLiteHelper.TABLE_MESSAGES, values, "_state=0 and _app_id='" + appId + "'", null);
			values = new ContentValues();
			values.put(InboxSQLiteHelper.COLUMN_CONVERSATIONS_NUM_UNREAD_MESSAGES, Integer.valueOf(0));
			this.database.update(InboxSQLiteHelper.TABLE_CONVERSATIONS, values, "_app_id='" + appId + "'", null);
			this.database.setTransactionSuccessful();
			success = true;
		} catch (SQLException e) {
			Log.d(TAG, "Error trying to mark all messages as read");
			success = false;
		} finally {
			this.database.endTransaction();
		}
		return success;
	}

	public boolean markMessageAsRead(Message message) {
		boolean updateConversationCount;
		try {
			this.database.beginTransaction();
			int conversationId = getConversationId(message.getSenderUserId());
			if (conversationId == -1) {
				throw new Exception("Could not find conversation for message with id=" + message.getId());
			}
			updateConversationCount = updateConversationCount(conversationId, -1);
			if (updateConversationCount) {
				ContentValues values = new ContentValues();
				values.put(InboxSQLiteHelper.COLUMN_MESSAGES_STATE, Integer.valueOf(1));
				this.database.update(InboxSQLiteHelper.TABLE_MESSAGES, values, "_message_id=" + message.getId(), null);
				this.database.setTransactionSuccessful();
				updateConversationCount = true;
				return updateConversationCount;
			}
			throw new Exception("Failed to decrement unread count for message with id=" + message.getId()
					+ " and conversation with id=" + conversationId);
		} catch (SQLException e) {
			Log.d(TAG, "Error trying to mark all messages as read");
			updateConversationCount = false;
		} catch (Exception e2) {
			Log.d(TAG, "Error trying to mark all messages as read");
			updateConversationCount = false;
		} finally {
			this.database.endTransaction();
		}
		return updateConversationCount;
	}

	public boolean markConversationAsRead(Conversation conversation) {
		boolean success;
		try {
			this.database.beginTransaction();
			ContentValues values = new ContentValues();
			values.put(InboxSQLiteHelper.COLUMN_CONVERSATIONS_NUM_UNREAD_MESSAGES, Integer.valueOf(0));
			this.database.update(InboxSQLiteHelper.TABLE_CONVERSATIONS, values,
					"_conversation_id=" + conversation.getId(), null);
			values = new ContentValues();
			values.put(InboxSQLiteHelper.COLUMN_MESSAGES_STATE, Integer.valueOf(1));
			this.database.update(InboxSQLiteHelper.TABLE_MESSAGES, values, "_sender_user_id="
					+ conversation.getSender().getId() + " and " + InboxSQLiteHelper.COLUMN_MESSAGES_STATE + "=" + 0,
					null);
			this.database.setTransactionSuccessful();
			success = true;
		} catch (SQLException e) {
			Log.d(TAG, "Error trying to mark conversation as read");
			success = false;
		} catch (Exception e2) {
			Log.d(TAG, "Error trying to mark conversation as read");
			success = false;
		} finally {
			this.database.endTransaction();
		}
		return success;
	}

	public boolean markAllMessageAsRead() {
		boolean success;
		try {
			this.database.beginTransaction();
			ContentValues values = new ContentValues();
			values.put(InboxSQLiteHelper.COLUMN_MESSAGES_STATE, Integer.valueOf(1));
			this.database.update(InboxSQLiteHelper.TABLE_MESSAGES, values, "_state=0", null);
			values = new ContentValues();
			values.put(InboxSQLiteHelper.COLUMN_CONVERSATIONS_NUM_UNREAD_MESSAGES, Integer.valueOf(0));
			this.database.update(InboxSQLiteHelper.TABLE_CONVERSATIONS, values, null, null);
			this.database.setTransactionSuccessful();
			success = true;
		} catch (SQLException e) {
			Log.d(TAG, "Error trying to mark all messages as read");
			success = false;
		} finally {
			this.database.endTransaction();
		}
		return success;
	}

	public int getLatestMessageId() {
		Cursor cursor = this.database.query(InboxSQLiteHelper.TABLE_MESSAGES,
				new String[] { InboxSQLiteHelper.COLUMN_MESSAGES_ID }, null, null, null, null, "_message_id DESC", "1");
		return cursor.moveToFirst() ? cursor.getInt(cursor.getColumnIndex(InboxSQLiteHelper.COLUMN_MESSAGES_ID)) : -1;
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
	public void updatePendingIntent(int senderUserId, PendingIntent pendingIntent) {
		if (senderUserId != -1 && pendingIntent != null) {
			this.pendingLaunchIntentCache.put(Integer.valueOf(senderUserId), pendingIntent);
		}
	}

	private int getUserId(String appId, String appSpecificUserId, String displayName) {
		String[] selectionArgs;
		String where;
		int userId = -1;
		String[] columns = new String[] { InboxSQLiteHelper.COLUMN_USERS_ID };
		if (appSpecificUserId == null) {
			selectionArgs = new String[] { appId, displayName };
			where = "_app_id=? and _display_name=?";
		} else {
			selectionArgs = new String[] { appId, displayName, appSpecificUserId };
			where = "_app_id=? and _display_name=? and _appspecific_user_id=?";
		}
		Cursor cursor = this.database.query(InboxSQLiteHelper.TABLE_USERS, columns, where, selectionArgs, null, null,
				null);
		if (cursor.moveToFirst()) {
			userId = cursor.getInt(cursor.getColumnIndex(InboxSQLiteHelper.COLUMN_USERS_ID));
		}
		cursor.close();
		return userId;
	}

	private int insertUser(String appId, String appSpecificUserId, String displayName) {
		ContentValues values = new ContentValues();
		values.put(InboxSQLiteHelper.COLUMN_USERS_APP_ID, appId);
		values.put(InboxSQLiteHelper.COLUMN_USERS_APPSPECIFIC_USER_ID, appSpecificUserId);
		values.put(InboxSQLiteHelper.COLUMN_USERS_DISPLAY_NAME, displayName);
		values.put(InboxSQLiteHelper.COLUMN_USERS_TYPE, Integer.valueOf(0));
		long rowId = this.database.insert(InboxSQLiteHelper.TABLE_USERS, null, values);
		return rowId != -1 ? (int) rowId : -1;
	}

	private int insertMessage(int senderUserId, RawMessage message) {
		ContentValues values = new ContentValues();
		values.put(InboxSQLiteHelper.COLUMN_MESSAGES_SENDER_USER_ID, Integer.valueOf(senderUserId));
		values.put(InboxSQLiteHelper.COLUMN_USERS_APP_ID, message.getAppId());
		values.put(InboxSQLiteHelper.COLUMN_MESSAGES_BODY, message.getBody());
		values.put(InboxSQLiteHelper.COLUMN_MESSAGES_STATE, Integer.valueOf(0));
		values.put(InboxSQLiteHelper.COLUMN_MESSAGES_TIMESTAMP, Long.valueOf(message.getTimestamp()));
		long rowId = this.database.insert(InboxSQLiteHelper.TABLE_MESSAGES, null, values);
		return rowId != -1 ? (int) rowId : -1;
	}

	private int getConversationId(int senderUserId) {
		int conversationId = -1;
		Cursor cursor = this.database.query(InboxSQLiteHelper.TABLE_CONVERSATIONS,
				new String[] { InboxSQLiteHelper.COLUMN_CONVERSATIONS_ID }, "_sender_user_id=" + senderUserId, null,
				null, null, null);
		if (cursor.moveToFirst()) {
			conversationId = cursor.getInt(cursor.getColumnIndex(InboxSQLiteHelper.COLUMN_CONVERSATIONS_ID));
		}
		cursor.close();
		return conversationId;
	}

	private int insertConversation(String appId, int senderUserId, int lastMessageId, boolean isUnread) {
		int numUnread;
		if (isUnread) {
			numUnread = 1;
		} else {
			numUnread = 0;
		}
		ContentValues values = new ContentValues();
		values.put(InboxSQLiteHelper.COLUMN_USERS_APP_ID, appId);
		values.put(InboxSQLiteHelper.COLUMN_MESSAGES_SENDER_USER_ID, Integer.valueOf(senderUserId));
		values.put(InboxSQLiteHelper.COLUMN_CONVERSATIONS_LAST_MESSAGE_ID, Integer.valueOf(lastMessageId));
		values.put(InboxSQLiteHelper.COLUMN_CONVERSATIONS_NUM_UNREAD_MESSAGES, Integer.valueOf(numUnread));
		long rowId = this.database.insert(InboxSQLiteHelper.TABLE_CONVERSATIONS, null, values);
		return rowId != -1 ? (int) rowId : -1;
	}

	private boolean deleteConversation(int conversationId) {
		return this.database.delete(InboxSQLiteHelper.TABLE_CONVERSATIONS,
				new StringBuilder().append("_conversation_id=").append(conversationId).toString(), null) == 1;
	}

	private boolean updateConversationCount(int conversationId, int unreadCountIncrement) {
		Cursor cursor = this.database.rawQuery("UPDATE conversations SET _num_unread_messages=_num_unread_messages+"
				+ unreadCountIncrement + " WHERE " + InboxSQLiteHelper.COLUMN_CONVERSATIONS_ID + "=" + conversationId,
				null);
		cursor.moveToFirst();
		cursor.close();
		return true;
	}

	private boolean updateConversation(int conversationId, int lastMessageId, int unreadCountIncrement) {
		Cursor cursor = this.database.rawQuery("UPDATE conversations SET _last_message_id=" + lastMessageId + ", "
				+ InboxSQLiteHelper.COLUMN_CONVERSATIONS_NUM_UNREAD_MESSAGES + "="
				+ InboxSQLiteHelper.COLUMN_CONVERSATIONS_NUM_UNREAD_MESSAGES + "+" + unreadCountIncrement + " WHERE "
				+ InboxSQLiteHelper.COLUMN_CONVERSATIONS_ID + "=" + conversationId, null);
		cursor.moveToFirst();
		cursor.close();
		return true;
	}

	private boolean updateConversationAfterMessageDelete(int conversationId, int senderUserId) {
		boolean success;
		Cursor cursor = this.database.query(InboxSQLiteHelper.TABLE_MESSAGES,
				new String[] { InboxSQLiteHelper.COLUMN_MESSAGES_ID }, "_sender_user_id=" + senderUserId, null, null,
				null, "_timestamp DESC");
		if (cursor.getCount() == 0) {
			success = deleteConversation(conversationId);
		} else if (cursor.moveToFirst()) {
			success = updateConversation(conversationId,
					cursor.getInt(cursor.getColumnIndex(InboxSQLiteHelper.COLUMN_MESSAGES_ID)), -1);
		} else {
			success = false;
		}
		cursor.close();
		return success;
	}

	public Cursor getConversationsView() {
		return this.database.query(InboxSQLiteHelper.VIEW_CONVERSATIONS, null, null, null, null, null,
				"_num_unread_messages<=0, _timestamp DESC");
	}

	public Cursor getMessagesView(int senderUserId) {
		return this.database.query(InboxSQLiteHelper.VIEW_MESSAGES, null, "_sender_user_id=" + senderUserId + " and "
				+ InboxSQLiteHelper.COLUMN_MESSAGES_STATE + "=" + 0, null, null, null, "_timestamp DESC");
	}

	public Cursor getUnreadMessagesViewSince(int messageId) {
		String where = "_state=0";
		if (messageId != -1) {
			where = where + " and _message_id > " + messageId;
		}
		return this.database.query(InboxSQLiteHelper.VIEW_MESSAGES, null, where, null, null, null, "_timestamp DESC");
	}

	public Conversation getConversationFromViewCursor(Cursor cursor) {
		Conversation conversation = getConversationFromCursor(cursor);
		Message message = getMessageFromCursor(cursor);
		User user = getUserFromCursor(cursor);
		conversation.setLastMessage(message);
		conversation.setSender(user);
		message.setSender(user);
		return conversation;
	}

	public Message getMessageFromViewCursor(Cursor cursor) {
		Message message = getMessageFromCursor(cursor);
		User user = getUserFromCursor(cursor);
		if (!(message == null || user == null)) {
			message.setSender(user);
		}
		return message;
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
	private Conversation getConversationFromCursor(Cursor cursor) {
		int id = cursor.getInt(cursor.getColumnIndex(InboxSQLiteHelper.COLUMN_CONVERSATIONS_ID));
		String appId = cursor.getString(cursor.getColumnIndex(InboxSQLiteHelper.COLUMN_USERS_APP_ID));
		int senderUserId = cursor.getInt(cursor.getColumnIndex(InboxSQLiteHelper.COLUMN_MESSAGES_SENDER_USER_ID));
		PendingIntent pendingLaunchIntent = this.pendingLaunchIntentCache.get(Integer.valueOf(senderUserId));
		Conversation conversation = new Conversation(id, appId, senderUserId, cursor.getInt(cursor
				.getColumnIndex(InboxSQLiteHelper.COLUMN_CONVERSATIONS_LAST_MESSAGE_ID)), cursor.getInt(cursor
				.getColumnIndex(InboxSQLiteHelper.COLUMN_CONVERSATIONS_NUM_UNREAD_MESSAGES)));
		conversation.setPendingIntent(pendingLaunchIntent);
		return conversation;
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
	private Message getMessageFromCursor(Cursor cursor) {
		int id = cursor.getInt(cursor.getColumnIndex(InboxSQLiteHelper.COLUMN_MESSAGES_ID));
		String appId = cursor.getString(cursor.getColumnIndex(InboxSQLiteHelper.COLUMN_USERS_APP_ID));
		int senderUserId = cursor.getInt(cursor.getColumnIndex(InboxSQLiteHelper.COLUMN_MESSAGES_SENDER_USER_ID));
		PendingIntent pendingLaunchIntent = this.pendingLaunchIntentCache.get(Integer.valueOf(senderUserId));
		Message message = new Message(id, appId, senderUserId, cursor.getString(cursor
				.getColumnIndex(InboxSQLiteHelper.COLUMN_MESSAGES_BODY)), cursor.getInt(cursor
				.getColumnIndex(InboxSQLiteHelper.COLUMN_MESSAGES_STATE)), cursor.getLong(cursor
				.getColumnIndex(InboxSQLiteHelper.COLUMN_MESSAGES_TIMESTAMP)));
		message.setPendingIntent(pendingLaunchIntent);
		message.setActions(this.actionsCache.get(Integer.valueOf(id)));
		return message;
	}

	private User getUserFromCursor(Cursor cursor) {
		return new User(cursor.getInt(cursor.getColumnIndex(InboxSQLiteHelper.COLUMN_USERS_ID)),
				cursor.getString(cursor.getColumnIndex(InboxSQLiteHelper.COLUMN_USERS_APP_ID)), cursor.getString(cursor
						.getColumnIndex(InboxSQLiteHelper.COLUMN_USERS_APPSPECIFIC_USER_ID)), cursor.getString(cursor
						.getColumnIndex(InboxSQLiteHelper.COLUMN_USERS_DISPLAY_NAME)), cursor.getInt(cursor
						.getColumnIndex(InboxSQLiteHelper.COLUMN_USERS_TYPE)));
	}

	public void reset() {
		this.database.delete(InboxSQLiteHelper.TABLE_CONVERSATIONS, null, null);
		this.database.delete(InboxSQLiteHelper.TABLE_MESSAGES, null, null);
		this.database.delete(InboxSQLiteHelper.TABLE_USERS, null, null);
	}
}

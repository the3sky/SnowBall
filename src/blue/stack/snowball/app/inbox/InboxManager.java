package blue.stack.snowball.app.inbox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.database.Cursor;
import blue.stack.snowball.app.apps.AppManager;
import blue.stack.snowball.app.logging.EventLogger;
import blue.stack.snowball.app.logging.EventLoggerManager;
import blue.stack.snowball.app.photos.ProfilePhotoManager;
import blue.stack.snowball.app.settings.Settings;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class InboxManager {
	private static final String TAG = "InboxManager";
	@Inject
	AppManager appManager;
	@Inject
	Context context;
	@Inject
	EventLoggerManager eventLoggerManager;
	boolean firstInboxEntryAdded;
	Map<String, Message> lastSavedMessages;
	List<InboxListener> listeners;
	InboxPersistence persistence;
	@Inject
	ProfilePhotoManager profilePhotoManager;
	@Inject
	Settings settings;

	@Inject
	private InboxManager() {
		this.listeners = new ArrayList();
	}

	@Inject
	private void start() {
		this.firstInboxEntryAdded = this.settings.getFirstInboxMessageAdded();
		this.lastSavedMessages = new HashMap();
		this.persistence = new InboxPersistence(this.context);
		this.persistence.open();
		DBMigrator.migrate(this.context, this);
	}

	public void stop() {
		if (this.persistence != null) {
			this.persistence.close();
			this.persistence = null;
		}
		this.lastSavedMessages = null;
		this.context = null;
	}

	public void addListener(InboxListener listener) {
		this.listeners.add(listener);
	}

	public void removeListener(InboxListener listener) {
		this.listeners.remove(listener);
	}

	public void saveMesssage(RawMessage message) {
		if (!this.firstInboxEntryAdded) {
			clearAllMessages();
		}
		if (!this.appManager.getAppById(message.getAppId()).shouldRemoveDuplicates() || !isDuplicateMessage(message)) {
			int savedMessageId = this.persistence.saveMessage(message);
			if (savedMessageId != -1) {
				Message savedMessage = getMessage(savedMessageId);
				User sender = savedMessage.getSender();
				if (!(sender == null || message.getProfilePhoto() == null)) {
					this.profilePhotoManager.cacheProfilePhoto(message.getAppId(), sender.getAppSpecificUserId(),
							sender.getDisplayName(), message.getProfilePhoto());
				}
				if (savedMessage != null) {
					this.lastSavedMessages.put(savedMessage.getAppId(), savedMessage);
					fireOnInboxMessageAdded(savedMessage);
				}
			}
		}
	}

	public int injectMessage(RawMessage message) {
		int savedMessageId = this.persistence.saveMessage(message);
		if (message.getProfilePhoto() != null) {
			this.profilePhotoManager.cacheProfilePhoto(message.getAppId(), message.getSenderAppSpecificUserId(),
					message.getSenderDisplayName(), message.getProfilePhoto());
		}
		return savedMessageId;
	}

	public Message getMessage(int messageId) {
		return this.persistence.getMessage(messageId);
	}

	public boolean deleteMessage(Message message) {
		boolean deleted = this.persistence.deleteMessage(message);
		if (deleted) {
			fireOnInboxUpdated();
		}
		return deleted;
	}

	public boolean deleteConversation(Conversation conversation) {
		boolean deleted = this.persistence.deleteConversation(conversation);
		if (deleted) {
			fireOnInboxUpdated();
		}
		return deleted;
	}

	public boolean clearAllMessages() {
		boolean cleared = this.persistence.deleteAllMessages();
		if (cleared) {
			fireOnInboxCleared();
		}
		return cleared;
	}

	public List<Message> getUnreadMessages(int limit) {
		return this.persistence.getUnreadMessages(limit);
	}

	public boolean markMessageAsRead(Message message) {
		boolean updated = this.persistence.markMessageAsRead(message);
		if (updated) {
			fireOnInboxUpdated();
		}
		return updated;
	}

	public boolean markConversationAsRead(Conversation conversation) {
		boolean updated = this.persistence.markConversationAsRead(conversation);
		if (updated) {
			fireOnInboxUpdated();
		}
		return updated;
	}

	public boolean markAllMessagesAsRead() {
		boolean updated = this.persistence.markAllMessageAsRead();
		if (updated) {
			fireOnInboxUpdated();
		}
		return updated;
	}

	public boolean markAppAsRead(String appId) {
		boolean updated = this.persistence.markAppAsRead(appId);
		if (updated) {
			fireOnInboxUpdated();
		}
		return updated;
	}

	public int getLatestMessageId() {
		return this.persistence.getLatestMessageId();
	}

	public Cursor getConversationsView() {
		return this.persistence.getConversationsView();
	}

	public Cursor getMessagesView(int senderUserId) {
		return this.persistence.getMessagesView(senderUserId);
	}

	public Cursor getUnreadMessagesViewSince(int messageId) {
		return this.persistence.getUnreadMessagesViewSince(messageId);
	}

	public Conversation getConversationFromViewCursor(Cursor cursor) {
		return this.persistence.getConversationFromViewCursor(cursor);
	}

	public Message getMessageFromViewCursor(Cursor cursor) {
		return this.persistence.getMessageFromViewCursor(cursor);
	}

	void fireOnInboxUpdated() {
		for (InboxListener listener : this.listeners) {
			listener.onInboxUpdated();
		}
	}

	void fireOnInboxMessageAdded(Message message) {
		EventLogger eventLogger = this.eventLoggerManager.getEventLogger();
		eventLogger.addAppEvent(EventLogger.INBOX_MESSAGE_ADDED, message.getAppId());
		if (!this.firstInboxEntryAdded) {
			this.firstInboxEntryAdded = true;
			this.settings.setFirstInboxMessageAdded(true);
			eventLogger.addAppEvent(Settings.KEY_FIRST_INBOX_MESSAGE_ADDED, message.getAppId());
		}
		for (InboxListener listener : this.listeners) {
			listener.onInboxMessageAdded(message);
		}
	}

	void fireOnInboxCleared() {
		this.lastSavedMessages.clear();
		for (InboxListener listener : this.listeners) {
			listener.onInboxCleared();
		}
	}

	boolean isDuplicateMessage(RawMessage message) {
		Message lastSavedMessage = this.lastSavedMessages.get(message.getAppId());
		if (lastSavedMessage == null) {
			return false;
		}
		String displayName = message.getSenderDisplayName();
		String messageBody = message.getBody();
		String lastDisplayName = lastSavedMessage.getSender().getDisplayName();
		String lastMessageBody = lastSavedMessage.getBody();
		if (displayName == null) {
			displayName = "";
		}
		if (messageBody == null) {
			messageBody = "";
		}
		if (lastDisplayName == null) {
			lastDisplayName = "";
		}
		if (lastMessageBody == null) {
			lastMessageBody = "";
		}
		if (!displayName.equals(lastDisplayName) || !messageBody.equals(lastMessageBody)) {
			return false;
		}
		this.persistence.updatePendingIntent(lastSavedMessage.getSender().getId(), message.getPendingLaunchIntent());
		return true;
	}
}

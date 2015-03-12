package blue.stack.snowball.app.apps;

import java.util.List;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import blue.stack.snowball.app.inbox.Message;
import blue.stack.snowball.app.inbox.RawMessage;
import blue.stack.snowball.app.inbox.User;
import blue.stack.snowball.app.notifications.Notification;
import blue.stack.snowball.app.notifications.NotificationProcessor;
import blue.stack.snowball.app.notifications.NotificationProcessorResult;

public class FBMessengerApp extends BaseApp implements NotificationProcessor {
	private static final int FB_MESSENGER_MSG_ID = 10000;
	public static final String PACKAGE_NAME = "com.facebook.orca";

	@Override
	public String getAppId() {
		return PACKAGE_NAME;
	}

	@Override
	public boolean shouldHandleNotification(Notification n) {
		return n.getPackageName().equals(PACKAGE_NAME) && n.getContentTitle() != null
				&& !n.getContentTitle().equals("") && n.getId() == FB_MESSENGER_MSG_ID;
	}

	@Override
	public Intent getLaunchIntentForMessage(Message message) {
		User sender = message.getSender();
		String conversationId = null;
		if (sender != null) {
			conversationId = sender.getAppSpecificUserId();
		}
		return (conversationId == null || conversationId.equals("")) ? super.getLaunchIntentForMessage(message)
				: getLaunchIntentForConversation(conversationId);
	}

	@Override
	public NotificationProcessorResult processNotification(Notification n) {
		String from = n.getContentTitle();
		String conversationId = n.getTag();
		long timestamp = n.getWhen();
		String messageBody = n.getContentText();
		PendingIntent pendingLaunchIntent = n.getPendingLaunchIntent();
		List actions = n.getActions();
		if (messageBody == null) {
			List<String> inboxText = n.getExpandedInboxText();
			if (inboxText != null && inboxText.size() > 0) {
				messageBody = inboxText.get(inboxText.size() - 1);
			}
		}
		if (conversationId == null || conversationId.equals("")) {
			conversationId = null;
		}
		Bitmap profilePhoto = null;
		if (shouldUseAsProfilePhoto(n.getExpandedLargeIconBig())) {
			profilePhoto = n.getExpandedLargeIconBig();
		} else if (shouldUseAsProfilePhoto(n.getLargeIcon())) {
			profilePhoto = n.getLargeIcon();
		}
		return new NotificationProcessorResult(new RawMessage(n.getPackageName(), conversationId, from, messageBody,
				timestamp, profilePhoto, pendingLaunchIntent, actions), true);
	}

	Intent getLaunchIntentForConversation(String conversationId) {
		ComponentName component = new ComponentName(PACKAGE_NAME,
				"com.facebook.messenger.intents.IntentHandlerActivity");
		Intent intent = new Intent("android.intent.action.VIEW");
		intent.addCategory("android.intent.category.DEFAULT");
		intent.setData(Uri.parse("fb-messenger://thread/" + conversationId));
		intent.setComponent(component);
		return intent;
	}
}

package blue.stack.snowball.app.apps;

import java.util.ArrayList;
import java.util.List;

import blue.stack.snowball.app.R;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.ContactsContract.Data;
import android.util.Log;
import blue.stack.snowball.app.apps.templates.InputFormatBuilder;
import blue.stack.snowball.app.apps.templates.MessageProcessor;
import blue.stack.snowball.app.apps.templates.MessageProcessor.Result;
import blue.stack.snowball.app.apps.templates.MessageProcessorBuilder;
import blue.stack.snowball.app.apps.templates.OutputFormatBuilder;
import blue.stack.snowball.app.apps.templates.PackageResourceLoader;
import blue.stack.snowball.app.apps.templates.TemplateStringBuilder;
import blue.stack.snowball.app.inbox.Message;
import blue.stack.snowball.app.inbox.RawMessage;
import blue.stack.snowball.app.inbox.User;
import blue.stack.snowball.app.notifications.Notification;
import blue.stack.snowball.app.notifications.NotificationProcessor;
import blue.stack.snowball.app.notifications.NotificationProcessorResult;

public class WhatsAppApp extends BaseApp implements NotificationProcessor {
	public static final String PACKAGE_NAME = "com.whatsapp";
	private static final String TAG = "WhatsAppApp";
	private static final String WHATSAPP_CONTACT_MIMETYPE = "vnd.android.cursor.item/vnd.com.whatsapp.profile";
	List<MessageProcessor> messageProcessors;
	List<MessageProcessor> tickerProcessors;

	@Override
	public void start(Context context) {
		super.start(context);
		this.tickerProcessors = new ArrayList();
		this.messageProcessors = new ArrayList();
		if (isAppInstalled()) {
			initTickerProcessors();
			initMessageProcessors();
		}
	}

	@Override
	public void stop() {
		this.tickerProcessors = null;
		this.messageProcessors = null;
		super.stop();
	}

	@Override
	public String getAppId() {
		return PACKAGE_NAME;
	}

	@Override
	public boolean shouldHandleNotification(Notification n) {
		return n.getPackageName().equals(PACKAGE_NAME);
	}

	@Override
	public Intent getLaunchIntentForMessage(Message message) {
		User sender = message.getSender();
		String whatsAppPhoneNumber = null;
		if (sender != null) {
			whatsAppPhoneNumber = sender.getAppSpecificUserId();
		}
		return whatsAppPhoneNumber == null ? super.getLaunchIntentForMessage(message)
				: getLaunchIntentForWhatsAppNumber(whatsAppPhoneNumber);
	}

	@Override
	public NotificationProcessorResult processNotification(Notification n) {
		String messageText;
		List<String> expandedInboxText = n.getExpandedInboxText();
		String fromText = n.getTickerText();
		if (n.getExpandedInboxText() != null && n.getExpandedInboxText().size() > 0) {
			messageText = n.getExpandedInboxText().get(expandedInboxText.size() - 1);
		} else if (n.getContentText() == null) {
			return null;
		} else {
			messageText = n.getContentText();
		}
		Result fromResult = null;
		for (MessageProcessor messageProcessor : this.tickerProcessors) {
			Result r = messageProcessor.processMessage(fromText);
			if (r.matches()) {
				fromResult = r;
				break;
			}
		}
		if (fromResult == null) {
			return null;
		}
		String from = fromResult.getOutputForKey("from");
		if (from == null) {
			return null;
		}
		boolean isGroup = fromResult.getValue("group") != null;
		Result messageResult = null;
		for (MessageProcessor messageProcessor2 : this.messageProcessors) {
			Result r = messageProcessor2.processMessage(messageText);
			if (r.matches()) {
				messageResult = r;
				break;
			}
		}
		if (messageResult == null) {
			return null;
		}
		String messageBody;
		if (isGroup) {
			messageBody = messageResult.getOutputForKey("groupMessage");
		} else {
			messageBody = messageResult.getOutputForKey("message");
		}
		if (messageBody == null) {
			return null;
		}
		long timestamp = n.getWhen();
		String whatsAppNumber = getWhatsAppNumber(from);
		PendingIntent pendingLaunchIntent = n.getPendingLaunchIntent();
		List actions = n.getActions();
		if (whatsAppNumber != null) {
			pendingLaunchIntent = null;
		}
		Bitmap profilePhoto = null;
		if (shouldUseAsProfilePhoto(n.getExpandedLargeIconBig())) {
			profilePhoto = n.getExpandedLargeIconBig();
		} else if (shouldUseAsProfilePhoto(n.getLargeIcon())) {
			profilePhoto = n.getLargeIcon();
		}
		return new NotificationProcessorResult(new RawMessage(n.getPackageName(), whatsAppNumber, from, messageBody,
				timestamp, profilePhoto, pendingLaunchIntent, actions), true);
	}

	@Override
	public boolean shouldRemoveDuplicates() {
		return true;
	}

	Intent getLaunchIntentForWhatsAppNumber(String whatsAppPhoneNumber) {
		ComponentName component = new ComponentName(PACKAGE_NAME, "com.whatsapp.Conversation");
		Intent intent = new Intent("android.intent.action.SENDTO");
		intent.addCategory("android.intent.category.DEFAULT");
		intent.setData(Uri.parse("smsto:" + whatsAppPhoneNumber));
		intent.setComponent(component);
		return intent;
	}

	String getWhatsAppNumber(String name) {
		Cursor cursor = this.context.getContentResolver().query(Data.CONTENT_URI, new String[] { "data3" },
				"display_name=? AND mimetype=?", new String[] { name, WHATSAPP_CONTACT_MIMETYPE }, null);
		String phoneNumber = null;
		int foundContacts = 0;
		while (cursor.moveToNext()) {
			foundContacts++;
			if (foundContacts > 1) {
				Log.d(TAG, "Found more than one WhatsApp contact -- skipping WhatsApp deeplinking");
				phoneNumber = null;
				break;
			}
			phoneNumber = cursor.getString(cursor.getColumnIndex("data3"));
			Log.d(TAG, "Found WhatsApp # " + phoneNumber + " for " + name);
		}
		cursor.close();
		return phoneNumber;
	}

	void initTickerProcessors() {
		String template = PackageResourceLoader.loadStringByResourceName(this.context, PACKAGE_NAME,
				"notification_ticker_header");
		addTickerProcessor(new MessageProcessorBuilder()
				.setInputFormat(
						new InputFormatBuilder()
								.setCanonicalInputTemplateFromPackage(PACKAGE_NAME, "group_subject_changed_by_name")
								.labelToken(1, "sender").labelToken(2, "group").build(this.context))
				.addOutputFormat(
						"from",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_from_group).build(
								this.context)).build(this.context));
		addTickerProcessor(new MessageProcessorBuilder()
				.setInputFormat(
						new InputFormatBuilder().setCanonicalInputTemplate(template.replace("%s", "%1$s @ %2$s"))
								.labelToken(1, "sender").labelToken(2, "group").build(this.context))
				.addOutputFormat(
						"from",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_from_group).build(
								this.context)).build(this.context));
		addTickerProcessor(new MessageProcessorBuilder()
				.setInputFormat(
						new InputFormatBuilder().setCanonicalInputTemplate(template.replace("%s", "%1$s"))
								.labelToken(1, "sender").build(this.context))
				.addOutputFormat(
						"from",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_from).build(
								this.context)).build(this.context));
	}

	void initMessageProcessors() {
		addMessageProcessor(new MessageProcessorBuilder()
				.setInputFormat(
						new InputFormatBuilder()
								.setCanonicalInputTemplate(
										new TemplateStringBuilder().addString("%1$s @ %2$s: ")
												.addString(PACKAGE_NAME, "conversations_most_recent_image")
												.build(this.context)).labelToken(1, "sender").labelToken(2, "group")
								.build(this.context))
				.addOutputFormat(
						"groupMessage",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_group_message_photo)
								.build(this.context)).build(this.context));
		addMessageProcessor(new MessageProcessorBuilder()
				.setInputFormat(
						new InputFormatBuilder()
								.setCanonicalInputTemplate(
										new TemplateStringBuilder().addString("%1$s: ")
												.addString(PACKAGE_NAME, "conversations_most_recent_image")
												.build(this.context)).labelToken(1, "sender").build(this.context))
				.addOutputFormat(
						"message",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_message_photo).build(
								this.context))
				.addOutputFormat(
						"groupMessage",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_group_message_photo)
								.build(this.context)).build(this.context));
		addMessageProcessor(new MessageProcessorBuilder()
				.setInputFormat(
						new InputFormatBuilder().setCanonicalInputTemplate(
								new TemplateStringBuilder().addString(PACKAGE_NAME, "conversations_most_recent_image")
										.build(this.context)).build(this.context))
				.addOutputFormat(
						"message",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_message_photo).build(
								this.context)).build(this.context));
		addMessageProcessor(new MessageProcessorBuilder()
				.setInputFormat(
						new InputFormatBuilder()
								.setCanonicalInputTemplate(
										new TemplateStringBuilder().addString("%1$s @ %2$s: ")
												.addString(PACKAGE_NAME, "conversations_most_recent_video")
												.build(this.context)).labelToken(1, "sender").labelToken(2, "group")
								.build(this.context))
				.addOutputFormat(
						"groupMessage",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_group_message_video)
								.build(this.context)).build(this.context));
		addMessageProcessor(new MessageProcessorBuilder()
				.setInputFormat(
						new InputFormatBuilder()
								.setCanonicalInputTemplate(
										new TemplateStringBuilder().addString("%1$s: ")
												.addString(PACKAGE_NAME, "conversations_most_recent_video")
												.build(this.context)).labelToken(1, "sender").build(this.context))
				.addOutputFormat(
						"message",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_message_video).build(
								this.context))
				.addOutputFormat(
						"groupMessage",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_group_message_video)
								.build(this.context)).build(this.context));
		addMessageProcessor(new MessageProcessorBuilder()
				.setInputFormat(
						new InputFormatBuilder().setCanonicalInputTemplate(
								new TemplateStringBuilder().addString(PACKAGE_NAME, "conversations_most_recent_video")
										.build(this.context)).build(this.context))
				.addOutputFormat(
						"message",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_message_video).build(
								this.context)).build(this.context));
		addMessageProcessor(new MessageProcessorBuilder()
				.setInputFormat(
						new InputFormatBuilder()
								.setCanonicalInputTemplate(
										new TemplateStringBuilder().addString("%1$s @ %2$s: ")
												.addString(PACKAGE_NAME, "conversations_most_recent_voice")
												.build(this.context)).labelToken(1, "sender").labelToken(2, "group")
								.build(this.context))
				.addOutputFormat(
						"groupMessage",
						new OutputFormatBuilder().setOutputTemplateFromResouce(
								R.string.entry_of_group_message_voice_note).build(this.context)).build(this.context));
		addMessageProcessor(new MessageProcessorBuilder()
				.setInputFormat(
						new InputFormatBuilder()
								.setCanonicalInputTemplate(
										new TemplateStringBuilder().addString("%1$s: ")
												.addString(PACKAGE_NAME, "conversations_most_recent_voice")
												.build(this.context)).labelToken(1, "sender").build(this.context))
				.addOutputFormat(
						"message",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_message_voice_note)
								.build(this.context))
				.addOutputFormat(
						"groupMessage",
						new OutputFormatBuilder().setOutputTemplateFromResouce(
								R.string.entry_of_group_message_voice_note).build(this.context)).build(this.context));
		addMessageProcessor(new MessageProcessorBuilder()
				.setInputFormat(
						new InputFormatBuilder().setCanonicalInputTemplate(
								new TemplateStringBuilder().addString(PACKAGE_NAME, "conversations_most_recent_voice")
										.build(this.context)).build(this.context))
				.addOutputFormat(
						"message",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_message_voice_note)
								.build(this.context)).build(this.context));
		addMessageProcessor(new MessageProcessorBuilder()
				.setInputFormat(
						new InputFormatBuilder()
								.setCanonicalInputTemplate(
										new TemplateStringBuilder().addString("%1$s @ %2$s: ")
												.addString(PACKAGE_NAME, "conversations_most_recent_contact")
												.build(this.context)).labelToken(1, "sender").labelToken(2, "group")
								.build(this.context))
				.addOutputFormat(
						"groupMessage",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_group_message_contact)
								.build(this.context)).build(this.context));
		addMessageProcessor(new MessageProcessorBuilder()
				.setInputFormat(
						new InputFormatBuilder()
								.setCanonicalInputTemplate(
										new TemplateStringBuilder().addString("%1$s: ")
												.addString(PACKAGE_NAME, "conversations_most_recent_contact")
												.build(this.context)).labelToken(1, "sender").build(this.context))
				.addOutputFormat(
						"message",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_message_contact)
								.build(this.context))
				.addOutputFormat(
						"groupMessage",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_group_message_contact)
								.build(this.context)).build(this.context));
		addMessageProcessor(new MessageProcessorBuilder()
				.setInputFormat(
						new InputFormatBuilder().setCanonicalInputTemplate(
								new TemplateStringBuilder()
										.addString(PACKAGE_NAME, "conversations_most_recent_contact").build(
												this.context)).build(this.context))
				.addOutputFormat(
						"message",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_message_contact)
								.build(this.context)).build(this.context));
		addMessageProcessor(new MessageProcessorBuilder()
				.setInputFormat(
						new InputFormatBuilder()
								.setCanonicalInputTemplate(
										new TemplateStringBuilder().addString("%1$s @ %2$s: ")
												.addString(PACKAGE_NAME, "conversations_most_recent_audio")
												.build(this.context)).labelToken(1, "sender").labelToken(2, "group")
								.build(this.context))
				.addOutputFormat(
						"groupMessage",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_group_message_audio)
								.build(this.context)).build(this.context));
		addMessageProcessor(new MessageProcessorBuilder()
				.setInputFormat(
						new InputFormatBuilder()
								.setCanonicalInputTemplate(
										new TemplateStringBuilder().addString("%1$s: ")
												.addString(PACKAGE_NAME, "conversations_most_recent_audio")
												.build(this.context)).labelToken(1, "sender").build(this.context))
				.addOutputFormat(
						"message",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_message_audio).build(
								this.context))
				.addOutputFormat(
						"groupMessage",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_group_message_audio)
								.build(this.context)).build(this.context));
		addMessageProcessor(new MessageProcessorBuilder()
				.setInputFormat(
						new InputFormatBuilder().setCanonicalInputTemplate(
								new TemplateStringBuilder().addString(PACKAGE_NAME, "conversations_most_recent_audio")
										.build(this.context)).build(this.context))
				.addOutputFormat(
						"message",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_message_audio).build(
								this.context)).build(this.context));
		addMessageProcessor(new MessageProcessorBuilder()
				.setInputFormat(
						new InputFormatBuilder()
								.setCanonicalInputTemplate(
										new TemplateStringBuilder().addString("%1$s @ %2$s: ")
												.addString(PACKAGE_NAME, "conversations_most_recent_location")
												.build(this.context)).labelToken(1, "sender").labelToken(2, "group")
								.build(this.context))
				.addOutputFormat(
						"groupMessage",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_group_message_map)
								.build(this.context)).build(this.context));
		addMessageProcessor(new MessageProcessorBuilder()
				.setInputFormat(
						new InputFormatBuilder()
								.setCanonicalInputTemplate(
										new TemplateStringBuilder().addString("%1$s: ")
												.addString(PACKAGE_NAME, "conversations_most_recent_location")
												.build(this.context)).labelToken(1, "sender").build(this.context))
				.addOutputFormat(
						"message",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_message_map).build(
								this.context))
				.addOutputFormat(
						"groupMessage",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_group_message_map)
								.build(this.context)).build(this.context));
		addMessageProcessor(new MessageProcessorBuilder()
				.setInputFormat(
						new InputFormatBuilder().setCanonicalInputTemplate(
								new TemplateStringBuilder().addString(PACKAGE_NAME,
										"conversations_most_recent_location").build(this.context)).build(this.context))
				.addOutputFormat(
						"message",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_message_map).build(
								this.context)).build(this.context));
		addMessageProcessor(new MessageProcessorBuilder()
				.setInputFormat(
						new InputFormatBuilder()
								.setCanonicalInputTemplateFromPackage(PACKAGE_NAME, "group_subject_changed_by_name")
								.labelToken(1, "sender").labelToken(2, "group").build(this.context))
				.addOutputFormat(
						"groupMessage",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_group_edit_name)
								.build(this.context)).build(this.context));
		addMessageProcessor(new MessageProcessorBuilder()
				.setInputFormat(
						new InputFormatBuilder().setCanonicalInputTemplate("%1$s @ %2$s: %3$s").labelToken(1, "sender")
								.labelToken(2, "group").labelToken(3, "message").build(this.context))
				.addOutputFormat(
						"groupMessage",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_group_message_text)
								.build(this.context)).build(this.context));
		addMessageProcessor(new MessageProcessorBuilder()
				.setInputFormat(
						new InputFormatBuilder().setCanonicalInputTemplate("%1$s: %2$s").labelToken(1, "sender")
								.labelToken(2, "message").build(this.context))
				.addOutputFormat(
						"message",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_message_text).build(
								this.context))
				.addOutputFormat(
						"groupMessage",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_group_message_text)
								.build(this.context)).build(this.context));
		addMessageProcessor(new MessageProcessorBuilder()
				.setInputFormat(
						new InputFormatBuilder().setCanonicalInputTemplate("%1$s").labelToken(1, "message")
								.build(this.context))
				.addOutputFormat(
						"message",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_message_text).build(
								this.context)).build(this.context));
	}

	void addTickerProcessor(MessageProcessor processor) {
		if (processor != null) {
			this.tickerProcessors.add(processor);
		}
	}

	void addMessageProcessor(MessageProcessor processor) {
		if (processor != null) {
			this.messageProcessors.add(processor);
		}
	}
}

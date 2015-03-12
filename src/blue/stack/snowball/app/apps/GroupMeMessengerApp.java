package blue.stack.snowball.app.apps;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.PendingIntent;
import android.content.Context;
import android.graphics.Bitmap;
import blue.stack.snowball.app.R;
import blue.stack.snowball.app.apps.templates.InputFormatBuilder;
import blue.stack.snowball.app.apps.templates.MessageProcessor;
import blue.stack.snowball.app.apps.templates.MessageProcessor.Result;
import blue.stack.snowball.app.apps.templates.MessageProcessorBuilder;
import blue.stack.snowball.app.apps.templates.OutputFormatBuilder;
import blue.stack.snowball.app.inbox.RawMessage;
import blue.stack.snowball.app.notifications.Notification;
import blue.stack.snowball.app.notifications.NotificationProcessor;
import blue.stack.snowball.app.notifications.NotificationProcessorResult;

public class GroupMeMessengerApp extends BaseApp implements NotificationProcessor {
	private static final String ADDED_TO_GROUP_FMT = "You've been added to \"%1$s\" with %2$s.";
	public static final String PACKAGE_NAME = "com.groupme.android";
	List<MessageProcessor> messageProcessors;

	@Override
	public void start(Context context) {
		super.start(context);
		this.messageProcessors = new ArrayList();
		if (isAppInstalled()) {
			initMessageProcessors();
		}
	}

	@Override
	public void stop() {
		this.messageProcessors = null;
		super.stop();
	}

	@Override
	public String getAppId() {
		return PACKAGE_NAME;
	}

	@Override
	public boolean shouldHandleNotification(Notification n) {
		return (!n.getPackageName().equals(PACKAGE_NAME) || n.getTickerText() == null || n.getTickerText().equals("") || n
				.getTickerText().length() == 0) ? false : true;
	}

	@Override
	public NotificationProcessorResult processNotification(Notification n) {
		String tickerText = n.getTickerText();
		String contentTitle = n.getContentTitle();
		String contentText = n.getContentText();
		List<String> expandedInboxText = n.getExpandedInboxText();
		if (expandedInboxText != null && expandedInboxText.size() > 0) {
			String expandedInboxTextFirst = expandedInboxText.get(0);
		}
		Map<String, String> inputs = new HashMap();
		inputs.put("tickerText", tickerText);
		inputs.put("contentTitle", contentTitle);
		inputs.put("contentText", contentText);
		Result result = null;
		for (MessageProcessor messageProcessor : this.messageProcessors) {
			result = messageProcessor.processMessage(inputs);
			if (result.matches()) {
				break;
			}
		}
		if (result == null || !result.matches()) {
			return null;
		}
		String from = result.getOutputForKey("from");
		String messageBody = result.getOutputForKey("message");
		if (from == null || messageBody == null) {
			return null;
		}
		long timestamp = n.getWhen();
		PendingIntent pendingLaunchIntent = n.getPendingLaunchIntent();
		List actions = n.getActions();
		Bitmap profilePhoto = null;
		if (shouldUseAsProfilePhoto(n.getExpandedLargeIconBig())) {
			profilePhoto = n.getExpandedLargeIconBig();
		} else if (shouldUseAsProfilePhoto(n.getLargeIcon())) {
			profilePhoto = n.getLargeIcon();
		}
		return new NotificationProcessorResult(new RawMessage(n.getPackageName(), null, from, messageBody, timestamp,
				profilePhoto, pendingLaunchIntent, actions), true);
	}

	void initMessageProcessors() {
		addMessageProcessor(new MessageProcessorBuilder()
				.addInputFormat(
						"tickerText",
						new InputFormatBuilder().setCanonicalInputTemplate(ADDED_TO_GROUP_FMT).labelToken(1, "group")
								.labelToken(2, "members").build(this.context))
				.addOutputFormat(
						"from",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_from_group).build(
								this.context))
				.addOutputFormat(
						"message",
						new OutputFormatBuilder().setOutputTemplateFromResouce(
								R.string.groupme_entry_of_message_addedtogroup).build(this.context))
				.build(this.context));
		addMessageProcessor(new MessageProcessorBuilder()
				.addInputFormat(
						"contentTitle",
						new InputFormatBuilder().setCanonicalInputTemplate("%1$s").labelToken(1, "group")
								.build(this.context))
				.addInputFormat(
						"contentText",
						new InputFormatBuilder()
								.setCanonicalInputTemplateFromPackage(PACKAGE_NAME, "last_message_photo")
								.labelToken(1, "sender").build(this.context))
				.addOutputFormat(
						"from",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_from_group).build(
								this.context))
				.addOutputFormat(
						"message",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_group_message_photo)
								.build(this.context)).build(this.context));
		addMessageProcessor(new MessageProcessorBuilder()
				.addInputFormat(
						"contentTitle",
						new InputFormatBuilder().setCanonicalInputTemplate("%1$s").labelToken(1, "sender")
								.build(this.context))
				.addInputFormat(
						"contentText",
						new InputFormatBuilder().setCanonicalInputTemplateFromPackage(PACKAGE_NAME,
								"notif_shared_photo").build(this.context))
				.addOutputFormat(
						"from",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_from).build(
								this.context))
				.addOutputFormat(
						"message",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_message_photo).build(
								this.context)).build(this.context));
		addMessageProcessor(new MessageProcessorBuilder()
				.addInputFormat(
						"tickerText",
						new InputFormatBuilder()
								.setCanonicalInputTemplateFromPackage(PACKAGE_NAME, "notif_liked_photo_ticker_group")
								.labelToken(1, "sender2").labelToken(2, "group").build(this.context))
				.addInputFormat(
						"contentTitle",
						new InputFormatBuilder().setCanonicalInputTemplate("%1$s").labelToken(1, "sender")
								.build(this.context))
				.addOutputFormat(
						"from",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_from_group).build(
								this.context))
				.addOutputFormat(
						"message",
						new OutputFormatBuilder().setOutputTemplateFromResouce(
								R.string.entry_of_group_message_liked_photo).build(this.context)).build(this.context));
		addMessageProcessor(new MessageProcessorBuilder()
				.addInputFormat(
						"contentTitle",
						new InputFormatBuilder().setCanonicalInputTemplate("%1$s").labelToken(1, "sender")
								.build(this.context))
				.addInputFormat(
						"contentText",
						new InputFormatBuilder()
								.setCanonicalInputTemplateFromPackage(PACKAGE_NAME, "notif_liked_photo").build(
										this.context))
				.addOutputFormat(
						"from",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_from).build(
								this.context))
				.addOutputFormat(
						"message",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_message_liked_photo)
								.build(this.context)).build(this.context));
		addMessageProcessor(new MessageProcessorBuilder()
				.addInputFormat(
						"contentTitle",
						new InputFormatBuilder().setCanonicalInputTemplate("%1$s").labelToken(1, "group")
								.build(this.context))
				.addInputFormat(
						"contentText",
						new InputFormatBuilder()
								.setCanonicalInputTemplateFromPackage(PACKAGE_NAME, "last_message_video")
								.labelToken(1, "sender").build(this.context))
				.addOutputFormat(
						"from",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_from_group).build(
								this.context))
				.addOutputFormat(
						"message",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_group_message_video)
								.build(this.context)).build(this.context));
		addMessageProcessor(new MessageProcessorBuilder()
				.addInputFormat(
						"contentTitle",
						new InputFormatBuilder().setCanonicalInputTemplate("%1$s").labelToken(1, "sender")
								.build(this.context))
				.addInputFormat(
						"contentText",
						new InputFormatBuilder().setCanonicalInputTemplateFromPackage(PACKAGE_NAME,
								"notif_shared_video").build(this.context))
				.addOutputFormat(
						"from",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_from).build(
								this.context))
				.addOutputFormat(
						"message",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_message_video).build(
								this.context)).build(this.context));
		addMessageProcessor(new MessageProcessorBuilder()
				.addInputFormat(
						"contentTitle",
						new InputFormatBuilder().setCanonicalInputTemplate("%1$s").labelToken(1, "group")
								.build(this.context))
				.addInputFormat(
						"contentText",
						new InputFormatBuilder()
								.setCanonicalInputTemplateFromPackage(PACKAGE_NAME, "last_message_location")
								.labelToken(1, "sender").build(this.context))
				.addOutputFormat(
						"from",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_from_group).build(
								this.context))
				.addOutputFormat(
						"message",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_group_message_map)
								.build(this.context)).build(this.context));
		addMessageProcessor(new MessageProcessorBuilder()
				.addInputFormat(
						"contentTitle",
						new InputFormatBuilder().setCanonicalInputTemplate("%1$s").labelToken(1, "sender")
								.build(this.context))
				.addInputFormat(
						"contentText",
						new InputFormatBuilder().setCanonicalInputTemplateFromPackage(PACKAGE_NAME,
								"notif_shared_location").build(this.context))
				.addOutputFormat(
						"from",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_from).build(
								this.context))
				.addOutputFormat(
						"message",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_message_map).build(
								this.context)).build(this.context));
		addMessageProcessor(new MessageProcessorBuilder()
				.addInputFormat(
						"tickerText",
						new InputFormatBuilder()
								.setCanonicalInputTemplateFromPackage(PACKAGE_NAME, "notif_liked_location_ticker_group")
								.labelToken(1, "sender2").labelToken(2, "group").build(this.context))
				.addInputFormat(
						"contentTitle",
						new InputFormatBuilder().setCanonicalInputTemplate("%1$s").labelToken(1, "sender")
								.build(this.context))
				.addInputFormat(
						"contentText",
						new InputFormatBuilder()
								.setCanonicalInputTemplateFromPackage(PACKAGE_NAME, "notif_liked_location")
								.labelToken(1, "location").build(this.context))
				.addOutputFormat(
						"from",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_from_group).build(
								this.context))
				.addOutputFormat(
						"message",
						new OutputFormatBuilder().setOutputTemplateFromResouce(
								R.string.groupme_entry_of_group_message_liked_location).build(this.context))
				.build(this.context));
		addMessageProcessor(new MessageProcessorBuilder()
				.addInputFormat(
						"contentTitle",
						new InputFormatBuilder().setCanonicalInputTemplate("%1$s").labelToken(1, "sender")
								.build(this.context))
				.addInputFormat(
						"contentText",
						new InputFormatBuilder()
								.setCanonicalInputTemplateFromPackage(PACKAGE_NAME, "notif_liked_location")
								.labelToken(1, "location").build(this.context))
				.addOutputFormat(
						"from",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_from).build(
								this.context))
				.addOutputFormat(
						"message",
						new OutputFormatBuilder().setOutputTemplateFromResouce(
								R.string.groupme_entry_of_message_liked_location).build(this.context))
				.build(this.context));
		addMessageProcessor(new MessageProcessorBuilder()
				.addInputFormat(
						"contentTitle",
						new InputFormatBuilder().setCanonicalInputTemplate("%1$s").labelToken(1, "sender")
								.build(this.context))
				.addInputFormat(
						"contentText",
						new InputFormatBuilder()
								.setCanonicalInputTemplateFromPackage(PACKAGE_NAME, "notif_liked_message")
								.labelToken(1, "message").labelToken(2, "group").build(this.context))
				.addOutputFormat(
						"from",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_from_group).build(
								this.context))
				.addOutputFormat(
						"message",
						new OutputFormatBuilder().setOutputTemplateFromResouce(
								R.string.groupme_entry_of_group_message_liked_message).build(this.context))
				.build(this.context));
		addMessageProcessor(new MessageProcessorBuilder()
				.addInputFormat(
						"contentTitle",
						new InputFormatBuilder().setCanonicalInputTemplate("%1$s").labelToken(1, "sender")
								.build(this.context))
				.addInputFormat(
						"contentText",
						new InputFormatBuilder()
								.setCanonicalInputTemplateFromPackage(PACKAGE_NAME, "notif_liked_direct_message")
								.labelToken(1, "message").build(this.context))
				.addOutputFormat(
						"from",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_from).build(
								this.context))
				.addOutputFormat(
						"message",
						new OutputFormatBuilder().setOutputTemplateFromResouce(
								R.string.groupme_entry_of_message_liked_message).build(this.context))
				.build(this.context));
		addMessageProcessor(new MessageProcessorBuilder()
				.addInputFormat(
						"contentTitle",
						new InputFormatBuilder().setCanonicalInputTemplate("%1$s").labelToken(1, "sender")
								.build(this.context))
				.addInputFormat(
						"contentText",
						new InputFormatBuilder().setCanonicalInputTemplate("%1$s").labelToken(1, "message")
								.build(this.context))
				.addOutputFormat(
						"from",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_from).build(
								this.context))
				.addOutputFormat(
						"message",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_message_text).build(
								this.context)).build(this.context));
	}

	void addMessageProcessor(MessageProcessor messageProcessor) {
		if (messageProcessor != null) {
			this.messageProcessors.add(messageProcessor);
		}
	}
}

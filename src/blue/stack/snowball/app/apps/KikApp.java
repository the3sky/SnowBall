package blue.stack.snowball.app.apps;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import blue.stack.snowball.app.R;
import android.app.PendingIntent;
import android.content.Context;
import android.graphics.Bitmap;
import blue.stack.snowball.app.apps.templates.InputFormatBuilder;
import blue.stack.snowball.app.apps.templates.MessageProcessor;
import blue.stack.snowball.app.apps.templates.MessageProcessor.Result;
import blue.stack.snowball.app.apps.templates.MessageProcessorBuilder;
import blue.stack.snowball.app.apps.templates.OutputFormatBuilder;
import blue.stack.snowball.app.inbox.RawMessage;
import blue.stack.snowball.app.notifications.Notification;
import blue.stack.snowball.app.notifications.NotificationProcessor;
import blue.stack.snowball.app.notifications.NotificationProcessorResult;

public class KikApp extends BaseApp implements NotificationProcessor {
	public static final String PACKAGE_NAME = "kik.android";
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
		return n.getPackageName().equals(PACKAGE_NAME);
	}

	@Override
	public NotificationProcessorResult processNotification(Notification n) {
		String contentTitle = n.getContentTitle();
		String contentText = n.getContentText();
		Map inputs = new HashMap();
		inputs.put("contentTitle", contentTitle);
		inputs.put("contentText", contentText);
		Result result = null;
		for (MessageProcessor messageProcessor : this.messageProcessors) {
			result = messageProcessor.processMessage(inputs);
			if (result.matches()) {
				break;
			}
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
		addTransform(new MessageProcessorBuilder()
				.addInputFormat(
						"contentText",
						new InputFormatBuilder()
								.setCanonicalInputTemplateFromPackage(PACKAGE_NAME,
										"notification_ticker_new_convo_with_").labelToken(1, "sender")
								.build(this.context))
				.addOutputFormat(
						"from",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_from).build(
								this.context))
				.addOutputFormat(
						"message",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_message_notext).build(
								this.context)).build(this.context));
		addTransform(new MessageProcessorBuilder()
				.addInputFormat(
						"contentTitle",
						new InputFormatBuilder().setCanonicalInputTemplate("%1$s").labelToken(1, "sender")
								.build(this.context))
				.addInputFormat(
						"contentText",
						new InputFormatBuilder().setCanonicalInputTemplateFromPackage(PACKAGE_NAME,
								"notification_new_picture_message").build(this.context))
				.addOutputFormat(
						"from",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_from).build(
								this.context))
				.addOutputFormat(
						"message",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_message_photo).build(
								this.context)).build(this.context));
		addTransform(new MessageProcessorBuilder()
				.addInputFormat(
						"contentTitle",
						new InputFormatBuilder().setCanonicalInputTemplate("%1$s").labelToken(1, "group")
								.build(this.context))
				.addInputFormat(
						"contentText",
						new InputFormatBuilder()
								.setCanonicalInputTemplateFromPackage(PACKAGE_NAME, "notification_new_app_message")
								.labelToken(1, "sender").build(this.context))
				.addOutputFormat(
						"from",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_from_group).build(
								this.context))
				.addOutputFormat(
						"message",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_group_message_notext)
								.build(this.context)).build(this.context));
		addTransform(new MessageProcessorBuilder()
				.addInputFormat(
						"contentTitle",
						new InputFormatBuilder().setCanonicalInputTemplateFromPackage(PACKAGE_NAME,
								"notification_new_message").build(this.context))
				.addOutputFormat(
						"from",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.kik_entry_of_from_app).build(
								this.context))
				.addOutputFormat(
						"message",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.kik_entry_of_message_new)
								.build(this.context)).build(this.context));
		addTransform(new MessageProcessorBuilder()
				.addInputFormat(
						"contentTitle",
						new InputFormatBuilder().setCanonicalInputTemplateFromPackage(PACKAGE_NAME,
								"notification_one_message").build(this.context))
				.addOutputFormat(
						"from",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.kik_entry_of_from_app).build(
								this.context))
				.addOutputFormat(
						"message",
						new OutputFormatBuilder().setOutputTemplateFromResouce(
								R.string.kik_entry_of_message_one_unread_conversation).build(this.context))
				.build(this.context));
		addTransform(new MessageProcessorBuilder()
				.addInputFormat(
						"contentTitle",
						new InputFormatBuilder()
								.setCanonicalInputTemplateFromPackage(PACKAGE_NAME, "notification_multiple_messages")
								.labelToken(1, "unreadCount").build(this.context))
				.addOutputFormat(
						"from",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.kik_entry_of_from_app).build(
								this.context))
				.addOutputFormat(
						"message",
						new OutputFormatBuilder().setOutputTemplateFromResouce(
								R.string.kik_entry_of_message_unread_conversations).build(this.context))
				.build(this.context));
		addTransform(new MessageProcessorBuilder().addInputFormat("contentTitle",
				new InputFormatBuilder().setInputTemplate("Kik").build(this.context)).build(this.context));
		addTransform(new MessageProcessorBuilder().addInputFormat("contentText",
				new InputFormatBuilder().setInputTemplate("").build(this.context)).build(this.context));
		addTransform(new MessageProcessorBuilder()
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

	void addTransform(MessageProcessor t) {
		if (t != null) {
			this.messageProcessors.add(t);
		}
	}
}

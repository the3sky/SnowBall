package blue.stack.snowball.app.apps;

import java.util.ArrayList;
import java.util.List;

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

public class ViberApp extends BaseApp implements NotificationProcessor {
	public static final String PACKAGE_NAME = "com.viber.voip";
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
		return (!n.getPackageName().equals(PACKAGE_NAME) || n.getTickerText() == null
				|| n.getTickerText().equals("") || n.getTickerText().length() == 0) ? false : true;
	}

	@Override
	public NotificationProcessorResult processNotification(Notification n) {
		String tickerText = n.getTickerText();
		Result result = null;
		for (MessageProcessor messageProcessor : this.messageProcessors) {
			result = messageProcessor.processMessage(tickerText);
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
		addMessageProcessor(new MessageProcessorBuilder()
				.setInputFormat(
						new InputFormatBuilder()
								.setInputTemplateFromPackage(PACKAGE_NAME, "message_notification_new_group_ticker")
								.addInputToken("${user}", "sender").addInputToken("${group}", "group")
								.build(this.context))
				.addOutputFormat(
						"from",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_from_group).build(
								this.context))
				.addOutputFormat(
						"message",
						new OutputFormatBuilder().setOutputTemplateFromResouce(
								R.string.entry_of_group_invited_you_and_sentmsg).build(this.context))
				.build(this.context));
		addMessageProcessor(new MessageProcessorBuilder()
				.setInputFormat(
						new InputFormatBuilder()
								.setInputTemplateFromPackage(PACKAGE_NAME, "message_notification_new_message_in_group")
								.addInputToken("${sender}", "sender").addInputToken("${group}", "group")
								.addInputToken("${message}", "message").build(this.context))
				.addOutputFormat(
						"from",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_from_group).build(
								this.context))
				.addOutputFormat(
						"message",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_group_message_text)
								.build(this.context)).build(this.context));
		addMessageProcessor(new MessageProcessorBuilder()
				.setInputFormat(
						new InputFormatBuilder()
								.setInputTemplateFromPackage(PACKAGE_NAME, "message_notification_smart_messages_group")
								.addInputToken("%s", "sender").build(this.context))
				.addOutputFormat(
						"from",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_from).build(
								this.context))
				.addOutputFormat(
						"message",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_message_new_waiting)
								.build(this.context)).build(this.context));
		addMessageProcessor(new MessageProcessorBuilder()
				.setInputFormat(
						new InputFormatBuilder()
								.setInputTemplateFromPackage(PACKAGE_NAME, "message_notification_smart_messages_one")
								.addInputToken("%s", "sender").build(this.context))
				.addOutputFormat(
						"from",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_from).build(
								this.context))
				.addOutputFormat(
						"message",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_message_new_waiting)
								.build(this.context)).build(this.context));
		addMessageProcessor(new MessageProcessorBuilder()
				.setInputFormat(
						new InputFormatBuilder()
								.setInputTemplateFromPackage(PACKAGE_NAME,
										"message_notification_group_renamed_full_ticker").addInputToken("%s", "skip")
								.addInputToken("%s", "group").addInputToken("%s", "sender").build(this.context))
				.addOutputFormat(
						"from",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_from_group).build(
								this.context))
				.addOutputFormat(
						"message",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_group_edit_name)
								.build(this.context)).build(this.context));
		addMessageProcessor(new MessageProcessorBuilder()
				.setInputFormat(
						new InputFormatBuilder().setCanonicalInputTemplate("%1$s: %2$s").labelToken(1, "sender")
								.labelToken(2, "message").build(this.context))
				.addOutputFormat(
						"from",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_from).build(
								this.context))
				.addOutputFormat(
						"message",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_message_text).build(
								this.context)).build(this.context));
	}

	void addMessageProcessor(MessageProcessor t) {
		if (t != null) {
			this.messageProcessors.add(t);
		}
	}
}

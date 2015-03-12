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
import blue.stack.snowball.app.apps.templates.TemplateStringBuilder;
import blue.stack.snowball.app.inbox.RawMessage;
import blue.stack.snowball.app.notifications.Notification;
import blue.stack.snowball.app.notifications.NotificationProcessor;
import blue.stack.snowball.app.notifications.NotificationProcessorResult;

public class KakaoTalkApp extends BaseApp implements NotificationProcessor {
	public static final String PACKAGE_NAME = "com.kakao.talk";
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
				.setInputFormat(
						new InputFormatBuilder()
								.setInputTemplate(
										new TemplateStringBuilder()
												.addString(PACKAGE_NAME, "message_for_notification_new_message")
												.replaceTokenWithString("{msg}", PACKAGE_NAME,
														"message_for_chatlog_audio").build(this.context))
								.addInputToken("{sender}", "sender").build(this.context))
				.addOutputFormat(
						"from",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_from).build(
								this.context))
				.addOutputFormat(
						"message",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_message_voice_note)
								.build(this.context)).build(this.context));
		addMessageProcessor(new MessageProcessorBuilder()
				.setInputFormat(
						new InputFormatBuilder()
								.setInputTemplate(
										new TemplateStringBuilder()
												.addString(PACKAGE_NAME, "message_for_notification_new_message")
												.replaceTokenWithString("{msg}", PACKAGE_NAME,
														"message_for_chatlog_contact").build(this.context))
								.addInputToken("{sender}", "sender").build(this.context))
				.addOutputFormat(
						"from",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_from).build(
								this.context))
				.addOutputFormat(
						"message",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_message_contact)
								.build(this.context)).build(this.context));
		addMessageProcessor(new MessageProcessorBuilder()
				.setInputFormat(
						new InputFormatBuilder()
								.setInputTemplate(
										new TemplateStringBuilder()
												.addString(PACKAGE_NAME, "message_for_notification_new_message")
												.replaceTokenWithString("{msg}", PACKAGE_NAME,
														"text_for_kakaotalk_profile").build(this.context))
								.addInputToken("{sender}", "sender").build(this.context))
				.addOutputFormat(
						"from",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_from).build(
								this.context))
				.addOutputFormat(
						"message",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_message_contact)
								.build(this.context)).build(this.context));
		addMessageProcessor(new MessageProcessorBuilder()
				.setInputFormat(
						new InputFormatBuilder()
								.setInputTemplate(
										new TemplateStringBuilder()
												.addString(PACKAGE_NAME, "message_for_notification_new_message")
												.replaceTokenWithString("{msg}", PACKAGE_NAME,
														"message_for_chatlog_photo").build(this.context))
								.addInputToken("{sender}", "sender").build(this.context))
				.addOutputFormat(
						"from",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_from).build(
								this.context))
				.addOutputFormat(
						"message",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_message_photo).build(
								this.context)).build(this.context));
		addMessageProcessor(new MessageProcessorBuilder()
				.setInputFormat(
						new InputFormatBuilder()
								.setInputTemplate(
										new TemplateStringBuilder()
												.addString(PACKAGE_NAME, "message_for_notification_new_message")
												.replaceTokenWithString("{msg}", PACKAGE_NAME,
														"message_for_chatlog_video").build(this.context))
								.addInputToken("{sender}", "sender").build(this.context))
				.addOutputFormat(
						"from",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_from).build(
								this.context))
				.addOutputFormat(
						"message",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_message_video).build(
								this.context)).build(this.context));
		addMessageProcessor(new MessageProcessorBuilder()
				.setInputFormat(
						new InputFormatBuilder()
								.setInputTemplateFromPackage(PACKAGE_NAME, "message_for_notification_new_message")
								.addInputToken("{sender}", "sender").addInputToken("{msg}", "message")
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

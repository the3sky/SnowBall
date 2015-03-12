package blue.stack.snowball.app.apps;

import java.util.ArrayList;
import java.util.List;

import blue.stack.snowball.app.R;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.telephony.SmsMessage;
import blue.stack.snowball.app.apps.templates.InputFormatBuilder;
import blue.stack.snowball.app.apps.templates.MessageProcessor;
import blue.stack.snowball.app.apps.templates.MessageProcessor.Result;
import blue.stack.snowball.app.apps.templates.MessageProcessorBuilder;
import blue.stack.snowball.app.apps.templates.OutputFormatBuilder;
import blue.stack.snowball.app.apps.templates.TemplateStringBuilder;
import blue.stack.snowball.app.core.GuiceModule;
import blue.stack.snowball.app.inbox.RawMessage;
import blue.stack.snowball.app.notifications.Notification;
import blue.stack.snowball.app.notifications.NotificationProcessor;
import blue.stack.snowball.app.notifications.NotificationProcessorResult;

public class HangoutsApp extends BaseApp implements NotificationProcessor {
	public static final String PACKAGE_NAME = "com.google.android.talk";
	public static final String RES_DEF_PACKAGE_NAME = "com.google.android.apps.hangouts";
	SmsMessage lastSmsSeen;
	List<MessageProcessor> messageProcessors;
	SMSMessageReceiver smsMessageReceiver;

	class SMSMessageReceiver extends BroadcastReceiver {
		SMSMessageReceiver() {
		}

		@Override
		public void onReceive(Context context, Intent intent) {
			if (PACKAGE_NAME.equals(GuiceModule.get().getInstance(AppManager.class).getSMSAppPackageName())) {
				HangoutsApp.this.processIncomingSMS(SmsMessage.createFromPdu((byte[]) ((Object[]) intent.getExtras()
						.get("pdus"))[0]));
			}
		}
	}

	@Override
	public void start(Context context) {
		super.start(context);
		this.smsMessageReceiver = new SMSMessageReceiver();
		IntentFilter filter = new IntentFilter();
		filter.setPriority(999);
		filter.addAction("android.provider.Telephony.SMS_RECEIVED");
		context.registerReceiver(this.smsMessageReceiver, filter);
		this.messageProcessors = new ArrayList();
		if (isAppInstalled()) {
			initMessageProcessors();
		}
	}

	@Override
	public void stop() {
		if (!(this.smsMessageReceiver == null || this.context == null)) {
			this.context.unregisterReceiver(this.smsMessageReceiver);
			this.smsMessageReceiver = null;
		}
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
		RawMessage message = null;
		long timestamp = n.getWhen();
		PendingIntent pendingLaunchIntent = n.getPendingLaunchIntent();
		List actions = n.getActions();
		Bitmap profilePhoto = null;
		if (shouldUseAsProfilePhoto(n.getExpandedLargeIconBig())) {
			profilePhoto = n.getExpandedLargeIconBig();
		} else if (shouldUseAsProfilePhoto(n.getLargeIcon())) {
			profilePhoto = n.getLargeIcon();
		}
		String smsSenderId = getSMSSenderId(from, messageBody);
		if (smsSenderId != null) {
			message = new RawMessage(SMSApp.APP_ID, smsSenderId, from, messageBody, timestamp, profilePhoto, null, null);
			this.lastSmsSeen = null;
		} else {
			RawMessage rawMessage = new RawMessage(n.getPackageName(), null, from, messageBody, timestamp,
					profilePhoto, pendingLaunchIntent, actions);
		}
		return new NotificationProcessorResult(message, true);
	}

	void initMessageProcessors() {
		addMessageProcessor(new MessageProcessorBuilder()
				.setInputFormat(
						new InputFormatBuilder()
								.setCanonicalInputTemplate(
										new TemplateStringBuilder()
												.addString("%1$s")
												.addString(PACKAGE_NAME, "notification_separator", RES_DEF_PACKAGE_NAME)
												.addString(PACKAGE_NAME, "notification_picture", RES_DEF_PACKAGE_NAME)
												.build(this.context)).labelToken(1, "sender").build(this.context))
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
												.addString(PACKAGE_NAME, "realtimechat_message_text_author",
														RES_DEF_PACKAGE_NAME)
												.addString(" ")
												.addString(PACKAGE_NAME, "realtimechat_message_image",
														RES_DEF_PACKAGE_NAME).build(this.context))
								.addInputToken("%s", "sender").build(this.context))
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
								.setCanonicalInputTemplate(
										new TemplateStringBuilder()
												.addString("%1$s")
												.addString(PACKAGE_NAME, "notification_separator", RES_DEF_PACKAGE_NAME)
												.addString(PACKAGE_NAME, "notification_audio", RES_DEF_PACKAGE_NAME)
												.build(this.context)).labelToken(1, "sender").build(this.context))
				.addOutputFormat(
						"from",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_from).build(
								this.context))
				.addOutputFormat(
						"message",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_message_audio).build(
								this.context)).build(this.context));
		addMessageProcessor(new MessageProcessorBuilder()
				.setInputFormat(
						new InputFormatBuilder()
								.setInputTemplate(
										new TemplateStringBuilder()
												.addString(PACKAGE_NAME, "realtimechat_message_text_author",
														RES_DEF_PACKAGE_NAME)
												.addString(" ")
												.addString(PACKAGE_NAME, "realtimechat_message_audio",
														RES_DEF_PACKAGE_NAME).build(this.context))
								.addInputToken("%s", "sender").build(this.context))
				.addOutputFormat(
						"from",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_from).build(
								this.context))
				.addOutputFormat(
						"message",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_message_audio).build(
								this.context)).build(this.context));
		addMessageProcessor(new MessageProcessorBuilder()
				.setInputFormat(
						new InputFormatBuilder()
								.setCanonicalInputTemplate(
										new TemplateStringBuilder()
												.addString("%1$s")
												.addString(PACKAGE_NAME, "notification_separator", RES_DEF_PACKAGE_NAME)
												.addString(PACKAGE_NAME, "notification_video", RES_DEF_PACKAGE_NAME)
												.build(this.context)).labelToken(1, "sender").build(this.context))
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
								.setInputTemplate(
										new TemplateStringBuilder()
												.addString(PACKAGE_NAME, "realtimechat_message_text_author",
														RES_DEF_PACKAGE_NAME)
												.addString(" ")
												.addString(PACKAGE_NAME, "realtimechat_message_video",
														RES_DEF_PACKAGE_NAME).build(this.context))
								.addInputToken("%s", "sender").build(this.context))
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
								.setCanonicalInputTemplate(
										new TemplateStringBuilder()
												.addString("%1$s")
												.addString(PACKAGE_NAME, "notification_separator", RES_DEF_PACKAGE_NAME)
												.addString(PACKAGE_NAME, "notification_location", RES_DEF_PACKAGE_NAME)
												.build(this.context)).labelToken(1, "sender").build(this.context))
				.addOutputFormat(
						"from",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_from).build(
								this.context))
				.addOutputFormat(
						"message",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_message_map).build(
								this.context)).build(this.context));
		addMessageProcessor(new MessageProcessorBuilder()
				.setInputFormat(
						new InputFormatBuilder()
								.setInputTemplate(
										new TemplateStringBuilder()
												.addString(PACKAGE_NAME, "realtimechat_message_text_author",
														RES_DEF_PACKAGE_NAME)
												.addString(" ")
												.addString(PACKAGE_NAME, "realtimechat_message_location",
														RES_DEF_PACKAGE_NAME).build(this.context))
								.addInputToken("%s", "sender").build(this.context))
				.addOutputFormat(
						"from",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_from).build(
								this.context))
				.addOutputFormat(
						"message",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_message_map).build(
								this.context)).build(this.context));
		addMessageProcessor(new MessageProcessorBuilder()
				.setInputFormat(
						new InputFormatBuilder()
								.setCanonicalInputTemplate(
										new TemplateStringBuilder()
												.addString("%1$s")
												.addString(PACKAGE_NAME, "notification_separator", RES_DEF_PACKAGE_NAME)
												.addString(PACKAGE_NAME, "notification_vcard", RES_DEF_PACKAGE_NAME)
												.build(this.context)).labelToken(1, "sender").build(this.context))
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
												.addString(PACKAGE_NAME, "realtimechat_message_text_author",
														RES_DEF_PACKAGE_NAME)
												.addString(" ")
												.addString(PACKAGE_NAME, "realtimechat_message_vcard",
														RES_DEF_PACKAGE_NAME).build(this.context))
								.addInputToken("%s", "sender").build(this.context))
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
								.setCanonicalInputTemplate(
										new TemplateStringBuilder()
												.addString("%1$s")
												.addString(PACKAGE_NAME, "notification_ticker_separator",
														RES_DEF_PACKAGE_NAME).addString("%2$s").build(this.context))
								.labelToken(1, "sender").labelToken(2, "message").build(this.context))
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

	String getSMSSenderId(String from, String message) {
		if (this.lastSmsSeen == null) {
			return null;
		}
		String smsAddress = this.lastSmsSeen.getOriginatingAddress();
		String smsFrom = SMSApp.getContactNameFromNumber(this.context, smsAddress);
		String smsMessage = this.lastSmsSeen.getMessageBody();
		return ((from == null || !from.contains(smsAddress)) && ((from == null || !from.contains(smsFrom)) && (smsMessage == null || !smsMessage
				.contains(message)))) ? null : smsAddress;
	}

	void processIncomingSMS(SmsMessage smsMessage) {
		this.lastSmsSeen = smsMessage;
	}
}

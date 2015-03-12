package blue.stack.snowball.app.apps;

import java.util.ArrayList;
import java.util.List;

import android.app.PendingIntent;
import android.content.Context;
import android.graphics.Bitmap;
import blue.stack.snowball.app.R;
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

public class HikeMessengerApp extends BaseApp implements NotificationProcessor {
	public static final String PACKAGE_NAME = "com.bsb.hike";
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
								.setCanonicalInputTemplate(
										new TemplateStringBuilder().addString("%2$s - ")
												.addString(PACKAGE_NAME, "accepted_your_favorite_request_details")
												.build(this.context)).labelToken(1, "sender").labelToken(2, "sender2")
								.build(this.context))
				.addOutputFormat(
						"from",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_from).build(
								this.context))
				.addOutputFormat(
						"message",
						new OutputFormatBuilder().setOutputTemplateFromResouce(
								R.string.hike_entry_of_message_favorite_request_details).build(this.context))
				.build(this.context));
		addMessageProcessor(new MessageProcessorBuilder()
				.setInputFormat(
						new InputFormatBuilder()
								.setCanonicalInputTemplateFromPackage(PACKAGE_NAME,
										"accepted_your_favorite_request_details").labelToken(1, "sender")
								.build(this.context))
				.addOutputFormat(
						"from",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_from).build(
								this.context))
				.addOutputFormat(
						"message",
						new OutputFormatBuilder().setOutputTemplateFromResouce(
								R.string.hike_entry_of_message_favorite_request_details).build(this.context))
				.build(this.context));
		addMessageProcessor(new MessageProcessorBuilder()
				.setInputFormat(
						new InputFormatBuilder()
								.setCanonicalInputTemplateFromPackage(PACKAGE_NAME, "add_as_friend_notification")
								.labelToken(1, "sender").build(this.context))
				.addOutputFormat(
						"from",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_from).build(
								this.context))
				.addOutputFormat(
						"message",
						new OutputFormatBuilder().setOutputTemplateFromResouce(
								R.string.hike_entry_of_message_friend_request).build(this.context)).build(this.context));
		addMessageProcessor(new MessageProcessorBuilder()
				.setInputFormat(
						new InputFormatBuilder()
								.setCanonicalInputTemplateFromPackage(PACKAGE_NAME,
										"friend_request_accepted_notification").labelToken(1, "sender")
								.build(this.context))
				.addOutputFormat(
						"from",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_from).build(
								this.context))
				.addOutputFormat(
						"message",
						new OutputFormatBuilder().setOutputTemplateFromResouce(
								R.string.hike_entry_of_message_friend_request_accepted).build(this.context))
				.build(this.context));
		addMessageProcessor(new MessageProcessorBuilder()
				.setInputFormat(
						new InputFormatBuilder()
								.setCanonicalInputTemplate(
										new TemplateStringBuilder().addString("%2$s ")
												.addString(PACKAGE_NAME, "status_text_notification")
												.build(this.context)).labelToken(1, "update").labelToken(2, "sender")
								.build(this.context))
				.addOutputFormat(
						"from",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_from).build(
								this.context))
				.addOutputFormat(
						"message",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.hike_entry_of_message_update)
								.build(this.context)).build(this.context));
		addMessageProcessor(new MessageProcessorBuilder()
				.setInputFormat(
						new InputFormatBuilder()
								.setCanonicalInputTemplateFromPackage(PACKAGE_NAME, "user_back_on_hike")
								.labelToken(1, "sender").build(this.context))
				.addOutputFormat(
						"from",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_from).build(
								this.context))
				.addOutputFormat(
						"message",
						new OutputFormatBuilder().setOutputTemplateFromResouce(
								R.string.hike_entry_of_message_joined_hike).build(this.context)).build(this.context));
		addMessageProcessor(new MessageProcessorBuilder()
				.setInputFormat(
						new InputFormatBuilder().setCanonicalInputTemplateFromPackage(PACKAGE_NAME, "joined_hike_new")
								.labelToken(1, "sender").build(this.context))
				.addOutputFormat(
						"from",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_from).build(
								this.context))
				.addOutputFormat(
						"message",
						new OutputFormatBuilder().setOutputTemplateFromResouce(
								R.string.hike_entry_of_message_joined_hike).build(this.context)).build(this.context));
		addMessageProcessor(new MessageProcessorBuilder()
				.setInputFormat(
						new InputFormatBuilder().setCanonicalInputTemplateFromPackage(PACKAGE_NAME, "joined_hike")
								.labelToken(1, "sender").build(this.context))
				.addOutputFormat(
						"from",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_from).build(
								this.context))
				.addOutputFormat(
						"message",
						new OutputFormatBuilder().setOutputTemplateFromResouce(
								R.string.hike_entry_of_message_joined_hike).build(this.context)).build(this.context));
		addMessageProcessor(new MessageProcessorBuilder()
				.setInputFormat(
						new InputFormatBuilder()
								.setCanonicalInputTemplateFromPackage(PACKAGE_NAME, "add_as_favorite_notification")
								.labelToken(1, "sender").build(this.context))
				.addOutputFormat(
						"from",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_from).build(
								this.context))
				.addOutputFormat(
						"message",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_message_favorite)
								.build(this.context)).build(this.context));
		addMessageProcessor(new MessageProcessorBuilder()
				.setInputFormat(
						new InputFormatBuilder()
								.setCanonicalInputTemplate(
										new TemplateStringBuilder().addString("%1$s - %2$s - ")
												.addString(PACKAGE_NAME, "file_msg_received").build(this.context))
								.labelToken(1, "group").labelToken(2, "sender").build(this.context))
				.addOutputFormat(
						"from",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_from_group).build(
								this.context))
				.addOutputFormat(
						"message",
						new OutputFormatBuilder()
								.setOutputTemplateFromResouce(R.string.entry_of_group_message_document).build(
										this.context)).build(this.context));
		addMessageProcessor(new MessageProcessorBuilder()
				.setInputFormat(
						new InputFormatBuilder()
								.setCanonicalInputTemplate(
										new TemplateStringBuilder().addString("%1$s - ")
												.addString(PACKAGE_NAME, "file_msg_received").build(this.context))
								.labelToken(1, "sender").build(this.context))
				.addOutputFormat(
						"from",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_from).build(
								this.context))
				.addOutputFormat(
						"message",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_message_document)
								.build(this.context)).build(this.context));
		addMessageProcessor(new MessageProcessorBuilder()
				.setInputFormat(
						new InputFormatBuilder()
								.setCanonicalInputTemplate(
										new TemplateStringBuilder().addString("%1$s - %2$s - ")
												.addString(PACKAGE_NAME, "sent_sticker").build(this.context))
								.labelToken(1, "group").labelToken(2, "sender").build(this.context))
				.addOutputFormat(
						"from",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_from_group).build(
								this.context))
				.addOutputFormat(
						"message",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_group_message_sticker)
								.build(this.context)).build(this.context));
		addMessageProcessor(new MessageProcessorBuilder()
				.setInputFormat(
						new InputFormatBuilder()
								.setCanonicalInputTemplate(
										new TemplateStringBuilder().addString("%1$s - ")
												.addString(PACKAGE_NAME, "sent_sticker").build(this.context))
								.labelToken(1, "sender").build(this.context))
				.addOutputFormat(
						"from",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_from).build(
								this.context))
				.addOutputFormat(
						"message",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_message_sticker)
								.build(this.context)).build(this.context));
		addMessageProcessor(new MessageProcessorBuilder()
				.setInputFormat(
						new InputFormatBuilder()
								.setCanonicalInputTemplate(
										new TemplateStringBuilder().addString("%1$s - %2$s - ")
												.addString(PACKAGE_NAME, "audio_recording_msg_received")
												.build(this.context)).labelToken(1, "group").labelToken(2, "sender")
								.build(this.context))
				.addOutputFormat(
						"from",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_from_group).build(
								this.context))
				.addOutputFormat(
						"message",
						new OutputFormatBuilder().setOutputTemplateFromResouce(
								R.string.entry_of_group_message_voice_note).build(this.context)).build(this.context));
		addMessageProcessor(new MessageProcessorBuilder()
				.setInputFormat(
						new InputFormatBuilder()
								.setCanonicalInputTemplate(
										new TemplateStringBuilder().addString("%1$s - ")
												.addString(PACKAGE_NAME, "audio_recording_msg_received")
												.build(this.context)).labelToken(1, "sender").build(this.context))
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
								.setCanonicalInputTemplate(
										new TemplateStringBuilder().addString("%1$s - %2$s - ")
												.addString(PACKAGE_NAME, "contact_msg_received").build(this.context))
								.labelToken(1, "group").labelToken(2, "sender").build(this.context))
				.addOutputFormat(
						"from",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_from_group).build(
								this.context))
				.addOutputFormat(
						"message",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_group_message_contact)
								.build(this.context)).build(this.context));
		addMessageProcessor(new MessageProcessorBuilder()
				.setInputFormat(
						new InputFormatBuilder()
								.setCanonicalInputTemplate(
										new TemplateStringBuilder().addString("%1$s - ")
												.addString(PACKAGE_NAME, "contact_msg_received").build(this.context))
								.labelToken(1, "sender").build(this.context))
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
										new TemplateStringBuilder().addString("%1$s - %2$s - ")
												.addString(PACKAGE_NAME, "location_msg_received").build(this.context))
								.labelToken(1, "group").labelToken(2, "sender").build(this.context))
				.addOutputFormat(
						"from",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_from_group).build(
								this.context))
				.addOutputFormat(
						"message",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_group_message_map)
								.build(this.context)).build(this.context));
		addMessageProcessor(new MessageProcessorBuilder()
				.setInputFormat(
						new InputFormatBuilder()
								.setCanonicalInputTemplate(
										new TemplateStringBuilder().addString("%1$s - ")
												.addString(PACKAGE_NAME, "location_msg_received").build(this.context))
								.labelToken(1, "sender").build(this.context))
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
										new TemplateStringBuilder().addString("%1$s - %2$s - ")
												.addString(PACKAGE_NAME, "audio_msg_received").build(this.context))
								.labelToken(1, "group").labelToken(2, "sender").build(this.context))
				.addOutputFormat(
						"from",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_from_group).build(
								this.context))
				.addOutputFormat(
						"message",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_group_message_audio)
								.build(this.context)).build(this.context));
		addMessageProcessor(new MessageProcessorBuilder()
				.setInputFormat(
						new InputFormatBuilder()
								.setCanonicalInputTemplate(
										new TemplateStringBuilder().addString("%1$s - ")
												.addString(PACKAGE_NAME, "audio_msg_received").build(this.context))
								.labelToken(1, "sender").build(this.context))
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
										new TemplateStringBuilder().addString("%1$s - %2$s - ")
												.addString(PACKAGE_NAME, "video_msg_received").build(this.context))
								.labelToken(1, "group").labelToken(2, "sender").build(this.context))
				.addOutputFormat(
						"from",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_from_group).build(
								this.context))
				.addOutputFormat(
						"message",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_group_message_video)
								.build(this.context)).build(this.context));
		addMessageProcessor(new MessageProcessorBuilder()
				.setInputFormat(
						new InputFormatBuilder()
								.setCanonicalInputTemplate(
										new TemplateStringBuilder().addString("%1$s - ")
												.addString(PACKAGE_NAME, "video_msg_received").build(this.context))
								.labelToken(1, "sender").build(this.context))
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
										new TemplateStringBuilder().addString("%1$s - %2$s - ")
												.addString(PACKAGE_NAME, "image_msg_received").build(this.context))
								.labelToken(1, "group").labelToken(2, "sender").build(this.context))
				.addOutputFormat(
						"from",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_from_group).build(
								this.context))
				.addOutputFormat(
						"message",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_group_message_photo)
								.build(this.context)).build(this.context));
		addMessageProcessor(new MessageProcessorBuilder()
				.setInputFormat(
						new InputFormatBuilder()
								.setCanonicalInputTemplate(
										new TemplateStringBuilder().addString("%1$s - ")
												.addString(PACKAGE_NAME, "image_msg_received").build(this.context))
								.labelToken(1, "sender").build(this.context))
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
						new InputFormatBuilder().setCanonicalInputTemplate("%1$s - %2$s - %3$s").labelToken(1, "group")
								.labelToken(2, "sender").labelToken(3, "message").build(this.context))
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
						new InputFormatBuilder().setCanonicalInputTemplate("%1$s - %2$s").labelToken(1, "sender")
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

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

public class TelegramApp extends BaseApp implements NotificationProcessor {
	public static final String PACKAGE_NAME = "org.telegram.messenger";
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

	@Override
	public boolean shouldRemoveDuplicates() {
		return true;
	}

	void initMessageProcessors() {
		addMessageProcessor(new MessageProcessorBuilder()
				.setInputFormat(
						new InputFormatBuilder()
								.setCanonicalInputTemplateFromPackage(PACKAGE_NAME, "NotificationEditedGroupName")
								.labelToken(1, "sender").labelToken(2, "group").build(this.context))
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
						new InputFormatBuilder()
								.setCanonicalInputTemplateFromPackage(PACKAGE_NAME, "NotificationEditedGroupPhoto")
								.labelToken(1, "sender").labelToken(2, "group").build(this.context))
				.addOutputFormat(
						"from",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_from_group).build(
								this.context))
				.addOutputFormat(
						"message",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_group_edit_photo)
								.build(this.context)).build(this.context));
		addMessageProcessor(new MessageProcessorBuilder()
				.setInputFormat(
						new InputFormatBuilder()
								.setCanonicalInputTemplateFromPackage(PACKAGE_NAME, "NotificationGroupAddMember")
								.labelToken(1, "sender").labelToken(2, "group").labelToken(3, "member")
								.build(this.context))
				.addOutputFormat(
						"from",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_from_group).build(
								this.context))
				.addOutputFormat(
						"message",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_group_add_member)
								.build(this.context)).build(this.context));
		addMessageProcessor(new MessageProcessorBuilder()
				.setInputFormat(
						new InputFormatBuilder()
								.setCanonicalInputTemplateFromPackage(PACKAGE_NAME, "NotificationGroupKickMember")
								.labelToken(1, "sender").labelToken(2, "group").labelToken(3, "member")
								.build(this.context))
				.addOutputFormat(
						"from",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_from_group).build(
								this.context))
				.addOutputFormat(
						"message",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_group_kick_member)
								.build(this.context)).build(this.context));
		addMessageProcessor(new MessageProcessorBuilder()
				.setInputFormat(
						new InputFormatBuilder()
								.setCanonicalInputTemplateFromPackage(PACKAGE_NAME, "NotificationGroupKickYou")
								.labelToken(1, "sender").labelToken(2, "group").build(this.context))
				.addOutputFormat(
						"from",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_from_group).build(
								this.context))
				.addOutputFormat(
						"message",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_group_kick_you).build(
								this.context)).build(this.context));
		addMessageProcessor(new MessageProcessorBuilder()
				.setInputFormat(
						new InputFormatBuilder()
								.setCanonicalInputTemplateFromPackage(PACKAGE_NAME, "NotificationGroupLeftMember")
								.labelToken(1, "sender").labelToken(2, "group").build(this.context))
				.addOutputFormat(
						"from",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_from_group).build(
								this.context))
				.addOutputFormat(
						"message",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_group_member_left)
								.build(this.context)).build(this.context));
		addMessageProcessor(new MessageProcessorBuilder()
				.setInputFormat(
						new InputFormatBuilder()
								.setCanonicalInputTemplateFromPackage(PACKAGE_NAME, "NotificationInvitedToGroup")
								.labelToken(1, "sender").labelToken(2, "group").build(this.context))
				.addOutputFormat(
						"from",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_from_group).build(
								this.context))
				.addOutputFormat(
						"message",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_group_invited_you)
								.build(this.context)).build(this.context));
		addMessageProcessor(new MessageProcessorBuilder()
				.setInputFormat(
						new InputFormatBuilder()
								.setCanonicalInputTemplateFromPackage(PACKAGE_NAME, "NotificationMessageGroupAudio")
								.labelToken(1, "sender").labelToken(2, "group").build(this.context))
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
								.setCanonicalInputTemplateFromPackage(PACKAGE_NAME, "NotificationMessageGroupContact")
								.labelToken(1, "sender").labelToken(2, "group").build(this.context))
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
								.setCanonicalInputTemplateFromPackage(PACKAGE_NAME, "NotificationMessageGroupDocument")
								.labelToken(1, "sender").labelToken(2, "group").build(this.context))
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
								.setCanonicalInputTemplateFromPackage(PACKAGE_NAME, "NotificationMessageGroupMap")
								.labelToken(1, "sender").labelToken(2, "group").build(this.context))
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
								.setCanonicalInputTemplateFromPackage(PACKAGE_NAME, "NotificationMessageGroupNoText")
								.labelToken(1, "sender").labelToken(2, "group").build(this.context))
				.addOutputFormat(
						"from",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_from_group).build(
								this.context))
				.addOutputFormat(
						"message",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_group_message_notext)
								.build(this.context)).build(this.context));
		addMessageProcessor(new MessageProcessorBuilder()
				.setInputFormat(
						new InputFormatBuilder()
								.setCanonicalInputTemplateFromPackage(PACKAGE_NAME, "NotificationMessageGroupPhoto")
								.labelToken(1, "sender").labelToken(2, "group").build(this.context))
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
								.setCanonicalInputTemplateFromPackage(PACKAGE_NAME, "NotificationMessageGroupText")
								.labelToken(1, "sender").labelToken(2, "group").labelToken(3, "message")
								.build(this.context))
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
								.setCanonicalInputTemplateFromPackage(PACKAGE_NAME, "NotificationMessageGroupVideo")
								.labelToken(1, "sender").labelToken(2, "group").build(this.context))
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
								.setCanonicalInputTemplateFromPackage(PACKAGE_NAME, "NotificationMessageAudio")
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
								.setCanonicalInputTemplateFromPackage(PACKAGE_NAME, "NotificationMessageContact")
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
								.setCanonicalInputTemplateFromPackage(PACKAGE_NAME, "NotificationMessageDocument")
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
								.setCanonicalInputTemplateFromPackage(PACKAGE_NAME, "NotificationMessageMap")
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
								.setCanonicalInputTemplateFromPackage(PACKAGE_NAME, "NotificationMessageNoText")
								.labelToken(1, "sender").build(this.context))
				.addOutputFormat(
						"from",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_from).build(
								this.context))
				.addOutputFormat(
						"message",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_message_notext).build(
								this.context)).build(this.context));
		addMessageProcessor(new MessageProcessorBuilder()
				.setInputFormat(
						new InputFormatBuilder()
								.setCanonicalInputTemplateFromPackage(PACKAGE_NAME, "NotificationMessagePhoto")
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
						new InputFormatBuilder()
								.setCanonicalInputTemplateFromPackage(PACKAGE_NAME, "NotificationMessageText")
								.labelToken(1, "sender").labelToken(2, "message").build(this.context))
				.addOutputFormat(
						"from",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_from).build(
								this.context))
				.addOutputFormat(
						"message",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_message_text).build(
								this.context)).build(this.context));
		addMessageProcessor(new MessageProcessorBuilder()
				.setInputFormat(
						new InputFormatBuilder()
								.setCanonicalInputTemplateFromPackage(PACKAGE_NAME, "NotificationMessageVideo")
								.labelToken(1, "sender").build(this.context))
				.addOutputFormat(
						"from",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_from).build(
								this.context))
				.addOutputFormat(
						"message",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_message_video).build(
								this.context)).build(this.context));
	}

	void addMessageProcessor(MessageProcessor t) {
		if (t != null) {
			this.messageProcessors.add(t);
		}
	}
}

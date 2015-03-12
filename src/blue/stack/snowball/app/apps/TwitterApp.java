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
import blue.stack.snowball.app.apps.templates.TemplateStringBuilder;
import blue.stack.snowball.app.inbox.RawMessage;
import blue.stack.snowball.app.notifications.Notification;
import blue.stack.snowball.app.notifications.NotificationProcessor;
import blue.stack.snowball.app.notifications.NotificationProcessorResult;

public class TwitterApp extends BaseApp implements NotificationProcessor {
	public static final String PACKAGE_NAME = "com.twitter.android";
	List<MessageProcessor> messageProcessors;

	@Override
	public String getAppId() {
		return PACKAGE_NAME;
	}

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
	public boolean shouldHandleNotification(Notification n) {
		return n.getPackageName().equals(PACKAGE_NAME);
	}

	@Override
	public NotificationProcessorResult processNotification(Notification n) {
		String tickerText = n.getTickerText();
		String contentTitle = n.getContentTitle();
		String contentText = n.getContentText();
		String expandedInboxTextFirst = null;
		String expandedInboxTextLast = null;
		int smallIcon = n.getSmallIcon();
		List<String> expandedInboxText = n.getExpandedInboxText();
		if (expandedInboxText != null && expandedInboxText.size() > 0) {
			expandedInboxTextFirst = expandedInboxText.get(0);
			expandedInboxTextLast = expandedInboxText.get(expandedInboxText.size() - 1);
		}
		Map<String, String> inputs = new HashMap();
		inputs.put("tickerText", tickerText);
		inputs.put("contentTitle", contentTitle);
		inputs.put("contentText", contentText);
		inputs.put("expandedInboxTextFirst", expandedInboxTextFirst);
		inputs.put("expandedInboxTextLast", expandedInboxTextLast);
		inputs.put("smallIcon", Integer.toString(smallIcon));
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
		addMessageProcessor(new MessageProcessorBuilder()
				.addInputFormat(
						"smallIcon",
						new InputFormatBuilder().setInputTemplate(
								new TemplateStringBuilder().addResourceIdString(PACKAGE_NAME, "ic_stat_dm", "drawable")
										.build(this.context)).build(this.context))
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
		addMessageProcessor(new MessageProcessorBuilder()
				.addInputFormat(
						"smallIcon",
						new InputFormatBuilder().setInputTemplate(
								new TemplateStringBuilder().addResourceIdString(PACKAGE_NAME, "ic_stat_dm", "drawable")
										.build(this.context)).build(this.context))
				.addInputFormat(
						"contentTitle",
						new InputFormatBuilder().setCanonicalInputTemplate("%1$s").labelToken(1, "sender")
								.build(this.context))
				.addInputFormat(
						"expandedInboxTextFirst",
						new InputFormatBuilder()
								.setCanonicalInputTemplateFromPackage(PACKAGE_NAME, "dm_user_conversation_preview")
								.labelToken(1, "sender2").labelToken(2, "message").build(this.context))
				.addOutputFormat(
						"from",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_from).build(
								this.context))
				.addOutputFormat(
						"message",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_message_text).build(
								this.context)).build(this.context));
		addMessageProcessor(new MessageProcessorBuilder()
				.addInputFormat(
						"tickerText",
						new InputFormatBuilder()
								.setCanonicalInputTemplateFromPackage(PACKAGE_NAME, "notif_single_fav_format")
								.labelToken(1, "sender").labelToken(2, "tweet").build(this.context))
				.addOutputFormat(
						"from",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_from).build(
								this.context))
				.addOutputFormat(
						"message",
						new OutputFormatBuilder().setOutputTemplateFromResouce(
								R.string.twitter_entry_of_message_favorited).build(this.context)).build(this.context));
		addMessageProcessor(new MessageProcessorBuilder()
				.addInputFormat(
						"tickerText",
						new InputFormatBuilder()
								.setCanonicalInputTemplateFromPackage(PACKAGE_NAME, "notif_single_follow_format")
								.labelToken(1, "sender").build(this.context))
				.addOutputFormat(
						"from",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_from).build(
								this.context))
				.addOutputFormat(
						"message",
						new OutputFormatBuilder().setOutputTemplateFromResouce(
								R.string.twitter_entry_of_message_followed).build(this.context)).build(this.context));
		addMessageProcessor(new MessageProcessorBuilder()
				.addInputFormat(
						"tickerText",
						new InputFormatBuilder()
								.setCanonicalInputTemplateFromPackage(PACKAGE_NAME,
										"notif_single_follower_request_format").labelToken(1, "sender")
								.build(this.context))
				.addOutputFormat(
						"from",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_from).build(
								this.context))
				.addOutputFormat(
						"message",
						new OutputFormatBuilder().setOutputTemplateFromResouce(
								R.string.twitter_entry_of_message_follow_request).build(this.context))
				.build(this.context));
		addMessageProcessor(new MessageProcessorBuilder()
				.addInputFormat(
						"tickerText",
						new InputFormatBuilder()
								.setCanonicalInputTemplateFromPackage(PACKAGE_NAME, "notif_single_media_tag_format")
								.labelToken(1, "sender").build(this.context))
				.addOutputFormat(
						"from",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_from).build(
								this.context))
				.addOutputFormat(
						"message",
						new OutputFormatBuilder().setOutputTemplateFromResouce(
								R.string.twitter_entry_of_message_photo_tag).build(this.context)).build(this.context));
		addMessageProcessor(new MessageProcessorBuilder()
				.addInputFormat(
						"tickerText",
						new InputFormatBuilder()
								.setCanonicalInputTemplateFromPackage(PACKAGE_NAME, "notif_single_rt_format")
								.labelToken(1, "sender").labelToken(2, "tweet").build(this.context))
				.addOutputFormat(
						"from",
						new OutputFormatBuilder().setOutputTemplateFromResouce(R.string.entry_of_from).build(
								this.context))
				.addOutputFormat(
						"message",
						new OutputFormatBuilder().setOutputTemplateFromResouce(
								R.string.twitter_entry_of_message_retweeted).build(this.context)).build(this.context));
	}

	void addMessageProcessor(MessageProcessor t) {
		if (t != null) {
			this.messageProcessors.add(t);
		}
	}
}

package blue.stack.snowball.app.lockscreen.ui;

import blue.stack.snowball.app.R;
import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import blue.stack.snowball.app.apps.App;
import blue.stack.snowball.app.apps.AppManager;
import blue.stack.snowball.app.core.GuiceModule;
import blue.stack.snowball.app.inbox.InboxManager;
import blue.stack.snowball.app.inbox.Message;
import blue.stack.snowball.app.lockscreen.LockScreenManager;
import blue.stack.snowball.app.photos.ProfilePhoto;
import blue.stack.snowball.app.photos.ProfilePhotoManager;
import blue.stack.snowball.app.photos.ProfilePhotoManager.OnProfilePhotoLoadedListener;
import blue.stack.snowball.app.ui.LockScreenMessageView;

import com.google.inject.Inject;

public class LockScreenWidgetPagerAdapter extends PagerAdapter {
	private static final String TAG = "LockScreenWidgetPagerAdapter";
	@Inject
	AppManager appManager;
	Context context;
	Cursor cursor;
	@Inject
	InboxManager inboxManager;
	@Inject
	LockScreenManager lockScreenManager;
	int maxEntries;
	boolean showMorePage;
	int showSinceMessageId;

	class AnonymousClass_3 implements OnClickListener {
		final/* synthetic */Message val$message;

		AnonymousClass_3(Message message) {
			this.val$message = message;
		}

		@Override
		public void onClick(View view) {
			Log.d(TAG, "Unlocking and launching app");
			LockScreenWidgetPagerAdapter.this.lockScreenManager.unlockAndLaunchApp(this.val$message);
		}
	}

	class AnonymousClass_2 implements OnProfilePhotoLoadedListener {
		final/* synthetic */LockScreenMessageView val$entryView;

		AnonymousClass_2(LockScreenMessageView lockScreenMessageView) {
			this.val$entryView = lockScreenMessageView;
		}

		@Override
		public void onProfilePhotoLoaded(String appId, String senderId, String senderName, ProfilePhoto photo) {
			if (LockScreenWidgetPagerAdapter.this.areStringsEqual(appId, this.val$entryView.getAppId())
					&& LockScreenWidgetPagerAdapter.this.areStringsEqual(senderId, this.val$entryView.getSenderId())
					&& LockScreenWidgetPagerAdapter.this.areStringsEqual(senderName, this.val$entryView.getFrom())) {
				this.val$entryView.setProfilePhoto(LockScreenWidgetPagerAdapter.this.context, photo);
			}
		}
	}

	public LockScreenWidgetPagerAdapter() {
		GuiceModule.get().injectMembers(this);
	}

	public void start(Context context, int showSinceMessageId, int maxEntries) {
		this.context = context;
		this.maxEntries = maxEntries;
		this.showMorePage = false;
		this.cursor = this.inboxManager.getUnreadMessagesViewSince(showSinceMessageId);
		this.showSinceMessageId = showSinceMessageId;
	}

	public void stop() {
	}

	public void setShowSinceMessageId(int showSinceMessageId) {
		this.showSinceMessageId = showSinceMessageId;
		reset();
	}

	public void reset() {
		if (this.cursor != null) {
			this.cursor.close();
		}
		this.cursor = this.inboxManager.getUnreadMessagesViewSince(this.showSinceMessageId);
		notifyDataSetChanged();
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		int count = getCount();
		if (this.showMorePage && position == count - 1) {
			LockScreenMoreMessagesView entryView = new LockScreenMoreMessagesView(this.context);
			entryView.setMoreCount(this.cursor.getCount() - this.maxEntries);
			container.addView(entryView);
			LockScreenMoreMessagesView item = entryView;
			((LinearLayout) entryView.findViewById(R.id.lockscreen_more_messages_body))
					.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View view) {
							Log.d(TAG, "Unlocking and launching inbox");
							LockScreenWidgetPagerAdapter.this.lockScreenManager.unlockAndLaunchInbox();
						}
					});
			return item;
		}
		this.cursor.moveToPosition(position);
		Message message = this.inboxManager.getMessageFromViewCursor(this.cursor);
		LockScreenMessageView entryView2 = new LockScreenMessageView(this.context);
		initMessageView(entryView2, message);
		container.addView(entryView2);
		return entryView2;
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		if (object instanceof LockScreenMessageView) {
			container.removeView((LockScreenMessageView) object);
		} else if (object instanceof LockScreenMoreMessagesView) {
			container.removeView((LockScreenMoreMessagesView) object);
		}
	}

	@Override
	public int getCount() {
		if (this.cursor == null) {
			return 0;
		}
		int count = this.cursor.getCount();
		if (count <= this.maxEntries) {
			this.showMorePage = false;
			return count;
		}
		this.showMorePage = true;
		return this.maxEntries + 1;
	}

	@Override
	public boolean isViewFromObject(View view, Object object) {
		return view == object;
	}

	void initMessageView(LockScreenMessageView entryView, Message message) {
		entryView.setMessageId(message.getId());
		entryView.setAppId(message.getAppId());
		entryView.setSenderId(message.getSender().getAppSpecificUserId());
		entryView.setFrom(message.getSender().getDisplayName());
		entryView.setMessage(message.getBody());
		entryView.setTimestamp(message.getTimestamp());
		App app = this.appManager.getAppById(message.getAppId());
		Drawable icon = app.getAppIcon();
		ProfilePhoto profilePhoto = app.getProfilePhotoForMessage(message);
		entryView.setProfilePhoto(this.context, profilePhoto);
		if (profilePhoto == null) {
			GuiceModule
					.get()
					.getInstance(ProfilePhotoManager.class)
					.loadProfilePhoto(message.getAppId(), message.getSender().getAppSpecificUserId(),
							message.getSender().getDisplayName(), new AnonymousClass_2(entryView));
		}
		entryView.setIcon(icon);
		entryView.findViewById(R.id.container).setOnClickListener(new AnonymousClass_3(message));
	}

	@Override
	public int getItemPosition(Object object) {
		return -2;
	}

	boolean areStringsEqual(String s1, String s2) {
		if (s1 == s2) {
			return true;
		}
		return s1 != null && s1.equals(s2);
	}
}

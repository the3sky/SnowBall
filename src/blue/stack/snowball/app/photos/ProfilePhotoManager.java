package blue.stack.snowball.app.photos;

import java.io.InputStream;
import java.util.concurrent.Executor;

import blue.stack.snowball.app.R;
import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.ContactsContract.Contacts;
import android.text.TextUtils;
import android.util.Log;
import android.util.LruCache;
import blue.stack.snowball.app.apps.SMSApp;
import blue.stack.snowball.app.photos.ProfilePhoto.ImageType;
import blue.stack.snowball.app.tools.CustomThreadPoolExecutor;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class ProfilePhotoManager {
	public static final String CONTACTS_ID_COLUMN = "_id";
	private static final int LRU_CACHE_SIZE = 100;
	private static final String TAG = "ProfilePhotoManager";
	@Inject
	Context context;
	private Executor executor;
	LruCache<ProfilePhotoKey, ProfilePhoto> photoCache;

	class AnonymousClass_1 extends AsyncTask<Void, Void, Uri> {
		final/* synthetic */String val$appId;
		final/* synthetic */ProfilePhotoKey val$key;
		final/* synthetic */OnProfilePhotoLoadedListener val$listener;
		final/* synthetic */String val$senderId;

		AnonymousClass_1(String str, String str2, ProfilePhotoKey profilePhotoKey,
				OnProfilePhotoLoadedListener onProfilePhotoLoadedListener) {
			this.val$senderId = str;
			this.val$appId = str2;
			this.val$key = profilePhotoKey;
			this.val$listener = onProfilePhotoLoadedListener;
		}

		@Override
		protected Uri doInBackground(Void[] objects) {
			if (isCancelled()) {
				return null;
			}
			if (TextUtils.isEmpty(this.val$senderId) || !this.val$appId.equals(SMSApp.APP_ID)
					|| !ProfilePhotoManager.this.isSMSShortCode(this.val$senderId)) {
				return ProfilePhotoManager.this.findContactImage(ProfilePhotoManager.this.context,
						this.val$key.senderName);
			}
			Log.d(TAG, "Found short code!");
			ProfilePhotoManager.this.context.getResources().getDrawable(R.drawable.profile_short_code);
			return Uri.parse("android.resource://blue.stack.snowball.app/drawable/profile_short_code");
		}

		@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
		@Override
		protected void onPostExecute(Uri photoUri) {
			if (!isCancelled() && photoUri != null) {
				ProfilePhoto photo = new ProfilePhoto();
				photo.setImageUri(photoUri);
				photo.setPreferredImageType(ImageType.URI);
				this.val$listener.onProfilePhotoLoaded(this.val$key.appId, this.val$key.senderId,
						this.val$key.senderName, photo);
				ProfilePhotoManager.this.photoCache.put(this.val$key, photo);
			}
		}
	}

	public static interface OnProfilePhotoLoadedListener {
		void onProfilePhotoLoaded(String str, String str2, String str3, ProfilePhoto profilePhoto);
	}

	class ProfilePhotoKey {
		String appId;
		String senderId;
		String senderName;

		ProfilePhotoKey(String appId, String senderId, String senderName) {
			this.appId = appId;
			this.senderId = senderId;
			this.senderName = senderName;
		}

		@Override
		public int hashCode() {
			int hashCode = 0;
			if (this.appId != null) {
				hashCode = 0 + this.appId.hashCode();
			}
			if (this.senderId != null) {
				hashCode += this.senderId.hashCode();
			}
			return this.senderName != null ? hashCode + this.senderName.hashCode() : hashCode;
		}

		@Override
		public boolean equals(Object o) {
			return o instanceof ProfilePhotoKey ? equals((ProfilePhotoKey) o) : false;
		}

		public boolean equals(ProfilePhotoKey compareToEntry) {
			return compareToEntry != null && areStringsEqual(this.appId, compareToEntry.appId)
					&& areStringsEqual(this.senderId, compareToEntry.senderId)
					&& areStringsEqual(this.senderName, compareToEntry.senderName);
		}

		boolean areStringsEqual(String s1, String s2) {
			if (s1 == s2) {
				return true;
			}
			return s1 != null && s1.equals(s2);
		}
	}

	@Inject
	private ProfilePhotoManager() {
		this.executor = new CustomThreadPoolExecutor();
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
	@Inject
	private void start() {
		this.photoCache = new LruCache<ProfilePhotoKey, ProfilePhoto>(LRU_CACHE_SIZE);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
	public void stop() {
		if (this.photoCache != null) {
			this.photoCache.evictAll();
		}
	}

	public void cacheProfilePhoto(String appId, String senderId, String senderName, Bitmap bitmapPhoto) {
		ProfilePhoto photo = new ProfilePhoto();
		photo.setBitmap(bitmapPhoto);
		photo.setPreferredImageType(ImageType.BITMAP);
		cacheProfilePhoto(appId, senderId, senderName, photo);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
	public void cacheProfilePhoto(String appId, String senderId, String senderName, ProfilePhoto photo) {
		this.photoCache.put(new ProfilePhotoKey(appId, senderId, senderName), photo);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
	public ProfilePhoto getProfilePhoto(String appId, String senderId, String senderName) {
		return this.photoCache.get(new ProfilePhotoKey(appId, senderId, senderName));
	}

	private boolean isSMSShortCode(String originatingNumber) {
		return !TextUtils.isEmpty(originatingNumber) && originatingNumber.length() <= 6;
	}

	public void loadProfilePhoto(String appId, String senderId, String senderName, OnProfilePhotoLoadedListener listener) {
		new AnonymousClass_1(senderId, appId, new ProfilePhotoKey(appId, senderId, senderName), listener)
				.executeOnExecutor(this.executor, new Void[0]);
	}

	Uri findContactImage(Context context, String displayNameToFind) {
		Cursor cursor = null;
		try {
			String selection = "UPPER(display_name) = UPPER(?)";
			String[] selectionParams = new String[1];
			String[] columns = new String[] { CONTACTS_ID_COLUMN };
			selectionParams[0] = displayNameToFind;
			cursor = context.getContentResolver().query(Contacts.CONTENT_URI, columns,
					"UPPER(display_name) = UPPER(?)", selectionParams, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
		Uri imageUri = null;
		if (cursor != null && cursor.getCount() > 0) {
			while (cursor.moveToNext()) {
				long id = cursor.getLong(cursor.getColumnIndex(CONTACTS_ID_COLUMN));
				try {
					InputStream inputStream = Contacts.openContactPhotoInputStream(context.getContentResolver(),
							ContentUris.withAppendedId(Contacts.CONTENT_URI, id));
					if (inputStream != null) {
						imageUri = Uri.withAppendedPath(Contacts.CONTENT_URI, String.valueOf(id));
						break;
					} else if (inputStream != null) {
						inputStream.close();
					}
				} catch (Exception e2) {
					e2.printStackTrace();
				}
			}
		}
		if (cursor != null) {
			cursor.close();
		}
		return imageUri != null ? imageUri : null;
	}
}

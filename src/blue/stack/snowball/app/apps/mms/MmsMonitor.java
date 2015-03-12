package blue.stack.snowball.app.apps.mms;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import blue.stack.snowball.app.R;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.Telephony.Mms.Inbox;
import android.provider.Telephony.MmsSms;
import android.telephony.TelephonyManager;
import android.util.Log;
import blue.stack.snowball.app.photos.ProfilePhotoManager;

public class MmsMonitor {
    private static final String ACTION_MMS_DOWNLOADED = "android.intent.action.TRANSACTION_COMPLETED_ACTION";
    private static final int ADDR_TYPE_FROM = 137;
    private static final int ADDR_TYPE_TO = 151;
    private static final String[] CONTENT_TYPE_AUDIO;
    private static final String[] CONTENT_TYPE_CALENDARS;
    private static final String[] CONTENT_TYPE_CONTACTS;
    private static final String CONTENT_TYPE_HTML = "text/html";
    private static final String[] CONTENT_TYPE_IMAGES;
    private static final String CONTENT_TYPE_TEXT = "text/plain";
    private static final String[] CONTENT_TYPE_VIDEOS;
    private static final long MAX_WAIT_TIME_FOR_MMS = 30000;
    private static final String MMS_SORT_ORDER = "_id desc";
    private static final String TAG = "MmsMonitor";
    Context context;
    long lastMessageId;
    List<MmsListener> listeners;
    MmsContentObserver mmsContentObserver;
    MmsDownloadedReceiver mmsDownloadedReceiver;
    List<PendingMms> pendingMessages;

    class MmsContentObserver extends ContentObserver {
        public MmsContentObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
            MmsMonitor.this.onConversationDatabaseChanged();
        }
    }

    class MmsDownloadedReceiver extends BroadcastReceiver {
        MmsDownloadedReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ACTION_MMS_DOWNLOADED)) {
                MmsMonitor.this.onConversationDatabaseChanged();
            }
        }
    }

    public static interface MmsListener {
        void onMmsReceived(MmsMessage mmsMessage);
    }

    public class MmsMessage {
        String message;
        List<String> recipientAddresses;
        String senderAddress;

        public MmsMessage(String senderAddress, List<String> recipientAddresses, String message) {
            this.senderAddress = senderAddress;
            this.recipientAddresses = recipientAddresses;
            this.message = message;
        }

        public String getSenderAddress() {
            return this.senderAddress;
        }

        public List<String> getRecipientAddresses() {
            return this.recipientAddresses;
        }

        public String getMessage() {
            return this.message;
        }
    }

    class PendingMms {
        long messageId;
        long timestamp;

        PendingMms(long messageId, long timestamp) {
            this.messageId = messageId;
            this.timestamp = timestamp;
        }
    }

    static {
        CONTENT_TYPE_IMAGES = new String[]{"image/*", "image/jpeg", "image/jpg", "image/gif", "image/vnd.wap.wbmp", "image/png", "image/x-ms-bmp"};
        CONTENT_TYPE_VIDEOS = new String[]{"video/*", "video/3gpp", "video/3gpp2", "video/h263", "video/mp4"};
        CONTENT_TYPE_CONTACTS = new String[]{"text/x-vcard"};
        CONTENT_TYPE_CALENDARS = new String[]{"text/x-vCalendar"};
        CONTENT_TYPE_AUDIO = new String[]{"audio/*", "audio/aac", "audio/amr", "audio/imelody", "audio/mid", "audio/midi", "audio/mp3", "audio/mpeg3", "audio/mpeg", "audio/mpg", "audio/mp4", "audio/x-mid", "audio/x-midi", "audio/x-mp3", "audio/x-mpeg3", "audio/x-mpeg", "audio/x-mpg", "audio/3gpp", "audio/x-wav", "application/ogg"};
    }

    @TargetApi(19)
    public void start(Context context) {
        this.context = context;
        this.listeners = new ArrayList();
        this.pendingMessages = new ArrayList();
        this.mmsDownloadedReceiver = null;
        this.mmsContentObserver = null;
        this.mmsContentObserver = new MmsContentObserver(new Handler(Looper.getMainLooper()));
        context.getContentResolver().registerContentObserver(MmsSms.CONTENT_URI, true, this.mmsContentObserver);
        this.lastMessageId = -1;
        loadLastMessageId();
    }

    public void stop() {
        if (this.mmsContentObserver != null) {
            this.context.getContentResolver().unregisterContentObserver(this.mmsContentObserver);
            this.mmsContentObserver = null;
        }
        if (this.mmsDownloadedReceiver != null) {
            this.context.unregisterReceiver(this.mmsDownloadedReceiver);
            this.mmsDownloadedReceiver = null;
        }
        this.listeners = null;
        this.context = null;
    }

    public void addMMSListener(MmsListener listener) {
        this.listeners.add(listener);
    }

    public void removeMMSListener(MmsListener listener) {
        this.listeners.remove(listener);
    }

    @TargetApi(19)
    void loadLastMessageId() {
        Cursor cursor = this.context.getContentResolver().query(Inbox.CONTENT_URI, null, null, null, MMS_SORT_ORDER);
        if (cursor.moveToFirst()) {
            this.lastMessageId = cursor.getLong(cursor.getColumnIndex(ProfilePhotoManager.CONTACTS_ID_COLUMN));
        }
        cursor.close();
    }

    @TargetApi(19)
    void onConversationDatabaseChanged() {
        Cursor cursor = this.context.getContentResolver().query(Inbox.CONTENT_URI, null, null, null, MMS_SORT_ORDER);
        long messageId = -1;
        if (cursor.moveToFirst()) {
            messageId = cursor.getLong(cursor.getColumnIndex(ProfilePhotoManager.CONTACTS_ID_COLUMN));
        }
        cursor.close();
        if (messageId != -1 && messageId > this.lastMessageId) {
            Log.d(TAG, "Adding messageId = " + messageId + " to the pending list");
            long pendingId = messageId;
            if (!this.pendingMessages.contains(Long.valueOf(pendingId))) {
                addPendingMessage(pendingId);
            }
        }
        this.lastMessageId = messageId;
        flushPendingMessages();
    }

    void addPendingMessage(long messageId) {
        for (PendingMms pendingMessage : this.pendingMessages) {
            if (pendingMessage.messageId == messageId) {
                return;
            }
        }
        this.pendingMessages.add(new PendingMms(messageId, System.currentTimeMillis()));
    }

    void flushPendingMessages() {
        Iterator<PendingMms> iter = this.pendingMessages.iterator();
        List<MmsMessage> mmsMessages = new ArrayList();
        while (iter.hasNext()) {
            PendingMms pendingMms = (PendingMms) iter.next();
            MmsMessage mmsMessage = getMMS(pendingMms.messageId);
            if (mmsMessage != null) {
                iter.remove();
                mmsMessages.add(mmsMessage);
            } else if (System.currentTimeMillis() - pendingMms.timestamp > MAX_WAIT_TIME_FOR_MMS) {
                iter.remove();
            }
        }
        for (MmsMessage mmsMessage2 : mmsMessages) {
            fireOnMmsReceived(mmsMessage2);
        }
    }

    void fireOnMmsReceived(MmsMessage mmsMessage) {
        for (MmsListener listener : this.listeners) {
            listener.onMmsReceived(mmsMessage);
        }
    }

    MmsMessage getMMS(long messageId) {
        String message = null;
        String senderAddress = null;
        List<String> recipientAddresses = new ArrayList();
        Uri uri = Uri.parse("content://mms/" + messageId + "/addr");
        Cursor cursor = this.context.getContentResolver().query(uri, null, new String("msg_id=" + messageId + " AND type=" + ADDR_TYPE_FROM), null, null);
        Log.d(TAG, "Found " + cursor.getCount() + " sender address for mms Id = " + messageId);
        while (cursor.moveToNext()) {
            senderAddress = cursor.getString(cursor.getColumnIndex("address"));
            Log.d(TAG, "Found sender address = " + senderAddress + " for mms Id = " + messageId);
        }
        cursor.close();
        if (senderAddress == null) {
            return null;
        }
        cursor = this.context.getContentResolver().query(uri, null, new String("msg_id=" + messageId + " AND type=" + ADDR_TYPE_TO), null, null);
        Log.d(TAG, "Found " + cursor.getCount() + " recipient addresses for mms Id = " + messageId);
        while (cursor.moveToNext()) {
            String address = cursor.getString(cursor.getColumnIndex("address"));
            recipientAddresses.add(address);
            Log.d(TAG, "Found recipient address = " + address + " for mms Id = " + messageId);
        }
        cursor.close();
        String myAddress = ((TelephonyManager) this.context.getSystemService("phone")).getLine1Number();
        Log.d(TAG, "Removing my address from recipients" + myAddress);
        removeAddress(myAddress, recipientAddresses);
        cursor = this.context.getContentResolver().query(Uri.parse("content://mms/part"), null, new String("mid=" + messageId), null, null);
        Log.d(TAG, "Found " + cursor.getCount() + " parts for mms Id = " + messageId);
        while (cursor.moveToNext()) {
            String contentType = cursor.getString(cursor.getColumnIndex("ct"));
            message = getMessageForContentType(contentType);
            if (message != null) {
                break;
            } else if (CONTENT_TYPE_TEXT.equals(contentType)) {
                message = cursor.getString(cursor.getColumnIndex("text"));
                break;
            } else {
                Log.d(TAG, "Skipping part with contentType = " + contentType);
            }
        }
        cursor.close();
        if (message == null) {
            return null;
        }
        Log.d(TAG, "Found MMS addess = " + senderAddress + "   |   message = " + message + "   |   id = " + messageId);
        return new MmsMessage(senderAddress, recipientAddresses, message);
    }

    void removeAddress(String addressToRemove, List<String> addresses) {
        if (addressToRemove != null) {
            Iterator<String> i = addresses.iterator();
            while (i.hasNext()) {
                String address = (String) i.next();
                if (address.contains(addressToRemove) || addressToRemove.contains(address)) {
                    i.remove();
                }
            }
        }
    }

    String getMessageForContentType(String contentType) {
        for (String contentTypeImage : CONTENT_TYPE_IMAGES) {
            if (contentTypeImage.equals(contentType)) {
                return this.context.getResources().getString(R.string.entry_of_message_photo);
            }
        }
        for (String contentTypeImage2 : CONTENT_TYPE_VIDEOS) {
            if (contentTypeImage2.equals(contentType)) {
                return this.context.getResources().getString(R.string.entry_of_message_multimedia);
            }
        }
        for (String contentTypeImage22 : CONTENT_TYPE_AUDIO) {
            if (contentTypeImage22.equals(contentType)) {
                return this.context.getResources().getString(R.string.entry_of_message_audio);
            }
        }
        for (String contentTypeImage222 : CONTENT_TYPE_CONTACTS) {
            if (contentTypeImage222.equals(contentType)) {
                return this.context.getResources().getString(R.string.entry_of_message_contact);
            }
        }
        for (String contentTypeImage2222 : CONTENT_TYPE_CALENDARS) {
            if (contentTypeImage2222.equals(contentType)) {
                return this.context.getResources().getString(R.string.entry_of_message_calendar_appointment);
            }
        }
        return CONTENT_TYPE_HTML.equals(contentType) ? this.context.getResources().getString(R.string.entry_of_message_notext) : null;
    }
}

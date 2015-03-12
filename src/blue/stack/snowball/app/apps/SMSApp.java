package blue.stack.snowball.app.apps;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.PhoneLookup;
import android.telephony.SmsMessage;
import android.util.Log;
import blue.stack.snowball.app.apps.mms.MmsMonitor;
import blue.stack.snowball.app.apps.mms.MmsMonitor.MmsListener;
import blue.stack.snowball.app.apps.mms.MmsMonitor.MmsMessage;
import blue.stack.snowball.app.core.GuiceModule;
import blue.stack.snowball.app.inbox.InboxManager;
import blue.stack.snowball.app.inbox.Message;
import blue.stack.snowball.app.inbox.RawMessage;
import blue.stack.snowball.app.inbox.User;
import blue.stack.snowball.app.notifications.NotificationManager;
import blue.stack.snowball.app.photos.ProfilePhoto;
import blue.stack.snowball.app.photos.ProfilePhotoManager;
import blue.stack.snowball.app.settings.Settings;
import blue.stack.snowball.app.settings.Settings.SettingChangeListener;

import com.google.inject.Inject;

public class SMSApp implements App, SettingChangeListener, MmsListener {
    private static final String ACTION_RECEIVED_SMS = "android.provider.Telephony.SMS_RECEIVED";
    public static final String APP_ID = "com.squanda.swoop.smsapp";
    private static final String HANGOUTS_PACKAGE_NAME = "com.google.android.talk";
    private static final String TAG = "SMSApp";
    Context context;
    boolean isAppEnabled;
    MmsMonitor mmsMonitor;
    @Inject
    Settings settings;
    SMSMessageReceiver smsMessageReceiver;

    class SMSMessageReceiver extends BroadcastReceiver {
        SMSMessageReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if (SMSApp.this.isAppEnabled && intent.getAction().equals(ACTION_RECEIVED_SMS)) {
                Bundle bundle = intent.getExtras();
                if (bundle != null) {
                    Object[] pdus = (Object[]) bundle.get("pdus");
                    if (pdus != null) {
                        SMSApp.this.onSMSReceived(SmsMessage.createFromPdu((byte[]) pdus[0]));
                    }
                }
            }
        }
    }

    public void start(Context context) {
        this.context = context;
        GuiceModule.get().injectMembers(this);
        this.smsMessageReceiver = new SMSMessageReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_RECEIVED_SMS);
        context.registerReceiver(this.smsMessageReceiver, filter);
        this.settings.registerSettingChangeListener(this);
        updateEnabledState();
        MmsMonitor mmsMonitor = new MmsMonitor();
        mmsMonitor.start(context);
        mmsMonitor.addMMSListener(this);
    }

    public void stop() {
        if (this.smsMessageReceiver != null) {
            this.context.unregisterReceiver(this.smsMessageReceiver);
            this.smsMessageReceiver = null;
        }
        if (this.mmsMonitor != null) {
            this.mmsMonitor.removeMMSListener(this);
            this.mmsMonitor.stop();
            this.mmsMonitor = null;
        }
        this.settings.unregisterSettingChangeListener(this);
        this.context = null;
    }

    public void restart() {
        Context savedContext = this.context;
        if (savedContext == null) {
            Log.d(TAG, "Failed to restart because we have a NULL context!!");
            return;
        }
        stop();
        start(savedContext);
    }

    public String getAppId() {
        return APP_ID;
    }

    public boolean isAppInstalled() {
        PackageManager pm = this.context.getPackageManager();
        Intent launchIntent = getLaunchIntent();
        return (launchIntent == null || launchIntent.resolveActivity(pm) == null) ? false : true;
    }

    public Intent getLaunchIntent() {
        return this.context.getPackageManager().getLaunchIntentForPackage(getAppPackageName());
    }

    public Intent getLaunchIntentForMessage(Message message) {
        User sender = message.getSender();
        String phoneNumber = null;
        if (sender != null) {
            phoneNumber = sender.getAppSpecificUserId();
        }
        return getLaunchIntentWithPhoneNumber(phoneNumber);
    }

    Intent getLaunchIntentWithPhoneNumber(String phoneNumber) {
        if (phoneNumber == null) {
            return getLaunchIntent();
        }
        String packageName = getAppPackageName();
        Intent intent = new Intent("android.intent.action.SENDTO");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.setPackage(packageName);
        if (phoneNumber.contains(",")) {
            intent.setData(Uri.parse("mmsto:" + phoneNumber));
        } else {
            intent.setData(Uri.parse("smsto:" + phoneNumber));
        }
        return intent.resolveActivity(this.context.getPackageManager()) == null ? getLaunchIntent() : intent;
    }

    public String getAppName() {
        return "SMS";
    }

    public String getAppPackageName() {
        return AppIds.getSMSAppPackageName(this.context);
    }

    public Drawable getAppIcon() {
        try {
            return this.context.getPackageManager().getApplicationIcon(getAppPackageName());
        } catch (NameNotFoundException e) {
            return null;
        }
    }

    public ProfilePhoto getProfilePhotoForMessage(Message message) {
        User sender = message.getSender();
        return sender == null ? null : ((ProfilePhotoManager) GuiceModule.get().getInstance(ProfilePhotoManager.class)).getProfilePhoto(sender.getAppId(), sender.getAppSpecificUserId(), sender.getDisplayName());
    }

    public boolean shouldRemoveDuplicates() {
        return false;
    }

    public void onSettingChanged(Settings settings, String key) {
        if (key.equals(Settings.KEY_EXCLUDED_APPS)) {
            updateEnabledState();
        }
    }

    void updateEnabledState() {
        if (this.settings.getExcludedApps().contains(getAppId())) {
            this.isAppEnabled = false;
        } else {
            this.isAppEnabled = true;
        }
    }

    public static String getContactNameFromNumber(Context context, String number) {
        if (number == null) {
            return number;
        }
        String name = number;
        String[] projection = new String[]{"display_name"};
        Cursor cursor = context.getContentResolver().query(Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number)), projection, null, null, null);
        if (cursor.moveToFirst()) {
            name = cursor.getString(cursor.getColumnIndex("display_name"));
        }
        return name;
    }

    void onSMSReceived(SmsMessage sms) {
        if (!HANGOUTS_PACKAGE_NAME.equals(getAppPackageName())) {
            String address = sms.getOriginatingAddress();
            String displayAddress = sms.getDisplayOriginatingAddress();
            String from = getContactNameFromNumber(this.context, address);
            String messageBody = sms.getMessageBody();
            long timestamp = sms.getTimestampMillis();
            ((InboxManager) GuiceModule.get().getInstance(InboxManager.class)).saveMesssage(new RawMessage(getAppId(), address, from, messageBody, timestamp, null, null, null));
            ((NotificationManager) GuiceModule.get().getInstance(NotificationManager.class)).removeNotificationsForSMS(getAppPackageName(), from, address, displayAddress, messageBody, timestamp);
        }
    }

    public void onMmsReceived(MmsMessage mmsMessage) {
        if (!HANGOUTS_PACKAGE_NAME.equals(getAppPackageName())) {
            String senderAddress = mmsMessage.getSenderAddress();
            List<String> receipientAddresses = mmsMessage.getRecipientAddresses();
            String messageBody = mmsMessage.getMessage();
            long timestamp = System.currentTimeMillis();
            String senderName = getContactNameFromNumber(this.context, senderAddress);
            String from = senderName;
            if (receipientAddresses.size() > 0) {
                for (String receipient : receipientAddresses) {
                    from = from + ", " + getContactNameFromNumber(this.context, receipient);
                }
                messageBody = senderName + ": " + messageBody;
            }
            String senderId = getSenderId(senderAddress, mmsMessage.getRecipientAddresses());
            Log.d(TAG, "MMS senderId = " + senderId);
            Log.d(TAG, "MMS from = " + from);
            Log.d(TAG, "MMS message = " + messageBody);
            InboxManager inboxManager = (InboxManager) GuiceModule.get().getInstance(InboxManager.class);
            inboxManager.saveMesssage(new RawMessage(getAppId(), senderId, from, messageBody, timestamp, null, null, null));
            ((NotificationManager) GuiceModule.get().getInstance(NotificationManager.class)).removeNotificationsForSMS(getAppPackageName(), from, senderAddress, null, messageBody, timestamp);
        }
    }

    String getSenderId(String senderAddress, List<String> recipientAddresses) {
        if (recipientAddresses == null || recipientAddresses.size() == 0) {
            return senderAddress;
        }
        List<String> addresses = new ArrayList(recipientAddresses);
        addresses.add(senderAddress);
        Collections.sort(addresses);
        boolean addedOnce = false;
        StringBuffer sb = new StringBuffer();
        for (String address : addresses) {
            if (addedOnce) {
                sb.append(",");
            } else {
                addedOnce = true;
            }
            sb.append(address);
        }
        return sb.toString();
    }
}

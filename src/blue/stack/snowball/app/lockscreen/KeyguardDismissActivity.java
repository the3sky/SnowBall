package blue.stack.snowball.app.lockscreen;

import blue.stack.snowball.app.R;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import blue.stack.snowball.app.MigrateActivity;
import blue.stack.snowball.app.apps.App;
import blue.stack.snowball.app.apps.AppManager;
import blue.stack.snowball.app.apps.AppManager.AppLaunchMethod;
import blue.stack.snowball.app.core.GuiceModule;
import blue.stack.snowball.app.inbox.Message;
import blue.stack.snowball.app.inbox.ui.InboxViewManager;
import blue.stack.snowball.app.logging.EventLogger;
import blue.stack.snowball.app.logging.EventLoggerManager;
import blue.stack.snowball.app.oob.OOBManager;

import com.google.inject.Inject;

public class KeyguardDismissActivity extends Activity implements LockScreenListener {
    private static final String KEY_APP_ID = "appId";
    private static final String KEY_MESSAGE_ID = "message_id";
    private static final String KEY_TYPE = "type";
    private static final String LAUNCH_TYPE_APP = "app";
    private static final String LAUNCH_TYPE_INBOX = "inbox";
    private static final String LAUNCH_TYPE_MESSAGE = "message";
    @Inject
    EventLoggerManager eventLoggerManager;
    @Inject
    LockScreenManager lockScreenManager;
    @Inject
    OOBManager oobManager;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_keyguard_dismiss);
        GuiceModule.get().injectMembers(this);
        this.lockScreenManager.addListener(this);
        getWindow().addFlags(4194304);
    }

    protected void onDestroy() {
        this.lockScreenManager.removeListener(this);
        super.onDestroy();
    }

    public void onLockScreenStarted(LockScreenManager manager) {
    }

    public void onLockScreenStopped(LockScreenManager manager) {
        Intent intent = getIntent();
        String type = intent.getStringExtra(KEY_TYPE);
        if (LAUNCH_TYPE_INBOX.equals(type)) {
            launchInbox();
        } else if (LAUNCH_TYPE_MESSAGE.equals(type)) {
            launchMessage(intent.getIntExtra(KEY_MESSAGE_ID, -1));
        } else if (LAUNCH_TYPE_APP.equals(type)) {
            launchApp(intent.getStringExtra(KEY_APP_ID));
        }
        finish();
    }

    public void onPhoneCall(LockScreenManager manager) {
    }

    public void onPhoneCallEnded(LockScreenManager manager) {
    }

    void launchInbox() {
        ((InboxViewManager) GuiceModule.get().getInstance(InboxViewManager.class)).openDrawer();
        this.eventLoggerManager.getEventLogger().addEvent(EventLogger.SNOWBALL_OPENED, EventLogger.PROPERTY_SNOWBALL_OPENED_METHOD, EventLogger.VALUE_SNOWBALL_OPENED_VIA_LOCKSCREEN);
    }

    void launchMessage(int messageId) {
        if (this.oobManager.getOOBNeedsShadeMigration()) {
            Intent intent = new Intent(getApplicationContext(), MigrateActivity.class);
            intent.addFlags(268468224);
            startActivity(intent);
            return;
        }
        ((AppManager) GuiceModule.get().getInstance(AppManager.class)).launchAppWithMessageId(messageId, AppLaunchMethod.Lockscreen);
    }

    void launchApp(String appId) {
        if (this.oobManager.getOOBNeedsShadeMigration()) {
            Intent intent = new Intent(getApplicationContext(), MigrateActivity.class);
            intent.addFlags(268468224);
            startActivity(intent);
            return;
        }
        ((AppManager) GuiceModule.get().getInstance(AppManager.class)).launchAppForAppId(appId, AppLaunchMethod.Lockscreen);
    }

    public static void launchAppForMessage(Context context, Message message) {
        Intent intent = new Intent(context, KeyguardDismissActivity.class);
        intent.putExtra(KEY_TYPE, LAUNCH_TYPE_MESSAGE);
        intent.putExtra(KEY_MESSAGE_ID, message.getId());
        intent.addFlags(268468224);
        context.startActivity(intent);
    }

    public static void launchApp(Context context, App app) {
        Intent intent = new Intent(context, KeyguardDismissActivity.class);
        intent.putExtra(KEY_TYPE, LAUNCH_TYPE_APP);
        intent.putExtra(KEY_APP_ID, app.getAppId());
        intent.addFlags(268468224);
        context.startActivity(intent);
    }

    public static void launchInbox(Context context) {
        Intent intent = new Intent(context, KeyguardDismissActivity.class);
        intent.putExtra(KEY_TYPE, LAUNCH_TYPE_INBOX);
        intent.addFlags(268468224);
        context.startActivity(intent);
    }
}

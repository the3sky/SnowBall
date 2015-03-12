package blue.stack.snowball.app.oob;

import java.util.List;

import blue.stack.snowball.app.R;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import blue.stack.snowball.app.apps.App;
import blue.stack.snowball.app.apps.AppManager;
import blue.stack.snowball.app.core.GuiceModule;
import blue.stack.snowball.app.inbox.InboxManager;
import blue.stack.snowball.app.inbox.RawMessage;

import com.google.inject.Inject;

public class OOBWelcomeMessageGenerator {
    private static final long MESSAGE_GAP = 96000;
    @Inject
    AppManager appManager;
    @Inject
    Context context;
    @Inject
    InboxManager inboxManager;

    public OOBWelcomeMessageGenerator() {
        GuiceModule.get().injectMembers(this);
    }

    public boolean generateWelcomeMessage() {
        List<App> quickLaunchApps = this.appManager.getQuickLaunchApps();
        if (quickLaunchApps.size() == 0) {
            return false;
        }
        App janeyApp;
        App valentinaApp;
        App laurenApp;
        if (quickLaunchApps.size() == 1) {
            janeyApp = (App) quickLaunchApps.get(0);
            valentinaApp = janeyApp;
            laurenApp = janeyApp;
        } else if (quickLaunchApps.size() == 2) {
            janeyApp = (App) quickLaunchApps.get(0);
            valentinaApp = (App) quickLaunchApps.get(1);
            laurenApp = janeyApp;
        } else {
            janeyApp = (App) quickLaunchApps.get(0);
            valentinaApp = (App) quickLaunchApps.get(1);
            laurenApp = (App) quickLaunchApps.get(2);
        }
        App snowballTeamApp = valentinaApp;
        String janeyName = this.context.getResources().getString(R.string.dummy_person_one);
        BitmapDrawable janeyPhoto = (BitmapDrawable) this.context.getResources().getDrawable(R.drawable.janey_profile);
        String valentinaName = this.context.getResources().getString(R.string.dummy_person_two);
        BitmapDrawable valentinaPhoto = (BitmapDrawable) this.context.getResources().getDrawable(R.drawable.valentina_profile);
        String laurenName = this.context.getResources().getString(R.string.dummy_person_three);
        BitmapDrawable laurenPhoto = (BitmapDrawable) this.context.getResources().getDrawable(R.drawable.lauren_profile);
        String snowballTeamName = this.context.getResources().getString(R.string.dummy_person_four);
        String janeyMessage = this.context.getResources().getString(R.string.dummy_message_one);
        long janeyTimestamp = System.currentTimeMillis();
        String valentinaMessage = this.context.getResources().getString(R.string.dummy_message_two);
        long valentinaTimestamp = janeyTimestamp - MESSAGE_GAP;
        String laurenMessage = this.context.getResources().getString(R.string.dummy_message_three);
        long laurenTimestamp = valentinaTimestamp - MESSAGE_GAP;
        String snowballTeamMessage = this.context.getResources().getString(R.string.dummy_message_four);
        long snowballTeamTimestamp = laurenTimestamp - MESSAGE_GAP;
        RawMessage message1 = new RawMessage(janeyApp.getAppId(), null, janeyName, janeyMessage, janeyTimestamp, janeyPhoto.getBitmap(), null, null);
        message1.setState(1);
        RawMessage message2 = new RawMessage(valentinaApp.getAppId(), null, valentinaName, valentinaMessage, valentinaTimestamp, valentinaPhoto.getBitmap(), null, null);
        message2.setState(1);
        RawMessage message3 = new RawMessage(laurenApp.getAppId(), null, laurenName, laurenMessage, laurenTimestamp, laurenPhoto.getBitmap(), getSendFeedbackPendingIntent(), null);
        message3.setState(1);
        RawMessage message4 = new RawMessage(snowballTeamApp.getAppId(), null, snowballTeamName, snowballTeamMessage, snowballTeamTimestamp, null, null, null);
        message4.setState(1);
        this.inboxManager.injectMessage(message4);
        this.inboxManager.injectMessage(message3);
        this.inboxManager.injectMessage(message2);
        this.inboxManager.injectMessage(message1);
        return true;
    }

    PendingIntent getSendFeedbackPendingIntent() {
        Intent intent = new Intent("android.intent.action.VIEW", Uri.parse("mailto:feedback@squanda.com?subject=Snowball%20Feedback"));
        intent.addFlags(268435456);
        return PendingIntent.getActivity(this.context, 0, intent, 0);
    }
}

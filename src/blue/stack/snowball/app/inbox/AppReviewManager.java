package blue.stack.snowball.app.inbox;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import blue.stack.snowball.app.logging.EventLogger;
import blue.stack.snowball.app.logging.EventLoggerManager;
import blue.stack.snowball.app.settings.Settings;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class AppReviewManager {
    private static final int FIRST_REVIEW_REQUEST = 259200000;
    private static final int MAX_REVIEW_REQUESTS = 3;
    private static final int REVIEW_REQUEST_INTERVAL = 604800000;
    private static final String TAG = "AppReviewManager";
    @Inject
    Context context;
    @Inject
    EventLoggerManager eventLoggerManager;
    @Inject
    Settings settings;

    @Inject
    private AppReviewManager() {
    }

    @Inject
    private void start() {
    }

    public void stop() {
    }

    public boolean shouldAskForReview() {
        boolean z = false;
        if (!this.settings.getReviewComplete()) {
            long reviewRequestCount = (long) this.settings.getReviewRequestedCount();
            if (reviewRequestCount < 3) {
                if (System.currentTimeMillis() - this.settings.getFirstReviewRequest() > 259200000 + (604800000 * reviewRequestCount)) {
                    z = true;
                }
            }
        }
        return z;
    }

    public void rateApp() {
        Intent intent = new Intent("android.intent.action.VIEW", Uri.parse("market://details?id=" + this.context.getApplicationContext().getPackageName()));
        intent.addFlags(268435456);
        try {
            this.context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            this.context.startActivity(new Intent("android.intent.action.VIEW", Uri.parse("http://play.google.com/store/apps/details?id=" + this.context.getApplicationContext().getPackageName())));
        }
        setReviewCompleted(true);
        this.eventLoggerManager.getEventLogger().addEvent(EventLogger.APP_REVIEW_RATED, "count", Integer.toString(this.settings.getReviewRequestedCount()));
    }

    public void giveFeedback() {
        Intent intent = new Intent("android.intent.action.VIEW", Uri.parse("mailto:feedback@squanda.com?subject=Snowball%20Feedback"));
        intent.addFlags(268435456);
        try {
            this.context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Log.d(TAG, "ActivityNotFoundException when trying to launch email feedback");
        }
        setReviewCompleted(false);
        this.eventLoggerManager.getEventLogger().addEvent(EventLogger.APP_REVIEW_FEEDBACK_GIVEN, "count", Integer.toString(this.settings.getReviewRequestedCount()));
    }

    public void reviewSkipped() {
        setReviewCompleted(false);
        this.eventLoggerManager.getEventLogger().addEvent(EventLogger.APP_REVIEW_SKIPPED, "count", Integer.toString(this.settings.getReviewRequestedCount()));
    }

    void setReviewCompleted(boolean success) {
        if (success) {
            this.settings.setReviewComplete(true);
        }
        this.settings.setReviewRequestedCount(this.settings.getReviewRequestedCount() + 1);
    }
}

package blue.stack.snowball.app.logging;

import android.content.Context;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class EventLoggerManager {
    @Inject
    Context context;
    EventLogger eventLogger;

    @Inject
    private EventLoggerManager() {
        this.eventLogger = null;
    }

    @Inject
    private void start() {
        this.eventLogger = new EventLogger(this.context);
    }

    public void stop() {
        if (this.eventLogger != null) {
            this.eventLogger.onDestroy();
        }
    }

    public EventLogger getEventLogger() {
        return this.eventLogger;
    }
}

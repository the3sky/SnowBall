package blue.stack.snowball.app.ui;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class TimerTextRefreshManager {
    private static final int REFRESH_TIME_INTERVAL = 10000;
    @Inject
    private Context context;
    private ArrayList<TimerTextRefreshListener> listeners;
    private TimerManagerReceiver receiver;
    private Timer secondsTimer;

    class AnonymousClass_2 extends TimerTask {
        final /* synthetic */ Handler val$handler;

        AnonymousClass_2(Handler handler) {
            this.val$handler = handler;
        }

        public void run() {
            this.val$handler.obtainMessage().sendToTarget();
        }
    }

    class TimerManagerReceiver extends BroadcastReceiver {
        TimerManagerReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("android.intent.action.SCREEN_OFF")) {
                TimerTextRefreshManager.this.onScreenOff();
            } else if (intent.getAction().equals("android.intent.action.SCREEN_ON")) {
                TimerTextRefreshManager.this.onScreenOn();
            }
        }
    }

    public static interface TimerTextRefreshListener {
        void onRefreshTime(TimerTextRefreshManager timerTextRefreshManager);
    }

    @Inject
    private TimerTextRefreshManager() {
        this.listeners = new ArrayList();
    }

    @Inject
    private void start() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.SCREEN_OFF");
        filter.addAction("android.intent.action.SCREEN_ON");
        this.receiver = new TimerManagerReceiver();
        this.context.registerReceiver(this.receiver, filter);
    }

    public void stop() {
        this.context.unregisterReceiver(this.receiver);
        stopTimer();
        this.listeners.clear();
    }

    public void addListener(TimerTextRefreshListener listener) {
        this.listeners.add(listener);
        refreshTimerState();
    }

    public void removeListener(TimerTextRefreshListener listener) {
        this.listeners.remove(listener);
        refreshTimerState();
    }

    private void startTimer() {
        Log.d("TimerManager", "Start Timer");
        if (this.secondsTimer == null) {
            Handler handler = new Handler() {
                public void handleMessage(Message msg) {
                    TimerTextRefreshManager.this.fireOnRefreshTime();
                }
            };
            this.secondsTimer = new Timer();
            this.secondsTimer.scheduleAtFixedRate(new AnonymousClass_2(handler), new Date(), 10000);
        }
    }

    private void fireOnRefreshTime() {
        Iterator i$ = this.listeners.iterator();
        while (i$.hasNext()) {
            ((TimerTextRefreshListener) i$.next()).onRefreshTime(this);
        }
    }

    private void stopTimer() {
        Log.d("TimerManager", "Stop Timer");
        this.secondsTimer.cancel();
        this.secondsTimer = null;
    }

    private void refreshTimerState() {
        boolean startTimer = true;
        if (!isScreenOn()) {
            startTimer = false;
        }
        if (this.listeners.size() == 0) {
            startTimer = false;
        }
        if (this.secondsTimer != null && !startTimer) {
            stopTimer();
        } else if (this.secondsTimer == null && startTimer) {
            startTimer();
        }
    }

    void onScreenOn() {
        fireOnRefreshTime();
        refreshTimerState();
    }

    void onScreenOff() {
        refreshTimerState();
    }

    public boolean isScreenOn() {
        return ((PowerManager) this.context.getSystemService("power")).isScreenOn();
    }
}

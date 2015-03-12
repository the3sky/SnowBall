package blue.stack.snowball.app.logging;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import blue.stack.snowball.app.network.WebServiceManager;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class RemoteLogger {
	private static final int FLUSH_THRESHOLD = 30000;
	private static final int FLUSH_TIMER_INTERVAL = 60000;
	private static final String TAG = "RemoteLogger";
	@Inject
	Context context;
	Timer flushTimer;
	long lastFlushTimetamp;
	StringBuffer logBuffer;
	List<RemoteDiagnosticsListener> remoteDiagnosticsListener;
	boolean remoteLoggingEnabled;
	@Inject
	WebServiceManager webServiceManager;

	class AnonymousClass_2 extends TimerTask {
		final/* synthetic */Handler val$flushTimerHandler;

		AnonymousClass_2(Handler handler) {
			this.val$flushTimerHandler = handler;
		}

		@Override
		public void run() {
			this.val$flushTimerHandler.obtainMessage().sendToTarget();
		}
	}

	@Inject
	void start() {
		this.remoteLoggingEnabled = false;
		this.remoteDiagnosticsListener = new ArrayList();
	}

	public void stop() {
		stopFlushTimer();
		this.remoteDiagnosticsListener.clear();
		this.remoteDiagnosticsListener = null;
	}

	public void addRemoteDiagnosticsListener(RemoteDiagnosticsListener listener) {
		this.remoteDiagnosticsListener.add(listener);
	}

	public void removeRemoteDiagnosticsListener(RemoteDiagnosticsListener listener) {
		this.remoteDiagnosticsListener.remove(listener);
	}

	public void enableRemoteLogging(boolean enable) {
		this.remoteLoggingEnabled = enable;
		if (enable) {
			this.logBuffer = new StringBuffer();
			this.lastFlushTimetamp = System.currentTimeMillis();
			startFlushTimer();
			return;
		}
		stopFlushTimer();
		this.logBuffer = null;
	}

	public void sendDiagnostics() {
		RemoteDiagnostics remoteDiagnostics = new RemoteDiagnostics();
		for (RemoteDiagnosticsListener diagnosticsListener : this.remoteDiagnosticsListener) {
			diagnosticsListener.onRemoteDiagnosticsRequested(remoteDiagnostics);
		}
		if (remoteDiagnostics.toString().length() > 0) {
			this.webServiceManager.sendRemoteDiagnostics(remoteDiagnostics.toString());
		}
	}

	public void d(String tag, String log) {
		Log.d(TAG, log);
		if (this.remoteLoggingEnabled) {
			this.logBuffer.append("[");
			this.logBuffer.append(tag);
			this.logBuffer.append(" (");
			this.logBuffer.append(System.currentTimeMillis());
			this.logBuffer.append(")] ");
			this.logBuffer.append(log);
			this.logBuffer.append("\r\n");
			flushIfNeeded();
		}
	}

	public void flush() {
		if (this.remoteLoggingEnabled) {
			if (this.logBuffer != null && this.logBuffer.length() > 0) {
				this.webServiceManager.sendRemoteLogData(this.logBuffer.toString());
				this.logBuffer = new StringBuffer();
			}
			this.lastFlushTimetamp = System.currentTimeMillis();
		}
	}

	void flushIfNeeded() {
		if (this.remoteLoggingEnabled && System.currentTimeMillis() - this.lastFlushTimetamp > 30000) {
			flush();
		}
	}

	void startFlushTimer() {
	}

	void stopFlushTimer() {
		if (this.flushTimer != null) {
			this.flushTimer.cancel();
			this.flushTimer = null;
		}
	}
}

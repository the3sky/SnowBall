package blue.stack.snowball.app.overlay;

import java.util.Iterator;
import java.util.List;

import blue.stack.snowball.app.R;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.Context;
import android.graphics.Point;
import android.support.v4.widget.ExploreByTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.Button;
import blue.stack.snowball.app.apps.AppManager;
import blue.stack.snowball.app.apps.AppManager.AppLaunchMethod;
import blue.stack.snowball.app.core.ListenerHandler;
import blue.stack.snowball.app.core.ListenerSource;
import blue.stack.snowball.app.inbox.Message;
import blue.stack.snowball.app.logging.EventLoggerManager;
import blue.stack.snowball.app.notifications.Action;
import blue.stack.snowball.app.overlay.OverlayNotificationController.OverlayNotificationListener;
import blue.stack.snowball.app.overlay.ui.OverlayNotificationBarView;
import blue.stack.snowball.app.overlay.ui.OverlayNotificationBarView.NotificationViewListener;
import blue.stack.snowball.app.settings.Settings;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class OverlayNotificationController implements ListenerSource<OverlayNotificationListener> {
	static String TAG;
	@Inject
	AppManager appManager;
	@Inject
	Context context;
	@Inject
	EventLoggerManager eventLoggerManager;
	ListenerHandler<OverlayNotificationListener> listenerHandler;
	OverlayNotificationBarView notificationBarView;
	View notificationTouchListener;
	@Inject
	Settings settings;

	class AnonymousClass_3 implements OnClickListener {
		final/* synthetic */PendingIntent val$intent;

		AnonymousClass_3(PendingIntent pendingIntent) {
			this.val$intent = pendingIntent;
		}

		@Override
		public void onClick(View v) {
			OverlayNotificationController.this.fireOnNotificationTouched();
			try {
				this.val$intent.send();
			} catch (CanceledException e) {
				Log.d(TAG, "Failed to launch pending intent because it was cancelled");
			}
			OverlayNotificationController.this.closePopup();
		}
	}

	class AnonymousClass_4 implements OnClickListener {
		final/* synthetic */Message val$message;

		AnonymousClass_4(Message message) {
			this.val$message = message;
		}

		@Override
		public void onClick(View v) {
			OverlayNotificationController.this.appManager
					.launchAppForMessage(this.val$message, AppLaunchMethod.HeadsUp);
			OverlayNotificationController.this.closePopup();
			OverlayNotificationController.this.fireOnNotificationTouched();
		}
	}

	public static interface OverlayNotificationListener {
		void onNotificationClosed();

		void onNotificationOpening();

		void onNotificationTouched();
	}

	public static class DefaultOverlayNotificationListener implements OverlayNotificationListener {
		@Override
		public void onNotificationOpening() {
		}

		@Override
		public void onNotificationClosed() {
		}

		@Override
		public void onNotificationTouched() {
		}
	}

	static {
		TAG = "OverlayNotificationController";
	}

	@Inject
	private OverlayNotificationController() {
		this.listenerHandler = new ListenerHandler();
	}

	@Inject
	void init() {
		this.notificationBarView = (OverlayNotificationBarView) ((LayoutInflater) this.context
				.getSystemService("layout_inflater")).inflate(R.layout.overlay_notification_bar, null);
		this.notificationTouchListener = new View(this.context);
		this.notificationTouchListener.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return OverlayNotificationController.this.notificationBarView.dispatchTouchEvent(event);
			}
		});
		this.notificationBarView.addListener(this, new NotificationViewListener() {
			@Override
			public void onNotificationViewClosed() {
				OverlayNotificationController.this.removeFromWindow();
				OverlayNotificationController.this.fireOnNotificationClosed();
			}
		});
	}

	public void closePopup() {
		this.notificationBarView.closePopup();
	}

	private void addToWindow() {
		if (this.notificationBarView.getParent() == null) {
			WindowManager windowManager = (WindowManager) this.context.getSystemService("window");
			Point windowSize = new Point();
			windowManager.getDefaultDisplay().getSize(windowSize);
			int widthMeasureSpec = MeasureSpec.makeMeasureSpec(Math.min(windowSize.x, windowSize.y), 1073741824);
			int heightMeasureSpec = MeasureSpec.makeMeasureSpec(windowSize.y, ExploreByTouchHelper.INVALID_ID);
			this.notificationBarView.setLayoutParams(new LayoutParams(-1, -2));
			this.notificationBarView.measure(widthMeasureSpec, heightMeasureSpec);
			int width = this.notificationBarView.getMeasuredWidth();
			int height = this.notificationBarView.getMeasuredHeight();
			WindowManager.LayoutParams params = new WindowManager.LayoutParams(width, height, 2010, 16843544, -3);
			params.gravity = 49;
			windowManager.addView(this.notificationBarView, params);
			params = new WindowManager.LayoutParams(width, height, 2010, 16843528, -3);
			params.gravity = 49;
			windowManager.addView(this.notificationTouchListener, params);
			Log.d(TAG, "Added overlay views to display");
		}
	}

	public void removeFromWindow() {
		if (this.notificationBarView.getParent() != null) {
			((WindowManager) this.context.getSystemService("window")).removeView(this.notificationBarView);
		}
		if (this.notificationTouchListener.getParent() != null) {
			((WindowManager) this.context.getSystemService("window")).removeView(this.notificationTouchListener);
		}
		Button buttonOne = (Button) this.notificationBarView.findViewById(R.id.inbox_action_button_one);
		Button buttonTwo = (Button) this.notificationBarView.findViewById(R.id.inbox_action_button_one);
		this.notificationBarView.setOnClickListener(null);
		buttonOne.setOnClickListener(null);
		buttonOne.setOnClickListener(null);
		Log.d(TAG, "Removed overlay views from display");
	}

	public void performHeadsUpPopup(Message message) {
		closePopup();
		this.notificationBarView.setupView(message);
		List<Action> actions = message.getActions();
		if (actions != null && actions.size() >= 1) {
			setViewOnClickIntent(this.notificationBarView.findViewById(R.id.inbox_action_button_one),
					actions.get(0).actionIntent);
		}
		if (actions != null && actions.size() >= 2) {
			setViewOnClickIntent(this.notificationBarView.findViewById(R.id.inbox_action_button_two),
					actions.get(1).actionIntent);
		}
		setViewOnClickIntent(this.notificationBarView.findViewById(R.id.translating_layer), message);
		addToWindow();
		fireOnNotificationOpening();
		this.notificationBarView.performHeadsUpPopup();
	}

	public void stop() {
		this.listenerHandler.clearAll();
		this.notificationBarView.removeListener(this);
		this.notificationBarView.stop();
	}

	private void setViewOnClickIntent(View view, PendingIntent intent) {
		view.setOnClickListener(new AnonymousClass_3(intent));
	}

	private void setViewOnClickIntent(View view, Message message) {
		view.setOnClickListener(new AnonymousClass_4(message));
	}

	@Override
	public void addListener(Object handle, OverlayNotificationListener listener) {
		this.listenerHandler.addListener(handle, listener);
	}

	@Override
	public void addListener(OverlayNotificationListener listener) {
		this.listenerHandler.addListener(listener);
	}

	@Override
	public void removeListener(Object handle) {
		this.listenerHandler.removeListener(handle);
	}

	private void fireOnNotificationOpening() {
		Iterator i$ = this.listenerHandler.iterator();
		while (i$.hasNext()) {
			((OverlayNotificationListener) i$.next()).onNotificationOpening();
		}
	}

	private void fireOnNotificationClosed() {
		Iterator i$ = this.listenerHandler.iterator();
		while (i$.hasNext()) {
			((OverlayNotificationListener) i$.next()).onNotificationClosed();
		}
	}

	private void fireOnNotificationTouched() {
		Iterator i$ = this.listenerHandler.iterator();
		while (i$.hasNext()) {
			((OverlayNotificationListener) i$.next()).onNotificationTouched();
		}
	}
}

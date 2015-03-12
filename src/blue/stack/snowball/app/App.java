/**
 * 
 */
package blue.stack.snowball.app;

import android.app.Application;
import android.app.NotificationManager;

/**
 * @author BunnyBlue
 *
 */
public class App extends Application {
	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Application#onCreate()
	 */
	static App instance;
	NotificationManager notificationManager;

	/**
	 * @return the instance
	 */
	public static App getInstance() {
		return instance;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		instance = this;
		// NotificationManager notificationManager = (NotificationManager)
		// getSystemService(NOTIFICATION_SERVICE);
	}

	public NotificationManager getNoticeManger() {

		return notificationManager;
	}
}

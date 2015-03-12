package blue.stack.snowball.app.shade.ui;

import java.util.List;

import blue.stack.snowball.app.R;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import blue.stack.snowball.app.apps.App;
import blue.stack.snowball.app.apps.AppManager;
import blue.stack.snowball.app.apps.AppManager.AppLaunchMethod;
import blue.stack.snowball.app.core.GuiceModule;
import blue.stack.snowball.app.inbox.ui.InboxViewManager;
import blue.stack.snowball.app.lockscreen.LockScreenManager;
import blue.stack.snowball.app.settings.Settings;
import blue.stack.snowball.app.settings.Settings.SettingChangeListener;
import blue.stack.snowball.app.tools.UIUtilities;
import blue.stack.snowball.app.ui.pageview.LinePageIndicator;
import blue.stack.snowball.app.ui.pageview.PageIndicator;

import com.google.inject.Inject;

public class QuickLaunchView extends LinearLayout {
	@Inject
	AppManager appManager;
	QuickLaunchAdaptor mAdapter;
	PageIndicator mIndicator;
	ViewPager mPager;

	class QuickLaunchAdaptor extends PagerAdapter implements SettingChangeListener {
		static final int NUMBER_OF_APPS_PER_LINE = 5;
		private List<App> quickLaunchApps;
		@Inject
		Settings settings;
		private String smsPackageName;

		class AnonymousClass_1 implements OnClickListener {
			final/* synthetic */App val$app;

			AnonymousClass_1(App app) {
				this.val$app = app;
			}

			@Override
			public void onClick(View view) {
				GuiceModule.get().getInstance(InboxViewManager.class).closeDrawer();
				LockScreenManager lockScreenManager = GuiceModule.get().getInstance(LockScreenManager.class);
				if (lockScreenManager.isPhoneLocked()) {
					lockScreenManager.unlockAndLaunchApp(this.val$app);
				} else {
					QuickLaunchView.this.appManager.launchAppWithBackButton(this.val$app, AppLaunchMethod.Quicklaunch);
				}
			}
		}

		public QuickLaunchAdaptor() {
			this.quickLaunchApps = null;
			this.smsPackageName = "";
			GuiceModule.get().injectMembers(this);
			this.settings.registerSettingChangeListener(this);
			this.quickLaunchApps = QuickLaunchView.this.appManager.getQuickLaunchApps();
		}

		public List<App> getQuickLaunchApps() {
			return this.quickLaunchApps;
		}

		public void setQuickLaunchApps(List<App> quickLaunchApps) {
			this.quickLaunchApps = quickLaunchApps;
			notifyDataSetChanged();
		}

		@Override
		public int getItemPosition(Object object) {
			return -2;
		}

		@Override
		public int getCount() {
			return ((this.quickLaunchApps.size() - 1) / 5) + 1;
		}

		@Override
		public boolean isViewFromObject(View view, Object o) {
			return view == o;
		}

		@Override
		public void notifyDataSetChanged() {
			super.notifyDataSetChanged();
			View indicatorView = (View) QuickLaunchView.this.mIndicator;
			if (getCount() == 1) {
				indicatorView.setVisibility(8);
			} else {
				indicatorView.setVisibility(0);
			}
			this.smsPackageName = QuickLaunchView.this.appManager.getSMSAppPackageName();
		}

		@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			LayoutInflater inflater = (LayoutInflater) QuickLaunchView.this.getContext().getSystemService(
					"layout_inflater");
			LinearLayout pageView = (LinearLayout) inflater.inflate(R.layout.shade_quick_launch_page, null);
			for (int i = position * 5; i < (position * 5) + 5; i++) {
				ImageButton button = (ImageButton) inflater.inflate(R.layout.quicklaunch_button, null);
				button.setScaleType(ScaleType.FIT_CENTER);
				if (i < this.quickLaunchApps.size()) {
					App app = this.quickLaunchApps.get(i);
					button.setImageDrawable(app.getAppIcon());
					button.setOnClickListener(new AnonymousClass_1(app));
				} else {
					button.setBackground(null);
					button.setOnClickListener(null);
				}
				pageView.addView(button, new LayoutParams(-1, -1, 0.5f));
			}
			container.addView(pageView);
			return pageView;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((LinearLayout) object);
		}

		@Override
		public void onSettingChanged(Settings settings, String key) {
			if (key.equals(Settings.KEY_QUICK_LAUNCH_APPS) || key.equals(Settings.KEY_EXCLUDED_APPS)) {
				UIUtilities.delayOneFrame(new Runnable() {
					@Override
					public void run() {
						QuickLaunchAdaptor.this.quickLaunchApps = QuickLaunchView.this.appManager.getQuickLaunchApps();
						QuickLaunchAdaptor.this.notifyDataSetChanged();
					}
				});
			}
		}
	}

	public QuickLaunchView(Context context) {
		super(context);
		init(null, 0);
	}

	public QuickLaunchView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(attrs, 0);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public QuickLaunchView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(attrs, defStyleAttr);
	}

	private void init(AttributeSet attrs, int defStyle) {
		if (!isInEditMode()) {
			GuiceModule.get().injectMembers(this);
			((LayoutInflater) getContext().getSystemService("layout_inflater")).inflate(
					R.layout.shade_quick_launch_view, this, true);
			this.mAdapter = new QuickLaunchAdaptor();
			this.mPager = (ViewPager) findViewById(R.id.pager);
			this.mPager.setAdapter(this.mAdapter);
			this.mIndicator = (LinePageIndicator) findViewById(R.id.indicator);
			this.mIndicator.setViewPager(this.mPager);
			this.mAdapter.notifyDataSetChanged();
		}
	}

	public void checkDefaultSMSAppIcon() {
		if (!this.appManager.getQuickLaunchApps().equals(this.mAdapter.getQuickLaunchApps())) {
			this.mAdapter.setQuickLaunchApps(this.appManager.getQuickLaunchApps());
		}
		String smsPackage = this.appManager.getSMSAppPackageName();
		if (smsPackage == null) {
			return;
		}
		if (this.mAdapter.smsPackageName == null || !smsPackage.equals(this.mAdapter.smsPackageName)) {
			this.mAdapter.notifyDataSetChanged();
		}
	}
}

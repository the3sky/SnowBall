package blue.stack.snowball.app.swipe.ui;

import java.util.ArrayList;
import java.util.List;

import blue.stack.snowball.app.R;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import blue.stack.snowball.app.apps.App;
import blue.stack.snowball.app.apps.AppManager;
import blue.stack.snowball.app.apps.AppManager.AppLaunchMethod;
import blue.stack.snowball.app.core.GuiceModule;
import blue.stack.snowball.app.inbox.ui.InboxViewManager;
import blue.stack.snowball.app.ui.anim.ElasticInInterpolator;
import blue.stack.snowball.app.ui.anim.ElasticOutInterpolator;

import com.android.volley.DefaultRetryPolicy;

public class QuickLaunchView extends LinearLayout {
    List<ImageButton> buttons;
    View closeLeft;
    View closeRight;
    boolean exitOnRight;

    class AnonymousClass_1 implements OnClickListener {
        final /* synthetic */ App val$app;
        final /* synthetic */ AppManager val$appManager;

        AnonymousClass_1(AppManager appManager, App app) {
            this.val$appManager = appManager;
            this.val$app = app;
        }

        public void onClick(View view) {
            ((InboxViewManager) GuiceModule.get().getInstance(InboxViewManager.class)).closeDrawer();
            this.val$appManager.launchAppWithBackButton(this.val$app, AppLaunchMethod.Quicklaunch);
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

    public QuickLaunchView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, defStyleAttr);
    }

    private void init(AttributeSet attrs, int defStyle) {
        ((LayoutInflater) getContext().getSystemService("layout_inflater")).inflate(R.layout.quick_launch_view, this, true);
        this.closeLeft = findViewById(R.id.quick_launch_close_left);
        this.closeRight = findViewById(R.id.quick_launch_close_right);
        setExitOnRight(false);
        this.buttons = new ArrayList(4);
        this.buttons.add((ImageButton) findViewById(R.id.quick_launch_button1));
        this.buttons.add((ImageButton) findViewById(R.id.quick_launch_button2));
        this.buttons.add((ImageButton) findViewById(R.id.quick_launch_button3));
        this.buttons.add((ImageButton) findViewById(R.id.quick_launch_button4));
        this.buttons.add((ImageButton) findViewById(R.id.quick_launch_button5));
        if (!isInEditMode()) {
            updateQuickLaunchButtons();
        }
    }

    public void onWindowFocusChanged(boolean hasWindowFocus) {
        updateQuickLaunchButtons();
        super.onWindowFocusChanged(hasWindowFocus);
    }

    public void hide() {
        for (ImageButton button : this.buttons) {
            button.setScaleX(0.0f);
            button.setScaleY(0.0f);
            this.closeLeft.setScaleX(0.0f);
            this.closeRight.setScaleX(0.0f);
        }
    }

    public void setExitOnRight(boolean exitOnRight) {
        this.exitOnRight = true;
        if (exitOnRight) {
            this.closeRight.setVisibility(0);
            this.closeLeft.setVisibility(8);
            return;
        }
        this.closeRight.setVisibility(8);
        this.closeLeft.setVisibility(0);
    }

    public void animateIn(int duration) {
        int increment;
        int start = 0;
        int end = 0;
        if (this.exitOnRight) {
            start = this.buttons.size() - 1;
            increment = -1;
        } else {
            end = this.buttons.size() - 1;
            increment = 1;
        }
        int index = start;
        while (true) {
            ((ImageButton) this.buttons.get(index)).animate().scaleX(DefaultRetryPolicy.DEFAULT_BACKOFF_MULT).scaleY(DefaultRetryPolicy.DEFAULT_BACKOFF_MULT).setInterpolator(new ElasticOutInterpolator(1.2f, 2.0f)).setDuration((long) duration).start();
            if (index == end) {
                this.closeLeft.animate().scaleX(DefaultRetryPolicy.DEFAULT_BACKOFF_MULT).scaleY(DefaultRetryPolicy.DEFAULT_BACKOFF_MULT).setInterpolator(new ElasticOutInterpolator(1.2f, 2.0f)).setDuration((long) duration).start();
                this.closeRight.animate().scaleX(DefaultRetryPolicy.DEFAULT_BACKOFF_MULT).scaleY(DefaultRetryPolicy.DEFAULT_BACKOFF_MULT).setInterpolator(new ElasticOutInterpolator(1.2f, 2.0f)).setDuration((long) duration).start();
                return;
            }
            index += increment;
        }
    }

    public void animateOut(int duration) {
        int increment;
        int start = 0;
        int end = 0;
        if (this.exitOnRight) {
            end = this.buttons.size() - 1;
            increment = 1;
        } else {
            start = this.buttons.size() - 1;
            increment = -1;
        }
        int index = start;
        while (true) {
            ((ImageButton) this.buttons.get(index)).animate().scaleX(0.0f).scaleY(0.0f).setInterpolator(new ElasticInInterpolator(1.2f, 2.0f)).setDuration((long) duration).start();
            if (index == end) {
                this.closeLeft.animate().scaleX(0.0f).scaleY(0.0f).setInterpolator(new ElasticInInterpolator(1.2f, 2.0f)).setDuration((long) duration).start();
                this.closeRight.animate().scaleX(0.0f).scaleY(0.0f).setInterpolator(new ElasticInInterpolator(1.2f, 2.0f)).setDuration((long) duration).start();
                return;
            }
            index += increment;
        }
    }

    void updateQuickLaunchButtons() {
        AppManager appManager = (AppManager) GuiceModule.get().getInstance(AppManager.class);
        List<App> quickLaunchApps = appManager.getQuickLaunchApps();
        for (int i = 0; i < this.buttons.size(); i++) {
            ImageButton button = (ImageButton) this.buttons.get(i);
            if (i < quickLaunchApps.size()) {
                App app = (App) quickLaunchApps.get(i);
                button.setImageDrawable(app.getAppIcon());
                button.setOnClickListener(new AnonymousClass_1(appManager, app));
            } else {
                button.setImageDrawable(null);
                button.setOnClickListener(null);
            }
        }
    }
}

package blue.stack.snowball.app.shade.ui;

import java.util.Iterator;

import blue.stack.snowball.app.R;
import android.animation.Animator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import blue.stack.snowball.app.apps.AppManager;
import blue.stack.snowball.app.core.GuiceModule;
import blue.stack.snowball.app.core.ListenerHandler;
import blue.stack.snowball.app.core.ListenerSource;
import blue.stack.snowball.app.oob.OOBTutorialActivity;
import blue.stack.snowball.app.settings.Settings;
import blue.stack.snowball.app.shade.ui.ShadeNotificationBarView.NotificationViewListener;
import blue.stack.snowball.app.swipe.ui.YettiTabLayout;
import blue.stack.snowball.app.ui.anim.AnimationCache;
import blue.stack.snowball.app.ui.anim.AnimationFinishListener;
import blue.stack.snowball.app.ui.anim.AnimationPNGSequence.AnimationDetails;
import blue.stack.snowball.app.ui.anim.AnimationPNGSequence.IAnimationListener;

import com.google.inject.Inject;
import com.mobeta.android.dslv.DragSortController;

public class ShadeNotificationBarView extends RelativeLayout implements ListenerSource<NotificationViewListener> {
    private static final long FLEX_DURATION = 600;
    private static final long READ_DELAY = 1500;
    private static int SLIDE_TEXT_DURATION = 0;
    private static int SLIDE_TEXT_LAG = 0;
    private static int SLIDE_TIME = 0;
    private static final String TAG = "ShadeNotificationBarView";
    @Inject
    AnimationCache animationCache;
    @Inject
    AppManager appManager;
    private DisplayState displayState;
    private ListenerHandler<NotificationViewListener> listenerHandler;
    @Inject
    Settings settings;
    private int unreadCount;

    class AnonymousClass_3 implements Runnable {
        final /* synthetic */ AnimationFinishListener val$animationListener;

        AnonymousClass_3(AnimationFinishListener animationFinishListener) {
            this.val$animationListener = animationFinishListener;
        }

        public void run() {
            if (this.val$animationListener != null) {
                this.val$animationListener.onAnimationEnd(null);
            }
        }
    }

    static /* synthetic */ class AnonymousClass_5 {
        static final /* synthetic */ int[] $SwitchMap$com$squanda$swoop$app$shade$ui$ShadeNotificationBarView$DisplayState;

        static {
            $SwitchMap$com$squanda$swoop$app$shade$ui$ShadeNotificationBarView$DisplayState = new int[DisplayState.values().length];
            try {
                $SwitchMap$com$squanda$swoop$app$shade$ui$ShadeNotificationBarView$DisplayState[DisplayState.Normal.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$squanda$swoop$app$shade$ui$ShadeNotificationBarView$DisplayState[DisplayState.Migration.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
        }
    }

    enum DisplayState {
        Normal,
        Migration
    }

    public static interface NotificationViewListener {
        void onNotificationViewClosed();
    }

    class AnonymousClass_1 extends AnimationFinishListener {
        final /* synthetic */ int val$unreadCount;
        final /* synthetic */ YettiTabLayout val$yettiAnimationView;

        class AnonymousClass_2 implements IAnimationListener {
            final /* synthetic */ IAnimationListener val$nestedListener;

            AnonymousClass_2(IAnimationListener iAnimationListener) {
                this.val$nestedListener = iAnimationListener;
            }

            public void onAnimationUpdated(int frame) {
            }

            public void onAnimationFinished() {
                AnonymousClass_1.this.val$yettiAnimationView.setUnreadCount(ShadeNotificationBarView.this.unreadCount, this.val$nestedListener);
            }
        }

        AnonymousClass_1(int i, YettiTabLayout yettiTabLayout) {
            this.val$unreadCount = i;
            this.val$yettiAnimationView = yettiTabLayout;
        }

        public void onAnimationEnd(Animator animation) {
            IAnimationListener nestedListener = new IAnimationListener() {
                public void onAnimationUpdated(int frame) {
                }

                public void onAnimationFinished() {
                    ShadeNotificationBarView.this.setDisplayState(DisplayState.Normal);
                    ShadeNotificationBarView.this.closePopup(READ_DELAY);
                }
            };
            if (this.val$unreadCount == 0) {
                AnimationDetails animationDetails = this.val$yettiAnimationView.getRandomAnimation();
                animationDetails.listener = nestedListener;
                this.val$yettiAnimationView.playAnimation(animationDetails);
            } else if (this.val$yettiAnimationView.isRotated().booleanValue()) {
                this.val$yettiAnimationView.setUnreadCount(ShadeNotificationBarView.this.unreadCount, nestedListener);
            } else {
                this.val$yettiAnimationView.rotateAndShowValue(new AnonymousClass_2(nestedListener));
            }
        }
    }

    class AnonymousClass_2 extends AnimationFinishListener {
        final /* synthetic */ YettiTabLayout val$yettiAnimationView;

        AnonymousClass_2(YettiTabLayout yettiTabLayout) {
            this.val$yettiAnimationView = yettiTabLayout;
        }

        public void onAnimationEnd(Animator animation) {
            this.val$yettiAnimationView.playAnimation(this.val$yettiAnimationView.getRandomAnimation());
        }
    }

    public class DefaultShadenotificationListener implements NotificationViewListener {
        public void onNotificationViewClosed() {
        }
    }

    static {
        SLIDE_TIME = OOBTutorialActivity.PULSE_FADE_TIME;
        SLIDE_TEXT_DURATION = OOBTutorialActivity.TRANSITION_DELAY;
        SLIDE_TEXT_LAG = OOBTutorialActivity.PULSE_FADE_TIME;
    }

    public ShadeNotificationBarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.displayState = DisplayState.Normal;
        this.listenerHandler = new ListenerHandler();
        if (!isInEditMode()) {
            GuiceModule.get().injectMembers(this);
        }
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        ((YettiTabLayout) findViewById(R.id.yetti_tab_layout)).setUnreadCount(0);
        findViewById(R.id.translating_layer).setTranslationY(-getResources().getDimension(R.dimen.shade_status_bar_notification_height));
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!isInEditMode()) {
        }
    }

    private void clearUnreadCount() {
        YettiTabLayout yettiAnimationView = (YettiTabLayout) findViewById(R.id.yetti_tab_layout);
        yettiAnimationView.setUnreadCount(0, null);
        yettiAnimationView.playAnimation("IDLE");
        this.unreadCount = 0;
    }

    private void performCountPopup(int unreadCount) {
        setDisplayState(DisplayState.Normal);
        this.unreadCount = unreadCount;
        YettiTabLayout yettiAnimationView = (YettiTabLayout) findViewById(R.id.yetti_tab_layout);
        if (this.unreadCount == 0) {
            yettiAnimationView.setUnreadCount(0);
        }
        openPopup(new AnonymousClass_1(unreadCount, yettiAnimationView));
    }

    public void openMigrationPopup() {
        setDisplayState(DisplayState.Migration);
        YettiTabLayout yettiAnimationView = (YettiTabLayout) findViewById(R.id.yetti_tab_layout);
        yettiAnimationView.clearAnimation();
        openPopup(new AnonymousClass_2(yettiAnimationView));
    }

    private void openPopup(AnimationFinishListener animationListener) {
        View translationLayer = findViewById(R.id.translating_layer);
        cancelTranslation();
        translationLayer.setTranslationY(-getResources().getDimension(R.dimen.shade_status_bar_notification_height));
        translationLayer.animate().setDuration((long) SLIDE_TIME).translationYBy(getResources().getDimension(R.dimen.shade_status_bar_notification_height)).withEndAction(new AnonymousClass_3(animationListener));
    }

    private void cancelTranslation() {
        findViewById(R.id.translating_layer).animate().cancel();
    }

    public void closePopup() {
        closePopup(0);
    }

    public void closePopup(long startDelay) {
        View translationLayer = findViewById(R.id.translating_layer);
        cancelTranslation();
        translationLayer.animate().setStartDelay(startDelay).setDuration((long) SLIDE_TIME).translationYBy(-getResources().getDimension(R.dimen.shade_status_bar_notification_height)).withEndAction(new Runnable() {
            public void run() {
                ShadeNotificationBarView.this.fireOnNotificationViewClosed();
            }
        });
    }

    public void setDisplayState(DisplayState displayState) {
        this.displayState = displayState;
        YettiTabLayout yettiAnimationView;
        switch (AnonymousClass_5.$SwitchMap$com$squanda$swoop$app$shade$ui$ShadeNotificationBarView$DisplayState[displayState.ordinal()]) {
            case DragSortController.ON_DRAG /*1*/:
                findViewById(R.id.tool_tip_arrow).setVisibility(8);
                findViewById(R.id.tool_tip_text).setVisibility(8);
                yettiAnimationView = (YettiTabLayout) findViewById(R.id.yetti_tab_layout);
                yettiAnimationView.setVisibility(0);
                yettiAnimationView.setTranslationX(0.0f);
                yettiAnimationView.setTranslationY(0.0f);
                findViewById(R.id.translating_layer).setTranslationY(0.0f);
            case DragSortController.ON_LONG_PRESS /*2*/:
                findViewById(R.id.tool_tip_arrow).setVisibility(0);
                findViewById(R.id.tool_tip_text).setVisibility(0);
                yettiAnimationView = (YettiTabLayout) findViewById(R.id.yetti_tab_layout);
                yettiAnimationView.setVisibility(0);
                yettiAnimationView.setTranslationX(0.0f);
                yettiAnimationView.setTranslationY(0.0f);
                findViewById(R.id.translating_layer).setTranslationY(0.0f);
            default:
        }
    }

    public void stop() {
        this.listenerHandler.clearAll();
    }

    public void addListener(Object handle, NotificationViewListener listener) {
        this.listenerHandler.addListener(handle, listener);
    }

    public void addListener(NotificationViewListener listener) {
        this.listenerHandler.addListener(listener);
    }

    public void removeListener(Object handle) {
        this.listenerHandler.removeListener(handle);
    }

    private void fireOnNotificationViewClosed() {
        Iterator i$ = this.listenerHandler.iterator();
        while (i$.hasNext()) {
            ((NotificationViewListener) i$.next()).onNotificationViewClosed();
        }
    }
}

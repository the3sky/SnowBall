package blue.stack.snowball.app.swipe.ui;

import java.util.Random;

import blue.stack.snowball.app.R;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.widget.TextView;
import blue.stack.snowball.app.ui.anim.AnimationCache;
import blue.stack.snowball.app.ui.anim.AnimationPNGSequence;
import blue.stack.snowball.app.ui.anim.AnimationPNGSequence.AnimationDetails;
import blue.stack.snowball.app.ui.anim.AnimationPNGSequence.IAnimationListener;

import com.android.volley.DefaultRetryPolicy;

public class YettiTabLayout extends RelativeLayout {
    static Tuple<String, Integer>[] expressionAnimations;
    Random random;
    Boolean rotated;

    class AnonymousClass_1 implements Runnable {
        final /* synthetic */ TextView val$badgeCountText;
        final /* synthetic */ IAnimationListener val$listener;

        AnonymousClass_1(TextView textView, IAnimationListener iAnimationListener) {
            this.val$badgeCountText = textView;
            this.val$listener = iAnimationListener;
        }

        public void run() {
            this.val$badgeCountText.animate().scaleY(DefaultRetryPolicy.DEFAULT_BACKOFF_MULT).scaleX(DefaultRetryPolicy.DEFAULT_BACKOFF_MULT).setDuration(200).withEndAction(new Runnable() {
                public void run() {
                    if (AnonymousClass_1.this.val$listener != null) {
                        AnonymousClass_1.this.val$listener.onAnimationFinished();
                    }
                }
            });
        }
    }

    private static class Tuple<X, Y> {
        public final X x;
        public final Y y;

        public Tuple(X x, Y y) {
            this.x = x;
            this.y = y;
        }
    }

    class AnonymousClass_2 implements IAnimationListener {
        final /* synthetic */ IAnimationListener val$listener;

        AnonymousClass_2(IAnimationListener iAnimationListener) {
            this.val$listener = iAnimationListener;
        }

        public void onAnimationUpdated(int frame) {
        }

        public void onAnimationFinished() {
            AnimationDetails details2 = new AnimationDetails();
            details2.sequenceName = "IDLE_ROTATE";
            details2.oneShot = Boolean.valueOf(true);
            details2.delay = 0;
            details2.listener = new IAnimationListener() {
                public void onAnimationUpdated(int frame) {
                }

                public void onAnimationFinished() {
                    AnonymousClass_2.this.val$listener.onAnimationFinished();
                }
            };
            YettiTabLayout.this.getHeadAnimation().playAnimation(details2);
        }
    }

    static {
        expressionAnimations = new Tuple[]{new Tuple("IDLE_WINK", Integer.valueOf(10)), new Tuple("IDLE_OOH", Integer.valueOf(8)), new Tuple("IDLE_LAUGH", Integer.valueOf(-1)), new Tuple("IDLE_HAPPY", Integer.valueOf(8)), new Tuple("IDLE_CLOSEDMOUTH", Integer.valueOf(8)), new Tuple("IDLE_BLINK", Integer.valueOf(-1))};
    }

    public YettiTabLayout(Context context) {
        super(context);
        this.random = new Random();
        this.rotated = Boolean.valueOf(false);
    }

    public YettiTabLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.random = new Random();
        this.rotated = Boolean.valueOf(false);
    }

    public YettiTabLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.random = new Random();
        this.rotated = Boolean.valueOf(false);
    }

    public static void CacheAnimations(AnimationCache animationCache) {
        for (Tuple<String, Integer> anim : expressionAnimations) {
            animationCache.getAnimation((String) anim.x);
        }
    }

    private AnimationPNGSequence getHeadAnimation() {
        return (AnimationPNGSequence) findViewById(R.id.yetti_animation);
    }

    public void setUnreadCount(int unreadCount) {
        setUnreadCount(unreadCount, null);
    }

    public void setUnreadCount(int unreadCount, IAnimationListener listener) {
        TextView badgeCountText = (TextView) findViewById(R.id.text_badge_count);
        badgeCountText.setAlpha(0.0f);
        if (unreadCount == 0) {
            badgeCountText.setText("0");
            getHeadAnimation().playAnimation("IDLE");
            return;
        }
        String nextText = String.format("%d", new Object[]{Integer.valueOf(unreadCount)});
        getHeadAnimation().playAnimation("IDLE_BACK");
        badgeCountText.setText(nextText);
        badgeCountText.setAlpha(DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        badgeCountText.setScaleX(DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        badgeCountText.setScaleY(DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        badgeCountText.animate().scaleY(1.3f).scaleX(1.3f).setDuration(200).withEndAction(new AnonymousClass_1(badgeCountText, listener));
    }

    public void playAnimation(String sequenceName) {
        AnimationDetails details = new AnimationDetails();
        details.sequenceName = sequenceName;
        playAnimation(details);
    }

    public void playAnimation(AnimationDetails details) {
        this.rotated = Boolean.valueOf(false);
        getHeadAnimation().playAnimation(details);
    }

    public void clearAnimation() {
        super.clearAnimation();
        this.rotated = Boolean.valueOf(false);
        getHeadAnimation().playAnimation("IDLE");
        ((TextView) findViewById(R.id.text_badge_count)).animate().cancel();
    }

    public AnimationDetails getRandomAnimation() {
        int expressionIndex = this.random.nextInt(expressionAnimations.length);
        AnimationDetails details = new AnimationDetails();
        details.sequenceName = (String) expressionAnimations[expressionIndex].x;
        details.oneShot = Boolean.valueOf(true);
        details.loopEnd = ((Integer) expressionAnimations[expressionIndex].y).intValue();
        details.loopStart = details.loopEnd;
        details.loopCount = 6;
        return details;
    }

    public void playRandomAnimationAfterDelay(long delay) {
        AnimationDetails details = getRandomAnimation();
        details.delay = delay;
        playAnimation(details);
    }

    public Boolean isRotated() {
        return this.rotated;
    }

    public void rotateAndShowValue(IAnimationListener listener) {
        this.rotated = Boolean.valueOf(true);
        int expressionIndex = this.random.nextInt(expressionAnimations.length);
        AnimationDetails details = new AnimationDetails();
        details.sequenceName = (String) expressionAnimations[expressionIndex].x;
        details.listener = new AnonymousClass_2(listener);
        getHeadAnimation().playAnimation(details);
    }
}

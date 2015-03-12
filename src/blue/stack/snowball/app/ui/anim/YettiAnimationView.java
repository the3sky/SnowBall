package blue.stack.snowball.app.ui.anim;

import blue.stack.snowball.app.R;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import blue.stack.snowball.app.ui.anim.AnimationPNGSequence.IAnimationListener;

import com.android.volley.DefaultRetryPolicy;

public class YettiAnimationView extends RelativeLayout {

    class AnonymousClass_1 implements IAnimationListener {
        final /* synthetic */ AnimationPNGSequence val$animationPNGSequence;
        final /* synthetic */ AnimationDetails val$details;
        final /* synthetic */ IAnimationListener val$incomingListener;

        AnonymousClass_1(AnimationDetails animationDetails, AnimationPNGSequence animationPNGSequence, IAnimationListener iAnimationListener) {
            this.val$details = animationDetails;
            this.val$animationPNGSequence = animationPNGSequence;
            this.val$incomingListener = iAnimationListener;
        }

        public void onAnimationUpdated(int frame) {
            if (this.val$details.landingFrame != 0) {
                if (frame >= this.val$details.landingFrame) {
                    YettiAnimationView.this.setTranslationX(0.0f);
                    YettiAnimationView.this.setTranslationY(0.0f);
                } else {
                    float percentage = ((float) frame) / ((float) this.val$details.landingFrame);
                    if (this.val$details.translationX != 0.0f) {
                        YettiAnimationView.this.setTranslationX(((float) this.val$animationPNGSequence.getWidth()) * (this.val$details.translationX * (DefaultRetryPolicy.DEFAULT_BACKOFF_MULT - percentage)));
                    }
                    if (this.val$details.translationY != 0.0f) {
                        YettiAnimationView.this.setTranslationY(((float) this.val$animationPNGSequence.getWidth()) * (this.val$details.translationY * (DefaultRetryPolicy.DEFAULT_BACKOFF_MULT - percentage)));
                    }
                }
            }
            if (this.val$incomingListener != null) {
                this.val$incomingListener.onAnimationUpdated(frame);
            }
        }

        public void onAnimationFinished() {
            if (this.val$incomingListener != null) {
                this.val$incomingListener.onAnimationFinished();
            }
        }
    }

    public static class AnimationDetails extends blue.stack.snowball.app.ui.anim.AnimationPNGSequence.AnimationDetails {
        public int landingFrame;
        public float translationX;
        public float translationY;

        public AnimationDetails() {
            this.translationX = 0.0f;
            this.translationY = 0.0f;
        }
    }

    public YettiAnimationView(Context context) {
        super(context);
    }

    public YettiAnimationView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public YettiAnimationView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public static AnimationDetails getAnimationDetails(String name) {
        AnimationDetails details = new AnimationDetails();
        details.sequenceName = name;
        if (name.equals("YETTI_WALK")) {
            details.translationX = -1.0f;
            details.landingFrame = 22;
        }
        return details;
    }

    public void playAnimation(AnimationDetails details) {
        AnimationPNGSequence animationPNGSequence = (AnimationPNGSequence) findViewById(R.id.yetti_animation);
        if (!(details.landingFrame == 0 || details.translationX == 0.0f)) {
            setTranslationX(((float) animationPNGSequence.getWidth()) * details.translationX);
        }
        details.listener = new AnonymousClass_1(details, animationPNGSequence, details.listener);
        animationPNGSequence.playAnimation((blue.stack.snowball.app.ui.anim.AnimationPNGSequence.AnimationDetails) details);
    }

    public static void cacheAnimations(AnimationCache animationCache) {
        animationCache.getAnimation("YETTI_WALK");
        animationCache.getAnimation("YETTI_WAVE");
    }
}

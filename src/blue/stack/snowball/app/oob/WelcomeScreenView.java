package blue.stack.snowball.app.oob;

import java.util.Random;

import org.apache.http.HttpStatus;

import blue.stack.snowball.app.R;
import android.animation.TimeAnimator;
import android.content.Context;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import blue.stack.snowball.app.ui.anim.GeometryUtils;
import blue.stack.snowball.app.ui.anim.SpringTranslateTimeListener;

import com.android.volley.DefaultRetryPolicy;

public class WelcomeScreenView extends RelativeLayout {
    Boolean launched;

    class AnonymousClass_1 implements AnimationListener {
        final /* synthetic */ ImageView val$bubble;

        AnonymousClass_1(ImageView imageView) {
            this.val$bubble = imageView;
        }

        public void onAnimationStart(Animation animation) {
            this.val$bubble.setAlpha(DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        }

        public void onAnimationEnd(Animation animation) {
        }

        public void onAnimationRepeat(Animation animation) {
        }
    }

    public WelcomeScreenView(Context context) {
        super(context);
        this.launched = Boolean.valueOf(false);
    }

    public WelcomeScreenView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.launched = Boolean.valueOf(false);
    }

    public WelcomeScreenView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.launched = Boolean.valueOf(false);
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        ViewGroup bubbleViews = (ViewGroup) findViewById(R.id.oob_bubbles_list);
        View yettiAnimation = findViewById(R.id.yetti_animation);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        if (!isInEditMode()) {
            ViewGroup bubbleViews = (ViewGroup) findViewById(R.id.oob_bubbles_list);
            for (int childIndex = 0; childIndex < bubbleViews.getChildCount(); childIndex++) {
                ((ImageView) bubbleViews.getChildAt(childIndex)).setAlpha(0.0f);
            }
        }
    }

    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if (!isInEditMode() && !this.launched.booleanValue()) {
            this.launched = Boolean.valueOf(true);
            ViewGroup bubbleViews = (ViewGroup) findViewById(R.id.oob_bubbles_list);
            View yettiAnimation = findViewById(R.id.yetti_animation);
            Random random = new Random();
            for (int childIndex = 0; childIndex < bubbleViews.getChildCount(); childIndex++) {
                ImageView bubble = (ImageView) bubbleViews.getChildAt(childIndex);
                PointF start = new PointF(((((float) (-bubble.getPaddingLeft())) - bubble.getX()) + yettiAnimation.getX()) + ((float) (yettiAnimation.getWidth() / 2)), ((((float) (-bubble.getPaddingTop())) - bubble.getY()) + yettiAnimation.getY()) + ((float) (yettiAnimation.getHeight() / 2)));
                PointF endPoint = new PointF(bubble.getX(), bubble.getY());
                SpringTranslateTimeListener springTranslate = new SpringTranslateTimeListener(bubble, start, GeometryUtils.scalePoint(GeometryUtils.normalizePoint(new PointF(endPoint.y - start.y, -(endPoint.x - start.x))), 3.0f), endPoint);
                springTranslate.setMassScale(2.0f);
                TimeAnimator timeAnimator = new TimeAnimator();
                timeAnimator.setTimeListener(springTranslate);
                timeAnimator.setStartDelay((long) (random.nextInt(HttpStatus.SC_BAD_REQUEST) + 1200));
                timeAnimator.start();
                springTranslate.setAnimationListener(new AnonymousClass_1(bubble));
            }
        }
    }
}

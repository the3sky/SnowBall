package blue.stack.snowball.app.oob;

import blue.stack.snowball.app.R;
import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.view.animation.BounceInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class SplashScreenView extends RelativeLayout {
    public SplashScreenView(Context context) {
        super(context);
    }

    public SplashScreenView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SplashScreenView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        if (!isInEditMode()) {
            ImageView snowballText = (ImageView) findViewById(R.id.snowball_text);
            TranslateAnimation translateAnimation = new TranslateAnimation(1, 2.0f, 1, 0.0f, 0, 0.0f, 0, 0.0f);
            translateAnimation.setDuration(1500);
            translateAnimation.setStartOffset(2500);
            translateAnimation.setInterpolator(new BounceInterpolator());
            snowballText.startAnimation(translateAnimation);
            animate().setStartDelay(5500).alpha(0.0f).setDuration(400).withEndAction(new Runnable() {
                public void run() {
                    ((ViewGroup) SplashScreenView.this.getParent()).removeView(SplashScreenView.this);
                }
            });
        }
    }
}

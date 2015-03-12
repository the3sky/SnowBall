package blue.stack.snowball.app.ui.anim;

import android.view.animation.Interpolator;

import com.android.volley.DefaultRetryPolicy;

public class BackOutInterpolator implements Interpolator {
    public static final float MAX_OVERSHOOT = 1.1000041f;
    private static final float OVERSHOOT = 1.70158f;

    public float getInterpolation(float t) {
        t -= DefaultRetryPolicy.DEFAULT_BACKOFF_MULT;
        return ((t * t) * ((2.70158f * t) + OVERSHOOT)) + DefaultRetryPolicy.DEFAULT_BACKOFF_MULT;
    }
}

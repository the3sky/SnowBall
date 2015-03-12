package blue.stack.snowball.app.ui.anim;

import android.view.animation.Interpolator;

import com.android.volley.DefaultRetryPolicy;

public class ElasticInInterpolator implements Interpolator {
    float _amplitude;
    float _period;

    public ElasticInInterpolator(float period) {
        this._period = 0.5f;
        this._amplitude = DefaultRetryPolicy.DEFAULT_BACKOFF_MULT;
        this._period = period;
    }

    public ElasticInInterpolator(float period, float amplitude) {
        this._period = 0.5f;
        this._amplitude = DefaultRetryPolicy.DEFAULT_BACKOFF_MULT;
        this._period = period;
        this._amplitude = amplitude;
    }

    public float getInterpolation(float t) {
        if (t == 0.0f) {
            return 0.0f;
        }
        if (t >= DefaultRetryPolicy.DEFAULT_BACKOFF_MULT) {
            return DefaultRetryPolicy.DEFAULT_BACKOFF_MULT;
        }
        float s;
        if (this._period == 0.0f) {
            this._period = 0.3f;
        }
        if (this._amplitude == 0.0f || this._amplitude < DefaultRetryPolicy.DEFAULT_BACKOFF_MULT) {
            this._amplitude = DefaultRetryPolicy.DEFAULT_BACKOFF_MULT;
            s = this._period / 4.0f;
        } else {
            s = (float) ((((double) this._period) / 6.283185307179586d) * Math.asin((double) (DefaultRetryPolicy.DEFAULT_BACKOFF_MULT / this._amplitude)));
        }
        t -= DefaultRetryPolicy.DEFAULT_BACKOFF_MULT;
        return (float) (-((Math.pow(2.0d, (double) (10.0f * t)) * ((double) this._amplitude)) * Math.sin((((double) (t - s)) * 6.283185307179586d) / ((double) this._period))));
    }
}

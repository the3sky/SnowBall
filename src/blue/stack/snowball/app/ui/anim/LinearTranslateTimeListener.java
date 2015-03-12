package blue.stack.snowball.app.ui.anim;

import android.animation.TimeAnimator;
import android.animation.TimeAnimator.TimeListener;
import android.graphics.PointF;
import android.view.View;
import android.view.animation.Animation.AnimationListener;

public class LinearTranslateTimeListener implements TimeListener {
    AnimationListener mAnimationListener;
    float mDuration;
    PointF mStart;
    PointF mTarget;
    View mView;

    public LinearTranslateTimeListener(View view, PointF start, PointF target, float duration) {
        this.mStart = start;
        this.mTarget = target;
        this.mView = view;
        this.mDuration = duration;
    }

    public void onTimeUpdate(TimeAnimator timeAnimator, long totalTime, long deltaTime) {
        if (!timeAnimator.isRunning()) {
            return;
        }
        if (((float) totalTime) > this.mDuration) {
            timeAnimator.cancel();
            if (this.mAnimationListener != null) {
                this.mAnimationListener.onAnimationEnd(null);
                return;
            }
            return;
        }
        step((float) totalTime);
    }

    public void setTarget(PointF target) {
        this.mTarget = target;
    }

    private void step(float elapsedTime) {
        PointF current = new PointF();
        float percentage = elapsedTime / this.mDuration;
        float y = ((this.mTarget.y - this.mStart.y) * percentage) + this.mStart.y;
        this.mView.setX(((this.mTarget.x - this.mStart.x) * percentage) + this.mStart.x);
        this.mView.setY(y);
    }

    public void setAnimationListener(AnimationListener animationListener) {
        this.mAnimationListener = animationListener;
    }
}

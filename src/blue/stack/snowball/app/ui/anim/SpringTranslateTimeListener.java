package blue.stack.snowball.app.ui.anim;

import android.animation.TimeAnimator;
import android.animation.TimeAnimator.TimeListener;
import android.graphics.PointF;
import android.view.View;
import android.view.animation.Animation.AnimationListener;

public class SpringTranslateTimeListener implements TimeListener {
    private static final float CLOSE_DEADSPOT_SIZE = 2.0f;
    private static final float DEADSPOT_VELOCITY = 0.6f;
    static final float DYNAMIC_DAMPING = 0.1f;
    private static final float FAR_DEADSPOT_SIZE = 4.0f;
    static final float FINAL_LERP_SPEED_FACTOR = 3.0f;
    static final float MASS = 0.4f;
    static final float MAX_DAMPING = 0.1f;
    private static final float MINIMUM_STEP = 1.0f;
    static final float MIN_DAMPING = 0.07f;
    static final float STIFFNESS = 0.01f;
    private AnimationListener mAnimationListener;
    float mMass;
    PointF mPosition;
    Boolean mStarted;
    PointF mTarget;
    PointF mVelocity;
    View mView;

    public SpringTranslateTimeListener(View view, PointF start, PointF velocity, PointF target) {
        this.mMass = MASS;
        this.mStarted = Boolean.valueOf(false);
        this.mAnimationListener = null;
        this.mView = view;
        this.mPosition = start;
        this.mVelocity = velocity;
        this.mTarget = target;
    }

    public void setMassScale(float massScale) {
        this.mMass *= massScale;
    }

    private Boolean isReallyClose() {
        return (Math.abs(this.mPosition.x - this.mTarget.x) >= FAR_DEADSPOT_SIZE || Math.abs(this.mPosition.y - this.mTarget.y) >= FAR_DEADSPOT_SIZE || Math.abs(this.mVelocity.x) >= DEADSPOT_VELOCITY || Math.abs(this.mVelocity.y) >= DEADSPOT_VELOCITY) ? Boolean.FALSE : Boolean.TRUE;
    }

    public Boolean isFinished() {
        return (Math.abs(this.mPosition.x - this.mTarget.x) >= CLOSE_DEADSPOT_SIZE || Math.abs(this.mPosition.y - this.mTarget.y) >= CLOSE_DEADSPOT_SIZE) ? Boolean.FALSE : Boolean.TRUE;
    }

    public void onTimeUpdate(TimeAnimator timeAnimator, long totalTime, long deltaTime) {
        if (!this.mStarted.booleanValue()) {
            this.mStarted = Boolean.valueOf(true);
            if (this.mAnimationListener != null) {
                this.mAnimationListener.onAnimationStart(null);
            }
        }
        if (timeAnimator.isRunning()) {
            if (isReallyClose().booleanValue()) {
                stepFast();
            } else {
                step();
            }
            if (isFinished().booleanValue() && this.mAnimationListener != null) {
                this.mAnimationListener.onAnimationEnd(null);
            }
        }
    }

    private void stepFast() {
        if (Math.abs(this.mTarget.y - this.mPosition.y) < MINIMUM_STEP) {
            this.mPosition.y = this.mTarget.y;
        } else {
            float deltaY = (this.mTarget.y - this.mPosition.y) / FINAL_LERP_SPEED_FACTOR;
            if (Math.abs(deltaY) < MINIMUM_STEP) {
                deltaY = deltaY < 0.0f ? -1.0f : MINIMUM_STEP;
            }
            this.mPosition.y += deltaY;
        }
        if (Math.abs(this.mTarget.x - this.mPosition.x) < MINIMUM_STEP) {
            this.mPosition.x = this.mTarget.x;
        } else {
            float deltaX = (this.mTarget.x - this.mPosition.x) / FINAL_LERP_SPEED_FACTOR;
            if (Math.abs(deltaX) < MINIMUM_STEP) {
                deltaX = deltaX < 0.0f ? -1.0f : MINIMUM_STEP;
            }
            this.mPosition.x += deltaX;
        }
        this.mView.setX(this.mPosition.x);
        this.mView.setY(this.mPosition.y);
    }

    private void step() {
        float dXv = (((this.mPosition.x - this.mTarget.x) * -0.01f) - (this.mVelocity.x * (((MINIMUM_STEP / ((Math.abs(this.mPosition.x - this.mTarget.x) * MAX_DAMPING) + MINIMUM_STEP)) * MAX_DAMPING) + MIN_DAMPING))) / this.mMass;
        this.mVelocity.x += dXv;
        this.mPosition.x += this.mVelocity.x;
        float dYv = (((this.mPosition.y - this.mTarget.y) * -0.01f) - (this.mVelocity.y * (((MINIMUM_STEP / ((Math.abs(this.mPosition.y - this.mTarget.y) * MAX_DAMPING) + MINIMUM_STEP)) * MAX_DAMPING) + MIN_DAMPING))) / this.mMass;
        this.mVelocity.y += dYv;
        this.mPosition.y += this.mVelocity.y;
        this.mView.setX(this.mPosition.x);
        this.mView.setY(this.mPosition.y);
    }

    public void setAnimationListener(AnimationListener animationListener) {
        this.mAnimationListener = animationListener;
    }
}

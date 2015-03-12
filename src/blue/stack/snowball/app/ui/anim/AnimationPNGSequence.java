package blue.stack.snowball.app.ui.anim;

import java.util.List;

import blue.stack.snowball.app.R;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;
import blue.stack.snowball.app.core.GuiceModule;

public class AnimationPNGSequence extends ImageView {
    public static int NO_LOOP_POINT;
    private AnimationDrawablePNG animationDrawable;

    public static class AnimationDetails {
        public long delay;
        public IAnimationListener listener;
        public int loopCount;
        public int loopEnd;
        public int loopStart;
        public Boolean oneShot;
        public String sequenceName;

        public AnimationDetails() {
            this.oneShot = Boolean.valueOf(true);
            this.delay = 0;
            this.loopStart = NO_LOOP_POINT;
            this.loopEnd = NO_LOOP_POINT;
            this.loopCount = 0;
        }
    }

    private class AnimationDrawablePNG extends AnimationDrawable {
        private boolean addingFrames;
        private IAnimationListener animationListener;
        private boolean finished;

        private AnimationDrawablePNG() {
            this.finished = false;
            this.addingFrames = false;
        }

        public IAnimationListener getAnimationListener() {
            return this.animationListener;
        }

        public void setAnimationListener(IAnimationListener animationListener) {
            this.animationListener = animationListener;
        }

        public boolean selectDrawable(int idx) {
            boolean ret = super.selectDrawable(idx);
            if (!this.addingFrames) {
                if (this.animationListener != null) {
                    this.animationListener.onAnimationUpdated(idx);
                }
                if (((getNumberOfFrames() == 1 && idx == 0) || (idx != 0 && idx >= getNumberOfFrames() - 1)) && !this.finished) {
                    this.finished = true;
                    if (this.animationListener != null) {
                        this.animationListener.onAnimationFinished();
                    }
                }
            }
            return ret;
        }

        public void unscheduleSelf(Runnable what) {
            super.unscheduleSelf(what);
        }

        public void beginAddingFrames() {
            this.addingFrames = true;
        }

        public void stopAddingFrames() {
            this.addingFrames = false;
        }
    }

    public static interface IAnimationListener {
        void onAnimationFinished();

        void onAnimationUpdated(int i);
    }

    static {
        NO_LOOP_POINT = -1;
    }

    public AnimationPNGSequence(Context context) {
        super(context);
    }

    public AnimationPNGSequence(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode()) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AnimationPNGSequence);
            AnimationDetails details = new AnimationDetails();
            CharSequence s = a.getString(0);
            details.sequenceName = (String) s;
            details.oneShot = Boolean.valueOf(a.getBoolean(1, details.oneShot.booleanValue()));
            details.delay = (long) a.getInt(2, (int) details.delay);
            details.loopCount = a.getInt(5, details.loopCount);
            details.loopEnd = a.getInt(4, details.loopEnd);
            details.loopStart = a.getInt(3, details.loopStart);
            if (s != null) {
                playAnimation(details);
            }
            a.recycle();
        }
    }

    public AnimationPNGSequence(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void stop() {
        if (this.animationDrawable != null) {
            this.animationDrawable.stop();
        }
    }

    public void playAnimation(String sequenceName) {
        AnimationDetails details = new AnimationDetails();
        details.sequenceName = sequenceName;
        playAnimation(details);
    }

    public void playAnimation(AnimationDetails details) {
        if (this.animationDrawable != null) {
            this.animationDrawable.stop();
        }
        this.animationDrawable = new AnimationDrawablePNG();
        this.animationDrawable.setAnimationListener(details.listener);
        this.animationDrawable.stop();
        this.animationDrawable.beginAddingFrames();
        try {
            List<Drawable> images = ((AnimationCache) GuiceModule.get().getInstance(AnimationCache.class)).getAnimation(details.sequenceName);
            boolean firstFrame = true;
            int loopCount = 0;
            int frameIdx = 0;
            while (frameIdx < images.size()) {
                Drawable image = (Drawable) images.get(frameIdx);
                int duration = 50;
                if (firstFrame && details.delay != 0) {
                    duration = (int) (((long) 50) + details.delay);
                    firstFrame = false;
                }
                this.animationDrawable.addFrame(image, duration);
                frameIdx++;
                if (details.loopEnd != NO_LOOP_POINT && details.loopEnd < frameIdx) {
                    loopCount++;
                    if (details.loopCount + 1 > loopCount) {
                        frameIdx = details.loopStart;
                    }
                }
            }
        } catch (Exception e) {
        }
        setImageDrawable(this.animationDrawable);
        this.animationDrawable.setOneShot(details.oneShot.booleanValue());
        this.animationDrawable.stopAddingFrames();
        this.animationDrawable.start();
    }
}

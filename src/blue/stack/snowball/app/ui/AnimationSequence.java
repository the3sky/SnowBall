package blue.stack.snowball.app.ui;

import java.util.ArrayList;
import java.util.List;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;

public class AnimationSequence {
	int currentStep;
	boolean repeat;
	List<Step> steps;
	View view;

	class AnonymousClass_1 implements AnimationListener {
		final/* synthetic */Step val$step;

		AnonymousClass_1(Step step) {
			this.val$step = step;
		}

		@Override
		public void onAnimationStart(Animation animation) {
		}

		@Override
		public void onAnimationEnd(Animation animation) {
			Runnable endAction = this.val$step.getEndAction();
			if (endAction != null) {
				endAction.run();
			}
			if (!AnimationSequence.this.runNextStep() && AnimationSequence.this.repeat) {
				AnimationSequence.this.start();
			}
		}

		@Override
		public void onAnimationRepeat(Animation animation) {
		}
	}

	public class Step {
		Animation animation;
		Runnable endAction;
		Runnable startAction;

		// public Step(AnimationSequence animationSequence, Animation animation)
		// {
		// this(null, animation, (Runnable) null);
		// }

		public Step(AnimationSequence animationSequence, Runnable startAction, Animation animation) {
			this(startAction, animation, null);
		}

		// public Step(AnimationSequence animationSequence, Animation animation,
		// Runnable endAction) {
		// this(null, animation, endAction);
		// }

		public Step(Runnable startAction, Animation animation, Runnable endAction) {
			this.startAction = startAction;
			this.animation = animation;
			this.endAction = endAction;
		}

		public Runnable getStartAction() {
			return this.startAction;
		}

		public Animation getAnimation() {
			return this.animation;
		}

		public Runnable getEndAction() {
			return this.endAction;
		}
	}

	public AnimationSequence() {
		this.view = null;
		this.repeat = false;
		this.steps = new ArrayList();
	}

	public void addAnimation(Animation animation) {
		addAnimation(null, animation, null);
	}

	public void addAnimation(Runnable startAction, Animation animation) {
		addAnimation(startAction, animation, null);
	}

	public void addAnimation(Animation animation, Runnable endAction) {
		addAnimation(null, animation, endAction);
	}

	public void addAnimation(Runnable startAction, Animation animation, Runnable endAction) {
		this.steps.add(new Step(startAction, animation, endAction));
	}

	public void setView(View view) {
		this.view = view;
	}

	public View getView() {
		return this.view;
	}

	public boolean getRepeat() {
		return this.repeat;
	}

	public void setRepeat(boolean repeat) {
		this.repeat = repeat;
	}

	public void start() {
		this.currentStep = 0;
		runNextStep();
	}

	public void clear() {
		for (Step step : this.steps) {
			step.getAnimation().setAnimationListener(null);
		}
		this.steps.clear();
		this.view.clearAnimation();
	}

	Step nextStep() {
		if (this.currentStep < this.steps.size()) {
			Step nextStep = this.steps.get(this.currentStep);
			this.currentStep++;
			return nextStep;
		}
		this.currentStep = 0;
		return null;
	}

	boolean runNextStep() {
		Step step = nextStep();
		if (step == null) {
			return false;
		}
		step.getAnimation().setAnimationListener(new AnonymousClass_1(step));
		Runnable startAction = step.getStartAction();
		if (startAction != null) {
			startAction.run();
		}
		this.view.startAnimation(step.getAnimation());
		return true;
	}
}

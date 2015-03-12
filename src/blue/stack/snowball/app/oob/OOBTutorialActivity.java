package blue.stack.snowball.app.oob;

import blue.stack.snowball.app.R;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewPropertyAnimator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.TextView;
import blue.stack.snowball.app.core.GuiceModule;
import blue.stack.snowball.app.logging.EventLogger;
import blue.stack.snowball.app.logging.EventLoggerManager;
import blue.stack.snowball.app.ui.AnimationSequence;

import com.android.volley.DefaultRetryPolicy;
import com.google.inject.Inject;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class OOBTutorialActivity extends Activity implements OOBListener {
	public static final int FADE_IN_TIME = 500;
	public static final int FADE_OUT_TIME = 250;
	public static final int PULSE_FADE_TIME = 300;
	public static final int PULSE_RESTART_PAUSE_TIME = 4000;
	public static final int READ_DELAY = 3000;
	public static final int SWIPE_DURATION = 500;
	private static final String TAG = "OOBTutorial";
	public static final int TEXT_TRANSITION_DELAY = 200;
	public static final int TRANSITION_DELAY = 1000;
	View dim;
	float dimValue;
	@Inject
	EventLoggerManager eventLoggerManager;
	TextView explanation;
	View headsup;
	View next;
	View notificationAccess;
	View notificationAccessTapIndicatior;
	@Inject
	OOBManager oobManager;
	View phone;
	View shade;
	int shadeHeight;
	View shadePullTarget;
	View shadePullTargetContainer;
	View shadePullTouchTarget;
	boolean started;

	class AnonymousClass_2 implements OnTouchListener {
		float currentShadeHeight;
		boolean opened;
		boolean touchDown;
		final/* synthetic */AnimationSequence val$sequence;
		final/* synthetic */int val$shadePullHeightTarget;
		float y;

		AnonymousClass_2(int i, AnimationSequence animationSequence) {
			this.val$shadePullHeightTarget = i;
			this.val$sequence = animationSequence;
			this.touchDown = false;
			this.opened = false;
			this.y = 0.0f;
			this.currentShadeHeight = 0.0f;
		}

		@Override
		public boolean onTouch(View view, MotionEvent motionEvent) {
			switch (motionEvent.getAction()) {
			case 0:// SyslogAppender.LOG_KERN /*0*/:
				this.y = motionEvent.getY();
				this.currentShadeHeight = 0.0f;
				this.touchDown = true;
				OOBTutorialActivity.this.shadePullTargetContainer.setVisibility(4);
				OOBTutorialActivity.this.updateText(OOBTutorialActivity.this.explanation,
						R.string.oob_pullout_shade_keep_pulling, 0, null);
				break;
			case 1:// DragSortController.ON_DRAG /*1*/:
			case 3:// Textifier.METHOD_DESCRIPTOR /*3*/:
				if (this.touchDown && !this.opened) {
					OOBTutorialActivity.this.shadePullTargetContainer.setVisibility(0);
					OOBTutorialActivity.this.animateShadeClose();
				}
				this.touchDown = false;
				break;
			case 2:// DragSortController.ON_LONG_PRESS /*2*/:
				if (this.touchDown && !this.opened) {
					this.currentShadeHeight += motionEvent.getY() - this.y;
					this.y = motionEvent.getY();
					if (this.currentShadeHeight >= 0.0f) {
						if (this.currentShadeHeight < (this.val$shadePullHeightTarget)) {
							OOBTutorialActivity.this.updateShadeHeight(this.currentShadeHeight);
							break;
						}
						OOBTutorialActivity.this.animateShadeOpen();
						OOBTutorialActivity.this.stopPullShadeAnimation(this.val$sequence);
						this.opened = true;
						OOBTutorialActivity.this.shadePullTouchTarget.setOnClickListener(null);
						OOBTutorialActivity.this.shadePullTouchTarget.setVisibility(8);
						OOBTutorialActivity.this.startTutorialStep2();
						break;
					}
					OOBTutorialActivity.this.updateShadeHeight(0.0f);
					break;
				}
			}
			return true;
		}
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	class AnonymousClass_3 implements AnimatorUpdateListener {
		final/* synthetic */float val$dimIncrement;

		AnonymousClass_3(float f) {
			this.val$dimIncrement = f;
		}

		@Override
		public void onAnimationUpdate(ValueAnimator animation) {
			Integer height = (Integer) animation.getAnimatedValue();
			LayoutParams layoutParams = OOBTutorialActivity.this.shade.getLayoutParams();
			layoutParams.height = height.intValue();
			OOBTutorialActivity.this.shade.setLayoutParams(layoutParams);
			OOBTutorialActivity.this.dim.setAlpha(height.floatValue() * this.val$dimIncrement);
			OOBTutorialActivity.this.shade.requestLayout();
		}
	}

	class AnonymousClass_4 implements AnimatorUpdateListener {
		final/* synthetic */float val$dimIncrement;

		AnonymousClass_4(float f) {
			this.val$dimIncrement = f;
		}

		@Override
		public void onAnimationUpdate(ValueAnimator animation) {
			Integer height = (Integer) animation.getAnimatedValue();
			LayoutParams layoutParams = OOBTutorialActivity.this.shade.getLayoutParams();
			layoutParams.height = height.intValue();
			OOBTutorialActivity.this.shade.setLayoutParams(layoutParams);
			OOBTutorialActivity.this.dim.setAlpha(height.floatValue() * this.val$dimIncrement);
			OOBTutorialActivity.this.shade.requestLayout();
		}
	}

	class AnonymousClass_5 implements Runnable {
		final/* synthetic */Runnable val$endAction;
		final/* synthetic */TextView val$textView;
		final/* synthetic */int val$updatedTextResId;

		AnonymousClass_5(TextView textView, int i, Runnable runnable) {
			this.val$textView = textView;
			this.val$updatedTextResId = i;
			this.val$endAction = runnable;
		}

		@Override
		public void run() {
			this.val$textView.setText(this.val$updatedTextResId);
			ViewPropertyAnimator animator2 = this.val$textView.animate().alpha(DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
					.setStartDelay(200).setDuration(500);
			if (this.val$endAction != null) {
				animator2.withEndAction(this.val$endAction);
			}
		}
	}

	class AnonymousClass_6 implements Runnable {
		final/* synthetic */View val$view;

		AnonymousClass_6(View view) {
			this.val$view = view;
		}

		@Override
		public void run() {
			this.val$view.setTranslationY(100.0f);
		}
	}

	class AnonymousClass_7 implements Runnable {
		final/* synthetic */View val$view;

		AnonymousClass_7(View view) {
			this.val$view = view;
		}

		@Override
		public void run() {
			this.val$view.setTranslationY(0.0f);
		}
	}

	class AnonymousClass_8 implements Runnable {
		final/* synthetic */View val$view;

		AnonymousClass_8(View view) {
			this.val$view = view;
		}

		@Override
		public void run() {
			this.val$view.setVisibility(8);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_oob_tutorial);
		GuiceModule.get().injectMembers(this);
		getWindow().addFlags(128);
		this.explanation = (TextView) findViewById(R.id.oob_explanation);
		this.phone = findViewById(R.id.oob_phone);
		this.shade = findViewById(R.id.oob_shade);
		this.headsup = findViewById(R.id.oob_headsup);
		this.dim = findViewById(R.id.oob_phone_dim);
		this.shadePullTarget = findViewById(R.id.oob_shade_pull_target);
		this.shadePullTargetContainer = findViewById(R.id.oob_shade_pull_target_container);
		this.shadePullTouchTarget = findViewById(R.id.shade_pull_touch_target);
		this.next = findViewById(R.id.oob_next);
		this.notificationAccess = findViewById(R.id.oob_notification_access);
		this.notificationAccessTapIndicatior = findViewById(R.id.oob_notification_access_tap_indicator);
		OnClickListener notificationAccessClickListener = new OnClickListener() {
			@Override
			public void onClick(View view) {
				OOBTutorialActivity.this.eventLoggerManager.getEventLogger().addEvent(
						EventLogger.OOB_TUTORIAL_SET_NOTIFICATION_ACCESS_PRESSED);
				Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
				intent.addFlags(1073741824);
				OOBTutorialActivity.this.startActivity(intent);
			}
		};
		this.next.setOnClickListener(notificationAccessClickListener);
		this.notificationAccessTapIndicatior.setOnClickListener(notificationAccessClickListener);
		setInitialState();
		this.oobManager.addListener(this);
		if (this.oobManager.isOOBTutorialComplete()) {
			startTurnOnNotificationAccess();
		} else {
			startTutorial();
		}
	}

	@Override
	protected void onDestroy() {
		this.oobManager.removeListener(this);
		super.onDestroy();
	}

	@Override
	public void onOOBComplete() {
		finish();
	}

	void setInitialState() {
		this.explanation.clearAnimation();
		this.explanation.setText(R.string.oob_tutorial_explanation);
		this.explanation.setAlpha(0.0f);
		this.phone.clearAnimation();
		this.phone.setAlpha(0.0f);
		this.shade.clearAnimation();
		LayoutParams layoutParams = this.shade.getLayoutParams();
		layoutParams.height = -2;
		this.shade.setLayoutParams(layoutParams);
		this.shade.setVisibility(4);
		this.dimValue = this.dim.getAlpha();
		this.dim.clearAnimation();
		this.dim.setVisibility(8);
		this.headsup.clearAnimation();
		this.headsup.setTranslationY(0.0f);
		this.headsup.setVisibility(8);
		this.headsup.setOnClickListener(null);
		this.headsup.setOnTouchListener(null);
		this.shadePullTouchTarget.setVisibility(8);
		this.shadePullTargetContainer.setVisibility(0);
		this.shadePullTarget.clearAnimation();
		this.shadePullTarget.setAlpha(0.0f);
		this.shadePullTarget.setTranslationX(0.0f);
		this.shadePullTarget.setVisibility(8);
		this.next.setAlpha(0.0f);
		this.next.setVisibility(4);
		this.notificationAccess.setAlpha(0.0f);
		this.notificationAccessTapIndicatior.setAlpha(0.0f);
		this.notificationAccessTapIndicatior.setVisibility(8);
	}

	void enableShadePullHandler() {
		LayoutParams layoutParams = this.shade.getLayoutParams();
		layoutParams.height = 0;
		this.shade.setLayoutParams(layoutParams);
		this.shade.setVisibility(0);
		this.dim.setAlpha(0.0f);
		this.dim.setVisibility(0);
		this.shadePullTouchTarget.setVisibility(0);
		int shadePullHeightTarget = (this.shadeHeight * 2) / 3;
		this.shadePullTouchTarget.setOnTouchListener(new AnonymousClass_2(shadePullHeightTarget,
				runPullShadeAnimation(this.shadePullTarget)));
	}

	void updateShadeHeight(float height) {
		float alpha = height * (this.dimValue / (this.shadeHeight));
		LayoutParams layoutParams = this.shade.getLayoutParams();
		layoutParams.height = (int) height;
		this.shade.setLayoutParams(layoutParams);
		this.dim.setAlpha(alpha);
	}

	void animateShadeOpen() {
		this.phone.performHapticFeedback(3, 2);
		updateText(this.explanation, R.string.oob_pullout_shade_all_the_way, 0, null);
		float dimIncrement = this.dimValue / (this.shadeHeight);
		float currentHeight = this.shade.getHeight();
		ValueAnimator va = ValueAnimator.ofInt(new int[] { (int) currentHeight, this.shadeHeight });
		va.addUpdateListener(new AnonymousClass_3(dimIncrement));
		va.start();
	}

	void animateShadeClose() {
		this.phone.performHapticFeedback(3, 2);
		updateText(this.explanation, R.string.oob_pullout_shade_all_the_way, 0, null);
		float dimIncrement = this.dimValue / (this.shadeHeight);
		float currentHeight = this.shade.getHeight();
		ValueAnimator va = ValueAnimator.ofInt(new int[] { (int) currentHeight, 0 });
		va.addUpdateListener(new AnonymousClass_4(dimIncrement));
		va.start();
	}

	void updateText(TextView textView, int updatedTextResId, int startDelay, Runnable endAction) {
		ViewPropertyAnimator animator = textView.animate().alpha(0.0f).setDuration(250).setStartDelay(startDelay)
				.withEndAction(new AnonymousClass_5(textView, updatedTextResId, endAction));
	}

	AnimationSequence runPullShadeAnimation(View view) {
		view.setVisibility(0);
		view.setAlpha(DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
		view.setTranslationY(0.0f);
		AnimationSequence sequence = new AnimationSequence();
		sequence.setView(view);
		sequence.setRepeat(true);
		AlphaAnimation fadeIn = new AlphaAnimation(0.0f, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
		fadeIn.setDuration(300);
		sequence.addAnimation(fadeIn);
		TranslateAnimation swipe = new TranslateAnimation(0.0f, 0.0f, 0.0f, 100.0f);
		swipe.setDuration(500);
		sequence.addAnimation(swipe);
		Animation fadeOut = new AlphaAnimation(DefaultRetryPolicy.DEFAULT_BACKOFF_MULT, 0.0f);
		fadeOut.setDuration(300);
		sequence.addAnimation(new AnonymousClass_6(view), fadeOut);
		Animation pause = new AlphaAnimation(0.0f, 0.0f);
		pause.setDuration(4000);
		sequence.addAnimation(pause, new AnonymousClass_7(view));
		sequence.start();
		return sequence;
	}

	void stopPullShadeAnimation(AnimationSequence as) {
		View view = as.getView();
		as.clear();
		view.animate().alpha(0.0f).setDuration(300).withEndAction(new AnonymousClass_8(view));
	}

	void startTutorial() {
		this.eventLoggerManager.getEventLogger().addEvent(EventLogger.OOB_TUTORIAL_STARTED);
		this.phone.animate().alpha(DefaultRetryPolicy.DEFAULT_BACKOFF_MULT).setDuration(500).setStartDelay(0)
				.withEndAction(new Runnable() {
					@Override
					public void run() {
						OOBTutorialActivity.this.shadeHeight = OOBTutorialActivity.this.shade.getHeight();
						OOBTutorialActivity.this.shade.setVisibility(8);
						OOBTutorialActivity.this.explanation.animate().alpha(DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
								.setDuration(500).setStartDelay(0).withEndAction(new Runnable() {
									@Override
									public void run() {
										OOBTutorialActivity.this.updateText(OOBTutorialActivity.this.explanation,
												R.string.oob_popout_tab_explanation, READ_DELAY, new Runnable() {

													class AnonymousClass_1 implements Runnable {
														final/* synthetic */float val$translateY;

														AnonymousClass_1(float f) {
															this.val$translateY = f;
														}

														@Override
														public void run() {
															OOBTutorialActivity.this.headsup.animate()
																	.translationY(this.val$translateY).setDuration(500)
																	.setStartDelay(3000).withEndAction(new Runnable() {
																		@Override
																		public void run() {
																			OOBTutorialActivity.this.headsup
																					.setTranslationY(0.0f);
																			OOBTutorialActivity.this.headsup
																					.setVisibility(8);
																			OOBTutorialActivity.this
																					.updateText(
																							OOBTutorialActivity.this.explanation,
																							R.string.oob_pullout_shade_explanation,
																							TRANSITION_DELAY,
																							new Runnable() {
																								@Override
																								public void run() {
																									OOBTutorialActivity.this
																											.enableShadePullHandler();
																								}
																							});
																		}
																	});
														}
													}

													@Override
													public void run() {
														float translateY = OOBTutorialActivity.this.getResources()
																.getDimension(R.dimen.oob_tab_translation_y);
														OOBTutorialActivity.this.headsup.setTranslationY(translateY);
														OOBTutorialActivity.this.headsup.setVisibility(0);
														OOBTutorialActivity.this.headsup.animate().translationY(0.0f)
																.setDuration(500).setStartDelay(3000)
																.withEndAction(new AnonymousClass_1(translateY));
													}
												});
									}
								});
					}
				});
	}

	void startTutorialShadeNotificationTest() {
		this.eventLoggerManager.getEventLogger().addEvent(EventLogger.OOB_TUTORIAL_STARTED);
		this.phone.animate().alpha(DefaultRetryPolicy.DEFAULT_BACKOFF_MULT).setDuration(500).setStartDelay(0)
				.withEndAction(new Runnable() {
					@Override
					public void run() {
						OOBTutorialActivity.this.shadeHeight = OOBTutorialActivity.this.shade.getHeight();
						OOBTutorialActivity.this.shade.setVisibility(8);
						OOBTutorialActivity.this.explanation.animate().alpha(DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
								.setDuration(500).setStartDelay(0).withEndAction(new Runnable() {
									@Override
									public void run() {
										OOBTutorialActivity.this.updateText(OOBTutorialActivity.this.explanation,
												R.string.oob_popout_tab_explanation_shade_notification_test,
												READ_DELAY, new Runnable() {
													@Override
													public void run() {
														OOBTutorialActivity.this.updateText(
																OOBTutorialActivity.this.explanation,
																R.string.oob_pullout_shade_explanation, READ_DELAY,
																new Runnable() {
																	@Override
																	public void run() {
																		OOBTutorialActivity.this
																				.enableShadePullHandler();
																	}
																});
													}
												});
									}
								});
					}
				});
	}

	void startTutorialStep2() {
		this.eventLoggerManager.getEventLogger().addEvent(EventLogger.OOB_TUTORIAL_DRAWER_OPENED);
		updateText(this.explanation, R.string.oob_perfect, 0, new Runnable() {
			@Override
			public void run() {
				OOBTutorialActivity.this.updateText(OOBTutorialActivity.this.explanation, R.string.oob_one_more_step,
						TRANSITION_DELAY, new Runnable() {
							@Override
							public void run() {
								OOBTutorialActivity.this.notificationAccess.animate()
										.alpha(DefaultRetryPolicy.DEFAULT_BACKOFF_MULT).setDuration(500)
										.setStartDelay(3000).withEndAction(new Runnable() {
											@Override
											public void run() {
												OOBTutorialActivity.this.updateText(
														OOBTutorialActivity.this.explanation,
														R.string.oob_notification_access_explanation, TRANSITION_DELAY,
														new Runnable() {
															@Override
															public void run() {
																OOBTutorialActivity.this.notificationAccessTapIndicatior
																		.setVisibility(0);
																OOBTutorialActivity.this.notificationAccessTapIndicatior
																		.animate()
																		.alpha(DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
																		.setDuration(500).setStartDelay(1000)
																		.withEndAction(new Runnable() {
																			@Override
																			public void run() {
																				OOBTutorialActivity.this
																						.updateText(
																								OOBTutorialActivity.this.explanation,
																								R.string.oob_notification_access_action,
																								TRANSITION_DELAY, null);
																				OOBTutorialActivity.this.next
																						.setVisibility(0);
																				OOBTutorialActivity.this.next
																						.setAlpha(0.0f);
																				OOBTutorialActivity.this.next
																						.animate()
																						.alpha(DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
																						.setStartDelay(1450)
																						.setDuration(500)
																						.withEndAction(new Runnable() {
																							@Override
																							public void run() {
																								OOBTutorialActivity.this.oobManager
																										.setOOBTutorialCompleted();
																							}
																						});
																			}
																		});
															}
														});
											}
										});
							}
						});
			}
		});
	}

	void startTurnOnNotificationAccess() {
		this.eventLoggerManager.getEventLogger().addEvent(EventLogger.OOB_TUTORIAL_STARTED_AT_NOTIFICATION_ACCESS);
		this.phone.animate().alpha(DefaultRetryPolicy.DEFAULT_BACKOFF_MULT).setDuration(500).setStartDelay(0);
		this.notificationAccess.animate().alpha(DefaultRetryPolicy.DEFAULT_BACKOFF_MULT).setDuration(500)
				.setStartDelay(0);
		this.explanation.setText(R.string.oob_notification_access_action);
		this.explanation.animate().alpha(DefaultRetryPolicy.DEFAULT_BACKOFF_MULT).setDuration(500).setStartDelay(0);
		this.notificationAccessTapIndicatior.animate().alpha(DefaultRetryPolicy.DEFAULT_BACKOFF_MULT).setDuration(500)
				.setStartDelay(0);
		this.notificationAccessTapIndicatior.setVisibility(0);
		this.next.setVisibility(0);
		this.next.setAlpha(0.0f);
		this.next.animate().alpha(DefaultRetryPolicy.DEFAULT_BACKOFF_MULT).setStartDelay(0).setDuration(500);
	}
}

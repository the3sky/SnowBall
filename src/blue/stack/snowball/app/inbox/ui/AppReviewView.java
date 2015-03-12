package blue.stack.snowball.app.inbox.ui;

import blue.stack.snowball.app.R;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;

public class AppReviewView extends FrameLayout {
    View appReviewContent;
    boolean isInFeedbackMode;
    OnReviewCompleteListener listener;
    Button noButton;
    TextView reviewDescription;
    Button yesButton;

    public static interface OnReviewCompleteListener {
        void onGiveFeedback();

        void onRateApp();

        void onReviewSkipped();
    }

    public AppReviewView(Context context) {
        super(context);
        init(null, 0);
    }

    public AppReviewView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public AppReviewView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        this.isInFeedbackMode = false;
        View view = ((LayoutInflater) getContext().getSystemService("layout_inflater")).inflate(R.layout.app_review_view, this, true);
        this.appReviewContent = view.findViewById(R.id.app_review_content);
        this.yesButton = (Button) view.findViewById(R.id.review_yes_button);
        this.noButton = (Button) view.findViewById(R.id.review_no_button);
        this.reviewDescription = (TextView) view.findViewById(R.id.review_description);
        this.yesButton.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                AppReviewView.this.onYesSelected();
            }
        });
        this.noButton.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                AppReviewView.this.onNoSelected();
            }
        });
        enableReviewMode();
    }

    public void setOnReviewCompleteListener(OnReviewCompleteListener listener) {
        this.listener = listener;
    }

    void onYesSelected() {
        if (this.isInFeedbackMode) {
            onGiveFeedback();
        } else {
            onRateApp();
        }
    }

    void onNoSelected() {
        if (this.isInFeedbackMode) {
            onClose();
        } else {
            enableFeedbackMode();
        }
    }

    void enableReviewMode() {
        this.yesButton.setText(getContext().getResources().getString(R.string.review_button_text));
        this.noButton.setText(getContext().getResources().getString(R.string.no_review_button_text));
        this.reviewDescription.setText(getContext().getResources().getString(R.string.review_description));
        this.isInFeedbackMode = false;
    }

    void enableFeedbackMode() {
        this.appReviewContent.animate().alpha(0.0f).setDuration(500).setInterpolator(new AccelerateInterpolator()).withEndAction(new Runnable() {
            public void run() {
                AppReviewView.this.yesButton.setText(AppReviewView.this.getContext().getResources().getString(R.string.give_feedback_button_text));
                AppReviewView.this.noButton.setText(AppReviewView.this.getContext().getResources().getString(R.string.no_give_feedback_button_text));
                AppReviewView.this.reviewDescription.setText(AppReviewView.this.getContext().getResources().getString(R.string.give_feedback_description));
                AppReviewView.this.appReviewContent.animate().alpha(DefaultRetryPolicy.DEFAULT_BACKOFF_MULT).setInterpolator(new DecelerateInterpolator()).setDuration(500);
            }
        });
        this.isInFeedbackMode = true;
    }

    void onRateApp() {
        if (this.listener != null) {
            this.listener.onRateApp();
        }
    }

    void onGiveFeedback() {
        if (this.listener != null) {
            this.listener.onGiveFeedback();
        }
        enableReviewMode();
    }

    void onClose() {
        if (this.listener != null) {
            this.listener.onReviewSkipped();
        }
        enableReviewMode();
    }
}

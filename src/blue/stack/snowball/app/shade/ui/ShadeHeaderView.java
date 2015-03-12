package blue.stack.snowball.app.shade.ui;

import blue.stack.snowball.app.R;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public class ShadeHeaderView extends LinearLayout {
	private float expandedHeight;
	private float headerCardTranslationY;
	private boolean lockToExpansion;

	public ShadeHeaderView(Context context) {
		super(context);
		this.lockToExpansion = false;
	}

	public ShadeHeaderView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.lockToExpansion = false;
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public ShadeHeaderView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		this.lockToExpansion = false;
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public void setExpandedHeight(float expandedHeight) {
		this.expandedHeight = expandedHeight;
		CardView cardHeader = (CardView) findViewById(R.id.header_card);
		int delta = (int) ((((int) getContext().getResources().getDimension(R.dimen.peek_height))) - this.expandedHeight);
		if (delta > 0) {
			cardHeader.setTranslationY(this.headerCardTranslationY - (delta));
		} else {
			cardHeader.setTranslationY(this.headerCardTranslationY);
		}
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		this.headerCardTranslationY = findViewById(R.id.header_card).getTranslationY();
	}
}

package blue.stack.snowball.app.ui;

import blue.stack.snowball.app.R;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

public class AspectRatioedRelativeLayout extends RelativeLayout {
    private static final String TAG = "AspectRatioedRelativeLayout";

    public AspectRatioedRelativeLayout(Context context) {
        super(context);
    }

    public AspectRatioedRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AspectRatioedRelativeLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        ViewGroup content = (ViewGroup) findViewById(R.id.content);
        if (content != null) {
            float heightScale = ((float) MeasureSpec.getSize(heightMeasureSpec)) / ((float) content.getHeight());
            content.setScaleX(heightScale);
            content.setScaleY(heightScale);
            Log.d(TAG, "Scaling content by: " + heightScale);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}

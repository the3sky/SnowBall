package blue.stack.snowball.app.shade.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

public class CircleBackgroundMask extends RelativeLayout {
    private Path clipPath;

    public CircleBackgroundMask(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.clipPath = new Path();
        setWillNotDraw(false);
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        float dy = ((float) h) - 2.0f;
        float dx = (float) w;
        float padding = (float) getPaddingLeft();
        this.clipPath.reset();
        this.clipPath.addCircle(dx / 2.0f, dy / 2.0f, (dx / 2.0f) - padding, Direction.CW);
        this.clipPath.close();
    }

    protected void dispatchDraw(Canvas canvas) {
        canvas.clipPath(this.clipPath);
        super.dispatchDraw(canvas);
    }
}

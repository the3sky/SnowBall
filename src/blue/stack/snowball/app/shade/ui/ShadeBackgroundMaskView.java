package blue.stack.snowball.app.shade.ui;
//package blue.stack.snowball.app.shade.ui;
//
//import android.content.Context;
//import android.content.res.TypedArray;
//import android.graphics.Canvas;
//import android.graphics.Paint;
//import android.graphics.Paint.Style;
//import android.graphics.Path;
//import android.support.v4.view.ViewCompat;
//import android.util.AttributeSet;
//import android.widget.RelativeLayout;
//import com.android.volley.DefaultRetryPolicy;
//import blue.stack.snowball.app.R;
//
//public class ShadeBackgroundMaskView extends RelativeLayout {
//    private float bendFactor;
//    private ShadeBackgroundJustify justify;
//    private Paint mPaint;
//    private Paint mPaintBlack;
//    private Path mPath;
//
//    enum ShadeBackgroundJustify {
//        Left,
//        Right,
//        Both
//    }
//
//    public ShadeBackgroundMaskView(Context context, AttributeSet attrs) {
//        super(context, attrs);
//        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ShadeNotificationBarView);
//        this.bendFactor = a.getFloat(1, 0.5f);
//        this.justify = ShadeBackgroundJustify.values()[a.getInt(0, 0)];
//        init();
//    }
//
//    void init() {
//        this.mPath = new Path();
//        this.mPaint = new Paint();
//        this.mPaintBlack = new Paint();
//        this.mPaint.setColor(getResources().getColor(R.color.snowball_blue));
//        this.mPaintBlack.setColor(ViewCompat.MEASURED_STATE_MASK);
//        setWillNotDraw(false);
//    }
//
//    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
//        float dy = ((float) h) - 2.0f;
//        float dx = (float) w;
//        this.mPath.reset();
//        this.mPath.moveTo(0.0f, 0.0f);
//        this.mPath.cubicTo(this.bendFactor * dy, 0.0f * dy, this.bendFactor * dy, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT * dy, dy, dy);
//        float rightInset = dx - dy;
//        if (this.justify == ShadeBackgroundJustify.Both) {
//            this.mPath.lineTo(rightInset, dy);
//            this.mPath.cubicTo(rightInset + (this.bendFactor * dy), dy * DefaultRetryPolicy.DEFAULT_BACKOFF_MULT, rightInset + (this.bendFactor * dy), dy * 0.0f, dx, 0.0f);
//            this.mPath.close();
//            return;
//        }
//        this.mPath.lineTo(dx, dy);
//        this.mPath.lineTo(dx, 0.0f);
//        this.mPath.close();
//    }
//
//    protected void onDraw(Canvas canvas) {
//        super.onDraw(canvas);
//        this.mPaint.setStyle(Style.FILL);
//        canvas.drawPath(this.mPath, this.mPaint);
//        this.mPaintBlack.setStyle(Style.STROKE);
//        canvas.drawPath(this.mPath, this.mPaintBlack);
//    }
//
//    protected void dispatchDraw(Canvas canvas) {
//        canvas.clipPath(this.mPath);
//        super.dispatchDraw(canvas);
//    }
// }

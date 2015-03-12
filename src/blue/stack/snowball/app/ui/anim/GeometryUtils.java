package blue.stack.snowball.app.ui.anim;

import android.graphics.PointF;
import android.graphics.RectF;

import com.android.volley.DefaultRetryPolicy;

public class GeometryUtils {
    public static PointF normalizePoint(PointF p) {
        PointF c = new PointF();
        c.x = p.x / p.length();
        c.y = p.y / p.length();
        return c;
    }

    public static PointF addPoint(PointF a, PointF b) {
        return new PointF(a.x + b.x, a.y + b.y);
    }

    public static PointF subPoint(PointF a, PointF b) {
        return new PointF(a.x - b.x, a.y - b.y);
    }

    public static PointF scalePoint(PointF a, float scale) {
        return new PointF(a.x * scale, a.y * scale);
    }

    static PointF findIntersection(PointF p1, PointF p2, PointF p3, PointF p4) {
        float xD1 = p2.x - p1.x;
        float xD2 = p4.x - p3.x;
        float yD1 = p2.y - p1.y;
        float yD2 = p4.y - p3.y;
        float xD3 = p1.x - p3.x;
        float yD3 = p1.y - p3.y;
        float len1 = (float) Math.sqrt((double) ((xD1 * xD1) + (yD1 * yD1)));
        float len2 = (float) Math.sqrt((double) ((xD2 * xD2) + (yD2 * yD2)));
        if (Math.abs(((xD1 * xD2) + (yD1 * yD2)) / (len1 * len2)) == DefaultRetryPolicy.DEFAULT_BACKOFF_MULT) {
            return null;
        }
        PointF pt = new PointF(0.0f, 0.0f);
        float div = (yD2 * xD1) - (xD2 * yD1);
        float ua = ((xD2 * yD3) - (yD2 * xD3)) / div;
        float ub = ((xD1 * yD3) - (yD1 * xD3)) / div;
        pt.x = p1.x + (ua * xD1);
        pt.y = p1.y + (ua * yD1);
        xD1 = pt.x - p1.x;
        xD2 = pt.x - p2.x;
        yD1 = pt.y - p1.y;
        yD2 = pt.y - p2.y;
        float segmentLen1 = (float) (Math.sqrt((double) ((xD1 * xD1) + (yD1 * yD1))) + Math.sqrt((double) ((xD2 * xD2) + (yD2 * yD2))));
        xD1 = pt.x - p3.x;
        xD2 = pt.x - p4.x;
        yD1 = pt.y - p3.y;
        yD2 = pt.y - p4.y;
        float segmentLen2 = (float) (Math.sqrt((double) ((xD1 * xD1) + (yD1 * yD1))) + Math.sqrt((double) ((xD2 * xD2) + (yD2 * yD2))));
        if (((double) Math.abs(len1 - segmentLen1)) <= 0.01d) {
            if (((double) Math.abs(len2 - segmentLen2)) <= 0.01d) {
                return pt;
            }
        }
        return null;
    }

    public static PointF getIntersection(RectF rect, PointF segmentStart, PointF segmentDirection) {
        if (segmentDirection.x == 0.0f && segmentDirection.y == 0.0f) {
            float distanceFromLeft = segmentStart.x - rect.left;
            float distanceFromRight = rect.right - segmentStart.x;
            float distanceFromTop = segmentStart.y - rect.top;
            float distanceFromBottom = rect.bottom - segmentStart.y;
            if (distanceFromLeft <= distanceFromRight && distanceFromLeft <= distanceFromTop && distanceFromLeft <= distanceFromBottom) {
                return new PointF(rect.left, segmentStart.y);
            }
            if (distanceFromRight <= distanceFromLeft && distanceFromRight <= distanceFromTop && distanceFromRight <= distanceFromBottom) {
                return new PointF(rect.right, segmentStart.y);
            }
            if (distanceFromTop <= distanceFromLeft && distanceFromTop <= distanceFromRight && distanceFromTop <= distanceFromBottom) {
                return new PointF(segmentStart.x, rect.top);
            }
            if (distanceFromBottom <= distanceFromLeft && distanceFromBottom <= distanceFromRight && distanceFromBottom <= distanceFromTop) {
                return new PointF(segmentStart.x, rect.bottom);
            }
        }
        segmentDirection = normalizePoint(segmentDirection);
        float lengthBox = rect.width() + rect.height();
        segmentDirection.x *= lengthBox;
        segmentDirection.y *= lengthBox;
        PointF segmentEnd = new PointF(segmentStart.x + segmentDirection.x, segmentStart.y + segmentDirection.y);
        PointF intersection = findIntersection(new PointF(rect.left, rect.top), new PointF(rect.right, rect.top), segmentStart, segmentEnd);
        if (intersection == null) {
            intersection = findIntersection(new PointF(rect.right, rect.top), new PointF(rect.right, rect.bottom), segmentStart, segmentEnd);
        }
        if (intersection == null) {
            intersection = findIntersection(new PointF(rect.left, rect.bottom), new PointF(rect.right, rect.bottom), segmentStart, segmentEnd);
        }
        return intersection == null ? findIntersection(new PointF(rect.left, rect.top), new PointF(rect.left, rect.bottom), segmentStart, segmentEnd) : intersection;
    }
}

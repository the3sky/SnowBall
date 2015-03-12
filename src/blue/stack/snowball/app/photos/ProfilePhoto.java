package blue.stack.snowball.app.photos;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.net.Uri;
import blue.stack.snowball.app.oob.OOBTutorialActivity;

public class ProfilePhoto {
    private Bitmap bitmap;
    private Uri imageUri;
    private ImageType preferredImageType;

    public enum ImageType {
        BITMAP,
        URI,
        NONE
    }

    public ProfilePhoto() {
        this.preferredImageType = ImageType.NONE;
    }

    public Bitmap getBitmap() {
        return this.bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public Uri getImageUri() {
        return this.imageUri;
    }

    public void setImageUri(Uri imageUri) {
        this.imageUri = imageUri;
    }

    public ImageType getPreferredImageType() {
        return this.preferredImageType;
    }

    public void setPreferredImageType(ImageType preferredImageType) {
        this.preferredImageType = preferredImageType;
    }

    public static Bitmap getRoundedRectBitmap(Bitmap bitmap) {
        Bitmap result = null;
        try {
            int preferredSize = Math.min(bitmap.getWidth(), bitmap.getHeight());
            result = Bitmap.createBitmap(preferredSize, preferredSize, Config.ARGB_8888);
            Canvas canvas = new Canvas(result);
            Paint paint = new Paint();
            Rect rect = new Rect(0, 0, OOBTutorialActivity.PULSE_FADE_TIME, OOBTutorialActivity.PULSE_FADE_TIME);
            paint.setAntiAlias(true);
            canvas.drawARGB(0, 0, 0, 0);
            paint.setColor(-12434878);
            canvas.drawCircle((float) (preferredSize / 2), (float) (preferredSize / 2), (float) (preferredSize / 2), paint);
            paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
            canvas.drawBitmap(bitmap, rect, rect, paint);
            return result;
        } catch (NullPointerException e) {
            return result;
        } catch (OutOfMemoryError e2) {
            return result;
        }
    }
}

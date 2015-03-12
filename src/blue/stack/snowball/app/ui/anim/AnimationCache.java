package blue.stack.snowball.app.ui.anim;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class AnimationCache {
    private Map<String, List<byte[]>> cache;
    @Inject
    Context context;

    @Inject
    private AnimationCache() {
        this.cache = new HashMap();
    }

    public void flushCache() {
        this.cache.clear();
    }

    public List<Drawable> getAnimation(String animationName) {
        if (!this.cache.containsKey(animationName)) {
            loadAnimaiton(animationName);
        }
        List<byte[]> animationList = (List) this.cache.get(animationName);
        List<Drawable> drawableList = new ArrayList();
        for (byte[] pngData : animationList) {
            drawableList.add(new BitmapDrawable(this.context.getResources(), BitmapFactory.decodeByteArray(pngData, 0, pngData.length)));
        }
        return drawableList;
    }

    private void loadAnimaiton(String animationName) {
        ArrayList<byte[]> animationList = new ArrayList();
        try {
            for (String name : this.context.getAssets().list(animationName)) {
                String fileName = animationName + File.separator + name;
                byte[] buffer = new byte[((int) this.context.getAssets().openFd(fileName).getLength())];
                int readLength = this.context.getAssets().open(fileName).read(buffer);
                animationList.add(buffer);
            }
        } catch (Exception e) {
        }
        this.cache.put(animationName, animationList);
    }
}

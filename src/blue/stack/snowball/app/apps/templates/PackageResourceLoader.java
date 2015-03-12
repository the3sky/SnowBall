package blue.stack.snowball.app.apps.templates;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.util.Log;

public class PackageResourceLoader {
    private static final String TAG = "PackageResourceLoader";

    public static String loadStringByResourceName(Context context, String packageName, String resourceName) {
        return loadStringByResourceName(context, packageName, resourceName, null);
    }

    public static String loadStringByResourceName(Context context, String packageName, String resourceName, String defPackageName) {
        String str = null;
        if (defPackageName == null) {
            defPackageName = packageName;
        }
        try {
            Resources resources = context.getPackageManager().getResourcesForApplication(packageName);
            str = resources.getString(resources.getIdentifier(resourceName, "string", defPackageName));
        } catch (NameNotFoundException nnfe) {
            Log.d(TAG, "Caught PackageManager.NameNotFoundException when loading resource: " + Log.getStackTraceString(nnfe));
        } catch (Exception e) {
            Log.d(TAG, "Caught exception when loading resource: " + Log.getStackTraceString(e));
        }
        return str;
    }

    public static int loadResourceIdByResourceName(Context context, String packageName, String resourceName, String resourceType) {
        return loadResourceIdByResourceName(context, packageName, resourceName, resourceType, null);
    }

    public static int loadResourceIdByResourceName(Context context, String packageName, String resourceName, String resourceType, String defPackageName) {
        int resourceId = 0;
        try {
            resourceId = context.getPackageManager().getResourcesForApplication(packageName).getIdentifier(resourceName, resourceType, packageName);
        } catch (NameNotFoundException nnfe) {
            Log.d(TAG, "Caught PackageManager.NameNotFoundException when loading resource: " + Log.getStackTraceString(nnfe));
        } catch (Exception e) {
            Log.d(TAG, "Caught exception when loading resource: " + Log.getStackTraceString(e));
        }
        return resourceId;
    }
}

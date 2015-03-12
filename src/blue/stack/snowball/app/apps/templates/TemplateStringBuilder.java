package blue.stack.snowball.app.apps.templates;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

public class TemplateStringBuilder {
    List<Element> elements;
    List<PostProcessor> postProcessors;

    static interface Element {
        String getString(Context context);
    }

    static interface PostProcessor {
        String processString(Context context, String str);
    }

    class StringElement implements Element {
        String element;

        public StringElement(String element) {
            this.element = element;
        }

        public String getString(Context context) {
            return this.element;
        }
    }

    class StringExternalResourceElement implements Element {
        String defPackageName;
        String packageName;
        String resourceName;

        public StringExternalResourceElement(String packageName, String resourceName, String defPackageName) {
            this.packageName = packageName;
            this.resourceName = resourceName;
            this.defPackageName = defPackageName;
        }

        public String getString(Context context) {
            return this.defPackageName == null ? PackageResourceLoader.loadStringByResourceName(context, this.packageName, this.resourceName) : PackageResourceLoader.loadStringByResourceName(context, this.packageName, this.resourceName, this.defPackageName);
        }
    }

    class StringExternalResourceIdElement implements Element {
        String defPackageName;
        String packageName;
        String resourceName;
        String resourceType;

        public StringExternalResourceIdElement(String packageName, String resourceName, String resourceType, String defPackageName) {
            this.packageName = packageName;
            this.resourceName = resourceName;
            this.resourceType = resourceType;
            this.defPackageName = defPackageName;
        }

        public String getString(Context context) {
            int resourceId;
            if (this.defPackageName == null) {
                resourceId = PackageResourceLoader.loadResourceIdByResourceName(context, this.packageName, this.resourceName, this.resourceType);
            } else {
                resourceId = PackageResourceLoader.loadResourceIdByResourceName(context, this.packageName, this.resourceName, this.resourceType, this.defPackageName);
            }
            return resourceId != 0 ? Integer.toString(resourceId) : null;
        }
    }

    class StringResourceElement implements Element {
        int resId;

        public StringResourceElement(int resId) {
            this.resId = resId;
        }

        public String getString(Context context) {
            return this.resId != 0 ? context.getResources().getString(this.resId) : null;
        }
    }

    class TokenReplacer implements PostProcessor {
        Element replaceWith;
        String tokenToReplace;

        TokenReplacer(Element replaceWith, String tokenToReplace) {
            this.replaceWith = replaceWith;
            this.tokenToReplace = tokenToReplace;
        }

        public String processString(Context context, String str) {
            return StringUtils.replaceFirstLiteral(str, this.tokenToReplace, this.replaceWith.getString(context));
        }
    }

    public TemplateStringBuilder() {
        this.elements = new ArrayList();
        this.postProcessors = new ArrayList();
    }

    public TemplateStringBuilder addString(String string) {
        this.elements.add(new StringElement(string));
        return this;
    }

    public TemplateStringBuilder addString(int resId) {
        this.elements.add(new StringResourceElement(resId));
        return this;
    }

    public TemplateStringBuilder addString(String packageName, String resourceName) {
        this.elements.add(new StringExternalResourceElement(packageName, resourceName, null));
        return this;
    }

    public TemplateStringBuilder addString(String packageName, String resourceName, String defPackageName) {
        this.elements.add(new StringExternalResourceElement(packageName, resourceName, defPackageName));
        return this;
    }

    public TemplateStringBuilder addResourceIdString(String packageName, String resourceName, String resourceType) {
        this.elements.add(new StringExternalResourceIdElement(packageName, resourceName, resourceType, null));
        return this;
    }

    public TemplateStringBuilder addResourceIdString(String packageName, String resourceName, String resourceType, String defPackageName) {
        this.elements.add(new StringExternalResourceIdElement(packageName, resourceName, resourceType, defPackageName));
        return this;
    }

    public TemplateStringBuilder replaceTokenWithString(String token, String string) {
        this.postProcessors.add(new TokenReplacer(new StringElement(string), token));
        return this;
    }

    public TemplateStringBuilder replaceTokenWithString(String token, int resId) {
        this.postProcessors.add(new TokenReplacer(new StringResourceElement(resId), token));
        return this;
    }

    public TemplateStringBuilder replaceTokenWithString(String token, String packageName, String resourceName) {
        this.postProcessors.add(new TokenReplacer(new StringExternalResourceElement(packageName, resourceName, null), token));
        return this;
    }

    public TemplateStringBuilder replaceTokenWithString(String token, String packageName, String resourceName, String defPackageName) {
        this.postProcessors.add(new TokenReplacer(new StringExternalResourceElement(packageName, resourceName, defPackageName), token));
        return this;
    }

    public String build(Context context) {
        String str;
        StringBuilder sb = new StringBuilder();
        for (Element element : this.elements) {
            str = element.getString(context);
            if (str == null) {
                return null;
            }
            sb.append(str);
        }
        str = sb.toString();
        for (PostProcessor postProcessor : this.postProcessors) {
            str = postProcessor.processString(context, str);
        }
        return str;
    }
}

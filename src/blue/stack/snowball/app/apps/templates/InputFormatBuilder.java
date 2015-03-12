package blue.stack.snowball.app.apps.templates;

import java.util.LinkedHashMap;
import java.util.Map;

import android.content.Context;
import android.util.Log;

public class InputFormatBuilder {
    private static final String TAG = "InputFormatBuilder";
    Map<String, Integer> inputLabels;
    String inputTemplate;
    String inputTemplatePackageName;
    String inputTemplateResourceName;
    boolean isInputTemplateInCanonicalForm;
    LinkedHashMap<String, String> labeledInputTokens;

    public InputFormatBuilder() {
        this.labeledInputTokens = new LinkedHashMap();
        this.inputLabels = new LinkedHashMap();
        this.isInputTemplateInCanonicalForm = false;
    }

    public InputFormatBuilder setInputTemplate(String inputTemplate) {
        this.inputTemplate = inputTemplate;
        this.isInputTemplateInCanonicalForm = false;
        return this;
    }

    public InputFormatBuilder setInputTemplateFromPackage(String inputTemplatePackageName, String inputTemplateResourceName) {
        this.inputTemplatePackageName = inputTemplatePackageName;
        this.inputTemplateResourceName = inputTemplateResourceName;
        this.isInputTemplateInCanonicalForm = false;
        return this;
    }

    public InputFormatBuilder addInputToken(String inputToken, String label) {
        this.labeledInputTokens.put(label, inputToken);
        return this;
    }

    public InputFormatBuilder labelToken(int token, String label) {
        this.inputLabels.put(label, Integer.valueOf(token));
        return this;
    }

    public InputFormatBuilder setCanonicalInputTemplate(String inputTemplate) {
        this.inputTemplate = inputTemplate;
        this.isInputTemplateInCanonicalForm = true;
        return this;
    }

    public InputFormatBuilder setCanonicalInputTemplateFromPackage(String inputTemplatePackageName, String inputTemplateResourceName) {
        this.inputTemplatePackageName = inputTemplatePackageName;
        this.inputTemplateResourceName = inputTemplateResourceName;
        this.isInputTemplateInCanonicalForm = true;
        return this;
    }

    public InputFormat build(Context context) {
        if (!(this.inputTemplatePackageName == null || this.inputTemplateResourceName == null)) {
            this.inputTemplate = PackageResourceLoader.loadStringByResourceName(context, this.inputTemplatePackageName, this.inputTemplateResourceName);
        }
        if (this.inputTemplate != null) {
            return this.isInputTemplateInCanonicalForm ? InputFormat.buildFromCanonicalTemplate(this.inputTemplate, this.inputLabels) : InputFormat.buildFromTemplate(this.inputTemplate, this.labeledInputTokens);
        } else {
            Log.d(TAG, "Error input template is null");
            return null;
        }
    }
}

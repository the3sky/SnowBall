package blue.stack.snowball.app.apps.templates;

import android.content.Context;
import android.util.Log;

public class OutputFormatBuilder {
    private static final String TAG = "OutputFormatBuilder";
    String outputTemplate;
    int outputTemplateResourceId;

    public OutputFormatBuilder setOutputTemplate(String outputTemplate) {
        this.outputTemplate = outputTemplate;
        return this;
    }

    public OutputFormatBuilder setOutputTemplateFromResouce(int outputTemplateResourceId) {
        this.outputTemplateResourceId = outputTemplateResourceId;
        return this;
    }

    public OutputFormat build(Context context) {
        if (this.outputTemplateResourceId != 0) {
            this.outputTemplate = context.getResources().getString(this.outputTemplateResourceId);
        }
        if (this.outputTemplate != null) {
            return OutputFormat.buildFromTemplate(this.outputTemplate);
        }
        Log.d(TAG, "Error output template is null");
        return null;
    }
}

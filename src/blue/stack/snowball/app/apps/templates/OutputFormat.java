package blue.stack.snowball.app.apps.templates;

import java.util.Map;
import java.util.Map.Entry;

import android.util.Log;

public class OutputFormat {
    private static final String TAG = "OutputFormat";
    String outputTemplate;

    public static OutputFormat buildFromTemplate(String outputTemplate) {
        if (outputTemplate == null) {
            Log.d(TAG, "output template is null");
            return null;
        }
        OutputFormat outputFormat = new OutputFormat();
        outputFormat.outputTemplate = outputTemplate;
        return outputFormat;
    }

    public String applyOutputFormat(Map<String, String> tokensAndValues) {
        String outputString = this.outputTemplate;
        for (Entry<String, String> stringStringEntry : tokensAndValues.entrySet()) {
            String value = (String) stringStringEntry.getValue();
            outputString = outputString.replace("${" + ((String) stringStringEntry.getKey()) + "}", value);
        }
        return outputString;
    }
}

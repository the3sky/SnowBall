package blue.stack.snowball.app.apps.templates;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.util.Log;

public class InputFormat {
    private static final String TAG = "InputFormat";
    Map<String, String> canonicalInputLabels;
    String canonicalInputTemplate;
    List<String> canonicalInputTokens;

    private InputFormat() {
    }

    public static InputFormat buildFromTemplate(String inputTemplate, LinkedHashMap<String, String> labeledInputTokens) {
        if (inputTemplate == null) {
            return null;
        }
        String canonicalInputTemplate = inputTemplate;
        List<String> canonicalInputTokens = new ArrayList();
        Map<String, String> canonicalInputLabels = new HashMap();
        int i = 1;
        for (Entry<String, String> stringStringEntry : labeledInputTokens.entrySet()) {
            String inputLabel = (String) stringStringEntry.getKey();
            String inputToken = (String) stringStringEntry.getValue();
            String canonicalToken = "%" + i;
            String canonicalTokenWithType = canonicalToken + "\\$s";
            canonicalInputTokens.add(canonicalToken);
            canonicalInputLabels.put(inputLabel, canonicalToken);
            if (canonicalInputTemplate.contains(inputToken)) {
                canonicalInputTemplate = StringUtils.replaceFirstLiteral(canonicalInputTemplate, inputToken, canonicalTokenWithType);
                i++;
            } else {
                Log.d(TAG, "Input template is missing one of the specified input tokens: template = " + inputTemplate + " |  missing token = " + inputToken);
                return null;
            }
        }
        InputFormat inputFormat = new InputFormat();
        inputFormat.canonicalInputTemplate = canonicalInputTemplate;
        inputFormat.canonicalInputTokens = canonicalInputTokens;
        inputFormat.canonicalInputLabels = canonicalInputLabels;
        return inputFormat;
    }

    public static InputFormat buildFromCanonicalTemplate(String inputTemplate, Map<String, Integer> inputLabels) {
        if (inputTemplate == null) {
            return null;
        }
        int numTokens = inputLabels.size();
        int i = 0;
        while (i < numTokens) {
            int tokenNum = i + 1;
            if (inputLabels.containsValue(Integer.valueOf(tokenNum))) {
                i++;
            } else {
                Log.d(TAG, "Failed to validate input labels -- " + tokenNum + " is missing");
                return null;
            }
        }
        Map<String, String> canonicalInputLabels = new HashMap();
        if (inputLabels != null) {
            for (Entry<String, Integer> stringIntegerEntry : inputLabels.entrySet()) {
                canonicalInputLabels.put((String) stringIntegerEntry.getKey(), "%" + Integer.toString(((Integer) stringIntegerEntry.getValue()).intValue()));
            }
        }
        String canonicalInputTemplate = inputTemplate;
        List<String> canonicalInputTokens = new ArrayList();
        for (i = 0; i < numTokens; i++) {
            canonicalInputTokens.add("%" + Integer.toString(i + 1));
        }
        InputFormat inputFormat = new InputFormat();
        inputFormat.canonicalInputTemplate = canonicalInputTemplate;
        inputFormat.canonicalInputTokens = canonicalInputTokens;
        inputFormat.canonicalInputLabels = canonicalInputLabels;
        return inputFormat;
    }

    public String getCanonicalInputTemplate() {
        return this.canonicalInputTemplate;
    }

    public String[] getCanonicalInputTokens() {
        return (String[]) this.canonicalInputTokens.toArray(new String[this.canonicalInputTokens.size()]);
    }

    public Map<String, String> getCanonicalInputLabels() {
        return this.canonicalInputLabels;
    }
}

package blue.stack.snowball.app.apps.templates;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import android.util.Log;

public class MessageProcessor {
    public static final String DEFAULT_INPUT_KEY = "default";
    private static final String TAG = "MessageProcessor";
    Map<String, InputFormat> inputFormatMap;
    Map<String, OutputFormat> outputFormatMap;

    public class Result {
        Map<String, String> labeledValues;
        Map<String, String> outputMap;

        public Result() {
            this.outputMap = null;
            this.labeledValues = null;
        }

        public Result(Map<String, String> outputMap, Map<String, String> labeledValues) {
            this.outputMap = outputMap;
            this.labeledValues = labeledValues;
        }

        public boolean matches() {
            return this.labeledValues != null;
        }

        public String getOutputForKey(String key) {
            return this.outputMap != null ? (String) this.outputMap.get(key) : null;
        }

        public String getValue(String label) {
            return this.labeledValues != null ? (String) this.labeledValues.get(label) : null;
        }
    }

    public static MessageProcessor newInstance(Map<String, InputFormat> inputFormatMap, Map<String, OutputFormat> outputFormatMap) {
        if (inputFormatMap == null) {
            Log.d(TAG, "InputFormatMap is null");
            return null;
        } else if (inputFormatMap.size() == 0) {
            Log.d(TAG, "zero input formats specified");
            return null;
        } else {
            for (InputFormat inputFormat : inputFormatMap.values()) {
                if (inputFormat == null) {
                    Log.d(TAG, "found null input format");
                    return null;
                }
            }
            MessageProcessor messageProcessor = new MessageProcessor();
            messageProcessor.inputFormatMap = inputFormatMap;
            messageProcessor.outputFormatMap = outputFormatMap;
            return messageProcessor;
        }
    }

    public Result processMessage(String input) {
        Map inputs = new HashMap();
        inputs.put(DEFAULT_INPUT_KEY, input);
        return processMessage(inputs);
    }

    public Result processMessage(Map<String, String> inputs) {
        Map<String, String> labeledResults = new HashMap();
        Map<String, String> output = new HashMap();
        try {
            String key;
            for (Entry<String, InputFormat> stringInputFormatEntry : this.inputFormatMap.entrySet()) {
                key = (String) stringInputFormatEntry.getKey();
                InputFormat inputFormat = (InputFormat) stringInputFormatEntry.getValue();
                String input = (String) inputs.get(key);
                if (input == null) {
                    Log.d(TAG, "Input not specified for key: " + key);
                    return new Result();
                }
                Map<String, String> labeledResult = processInput(input, inputFormat);
                if (labeledResult == null) {
                    Log.d(TAG, "Failed to get labeled results for key: " + key);
                    return new Result();
                }
                labeledResults.putAll(labeledResult);
            }
            for (Entry<String, OutputFormat> stringOutputFormatEntry : this.outputFormatMap.entrySet()) {
                key = (String) stringOutputFormatEntry.getKey();
                String outputForKey = ((OutputFormat) stringOutputFormatEntry.getValue()).applyOutputFormat(labeledResults);
                if (outputForKey == null) {
                    break;
                }
                output.put(key, outputForKey);
            }
            return new Result(output, labeledResults);
        } catch (Exception e) {
            Log.d(TAG, "Caught exception: " + Log.getStackTraceString(e));
            return new Result();
        }
    }

    Map<String, String> processInput(String input, InputFormat inputFormat) {
        String canonicalInputTemplate = inputFormat.getCanonicalInputTemplate();
        String[] canonicalInputTokens = inputFormat.getCanonicalInputTokens();
        Map<String, String> canonicalInputLabels = inputFormat.getCanonicalInputLabels();
        Map<String, String> canonicalResult = new NumberedFormatStringParser(canonicalInputTemplate, canonicalInputTokens).parseFormattedString(input);
        return canonicalResult != null ? getLabeledResult(canonicalResult, canonicalInputLabels) : null;
    }

    Map<String, String> getLabeledResult(Map<String, String> canonicalResult, Map<String, String> canonicalLabels) {
        Map<String, String> labeledResult = new HashMap();
        for (Entry<String, String> stringStringEntry : canonicalLabels.entrySet()) {
            String label = (String) stringStringEntry.getKey();
            String value = (String) canonicalResult.get((String) stringStringEntry.getValue());
            if (value == null) {
                return null;
            }
            labeledResult.put(label, value);
        }
        return labeledResult;
    }
}

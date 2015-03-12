package blue.stack.snowball.app.apps.templates;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;

public class MessageProcessorBuilder {
	private static final String TAG = "MessageProcessorBuilder";
	Map<String, InputFormat> inputFormatMap;
	Map<String, OutputFormat> outputFormatMap;

	public MessageProcessorBuilder() {
		this.inputFormatMap = new HashMap();
		this.outputFormatMap = new HashMap();
	}

	public MessageProcessorBuilder setInputFormat(InputFormat inputFormat) {
		this.inputFormatMap.put(MessageProcessor.DEFAULT_INPUT_KEY, inputFormat);
		return this;
	}

	public MessageProcessorBuilder addInputFormat(String key, InputFormat inputFormat) {
		this.inputFormatMap.put(key, inputFormat);
		return this;
	}

	public MessageProcessorBuilder addOutputFormat(String key, OutputFormat outputFormat) {
		if (outputFormat != null) {
			this.outputFormatMap.put(key, outputFormat);
		}
		return this;
	}

	public MessageProcessor build(Context context) {
		return MessageProcessor.newInstance(this.inputFormatMap, this.outputFormatMap);
	}
}

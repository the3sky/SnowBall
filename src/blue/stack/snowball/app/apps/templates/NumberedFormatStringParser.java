package blue.stack.snowball.app.apps.templates;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import android.util.Log;

public class NumberedFormatStringParser {
	private static final String TAG = "NumberedFormatStringParser";
	String formatString;
	int[] order;
	Pattern pattern;
	String[] tokens;

	public NumberedFormatStringParser(String formatString, String[] tokens) {
		this.formatString = formatString;
		this.tokens = tokens;
		this.order = getTokenOrder(formatString, tokens);
		this.pattern = getPattern(formatString, tokens);
	}

	public void setFormatString(String formatString) {
		this.formatString = formatString;
	}

	public void setTokens(String[] tokens) {
		this.tokens = tokens;
	}

	public Map<String, String> parseFormattedString(String str) {
		Map<String, String> map = null;
		Matcher matcher = this.pattern.matcher(str);
		if (matcher.matches() && this.order != null && this.order.length == this.tokens.length
				&& matcher.groupCount() == this.tokens.length) {
			map = new HashMap();
			int i = 0;
			while (i < this.tokens.length) {
				if (this.order[i] >= 0 && this.order[i] < matcher.groupCount()) {
					String value = matcher.group(this.order[i] + 1);
					if (value == null) {
						value = "";
					}
					map.put(this.tokens[i], value);
				}
				i++;
			}
		}
		return map;
	}

	static int getTokenIndex(String str, String token) {
		if (token == null) {
			return -1;
		}
		int index = str.indexOf(token);
		return index == -1 ? str.indexOf(new StringBuilder(token).reverse().toString()) : index;
	}

	static Pattern getPattern(String formatString, String[] tokens) {
		String pms = "\\Q" + formatString + "\\E";
		for (String token : tokens) {
			if (token != null) {
				pms = pms.replace(token, "").replace(new StringBuilder(token).reverse().toString(), "");
			}
		}
		pms = pms.replace("$s", "\\E([\\s\\S]*)\\Q");
		Pattern pattern = null;
		try {
			pattern = Pattern.compile(pms);
		} catch (PatternSyntaxException e) {
			Log.d(TAG, "Caught PatternSyntaxException compiling string: " + pms);
		}
		return pattern;
	}

	static int[] getTokenOrder(String formatString, String[] tokens) {
		int i;
		Map<Integer, Integer> positionMap = new HashMap(tokens.length);
		int[] tokenIndicies = new int[tokens.length];
		for (i = 0; i < tokens.length; i++) {
			int index = getTokenIndex(formatString, tokens[i]);
			if (index == -1) {
				return null;
			}
			tokenIndicies[i] = index;
			positionMap.put(Integer.valueOf(tokenIndicies[i]), Integer.valueOf(i));
		}
		Arrays.sort(tokenIndicies);
		int[] order = new int[tokenIndicies.length];
		for (i = 0; i < order.length; i++) {
			order[i] = positionMap.get(Integer.valueOf(tokenIndicies[i])).intValue();
		}
		return order;
	}
}

package blue.stack.snowball.app.apps.templates;

public class StringUtils {
    public static String replaceFirstLiteral(String str, String target, String replacement) {
        return str.replaceFirst("\\Q" + target + "\\E", replacement);
    }
}

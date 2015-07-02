package talon;

import java.util.regex.Matcher;

public class Utils {
    public static String getDelimiter(String body) {
        Matcher matcher = Constants.RE_DELIMITER.matcher(body);
        boolean found = matcher.find();
        if (found) {
            return matcher.group();
        } else {
            return "\n";
        }
    }

    public static String join(String[] string, String delimiter) {
        StringBuilder sbStr = new StringBuilder();
        for (int i = 0, il = string.length; i < il; i++) {
            if (i > 0)
                sbStr.append(delimiter);
            sbStr.append(string[i]);
        }
        return sbStr.toString();
    }
}

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
}

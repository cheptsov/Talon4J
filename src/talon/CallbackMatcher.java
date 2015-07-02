package talon;

import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CallbackMatcher {
    public interface ReplaceCallback {
        String replaceMatch(MatchResult matchResult);
    }

    private final Pattern pattern;

    public CallbackMatcher(Pattern pattern) {
        this.pattern = pattern;
    }

    public String replaceMatches(String string, ReplaceCallback callback) {
        final Matcher matcher = this.pattern.matcher(string);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            MatchResult matchResult = matcher.toMatchResult();
            matcher.appendReplacement(buffer, callback.replaceMatch(matchResult));
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }
}

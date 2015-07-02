package talon;

import org.junit.Test;

import java.util.regex.MatchResult;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

public class CallbackMatcherTest {

    @Test
    public void testReplaceMatches() throws Exception {
        assertEquals("found<ab12> cd found<efg34> asdf 123", new CallbackMatcher(Pattern.compile("([a-zA-Z]+[0-9]+)"))
                .replaceMatches("ab12 cd efg34 asdf 123", new CallbackMatcher.ReplaceCallback() {
                    @Override
                    public String replaceMatch(MatchResult matchResult) {
                        return "found<" + matchResult.group() +">";
            }
        }));
    }
}
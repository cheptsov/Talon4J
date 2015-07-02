package talon;

import java.util.Arrays;
import java.util.Objects;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The module's functions operate on message bodies trying to extract
 * original messages (without quoted messages)
 */
public class Quotation {
    public static final Pattern RE_FWD = Pattern.compile("^[-]+[ ]*Forwarded message[ ]*[-]+$", Pattern.CASE_INSENSITIVE);

    public static final Pattern RE_ON_DATE_SMB_WROTE =
            Pattern.compile(String.format("(-*[ ]?(%1$s)[ ].*(%2$s)(.*%n)\\{0,2}.*(%3$s):?-*)",
                    // Beginning of the line
                    // English
                    "On|" +
                            // French
                            "Le|" +
                            // Polish
                            "W dniu|" +
                            // Dutch
                            "Op",
                    // Date and sender separator
                    // most languages separate date and sender address by comma
                    ",|" +
                            // polish date and sender address separator
                            "|użytkownik|",
                    // English
                    "wrote|sent|" +
                            // French
                            "a écrit|" +
                            // Polish
                            "napisał|" +
                            // Dutch
                            "schreef|verzond|geschreven"));

    public static final Pattern RE_ON_DATE_WROTE_SMB = Pattern.compile(
            String.format("(-*[ ]?(%1$s)[ ].*(.*%n){0,2}.*(%2$s)[ ].*:)",
                    // Beginning of the line
                    "Op",
                    // Ending of the line
                    // Dutch
                    "schreef|verzond|geschreven"));


    public static final Pattern RE_QUOTATION = Pattern.compile(
            // quotation border: splitter line or a number of quotation marker lines
            "((s|(me*){2,}).*me*)[te]*$");

    public static final Pattern RE_EMPTY_QUOTATION = Pattern.compile(
            "(s|(me*){2,})e*");

    public static final Pattern RE_ORIGINAL_MESSAGE = Pattern.compile(
            String.format("[\\s]*[-]+[ ]*(%s)[ ]*[-]+",
                    // English
                    "Original Message|Reply Message|" +
                            // German
                            "Ursprüngliche Nachricht|Antwort Nachricht|" +
                            // Danish
                            "Oprindelig meddelelse"), Pattern.CASE_INSENSITIVE);

    public static final Pattern RE_FROM_COLON_OR_DATE_COLON = Pattern.compile(
            String.format("(_+\\r?\\n)?[\\s]*(:?[*]?%s)[\\s]?:[*]? .*",
                    // "From" in different languages.
                    "From|Van|De|Von|Fra|" +
                            // "Date" in different languages.
                            "Date|Datum|Envoyé"), Pattern.CASE_INSENSITIVE);

    public static final Pattern[] SPLITTER_PATTERNS = new Pattern[]{
            RE_ORIGINAL_MESSAGE,
            Pattern.compile("(\\d+/\\d+/\\d+|\\d+\\.\\d+\\.\\d+).*@"),
            RE_ON_DATE_SMB_WROTE,
            RE_ON_DATE_WROTE_SMB,
            RE_FROM_COLON_OR_DATE_COLON,
            Pattern.compile("\\S{3,10}, \\d\\d? \\S{3,10} 20\\d\\d,? \\d\\d?:\\d\\d(:\\d\\d)?( \\S+){3,6}@\\S+:")
    };

    public static final Pattern RE_LINK = Pattern.compile("<(http://[^>]*)>");

    public static final Pattern RE_NORMALIZED_LINK = Pattern.compile("@@(http://[^>@]*)@@");

    public static final Pattern RE_PARENTHESIS_LINK = Pattern.compile("\\(https?://");

    public static final int SPLITTER_MAX_LINES = 4;

    public static final int MAX_LINES_COUNT = 1000;

    public static final Pattern QUOT_PATTERN = Pattern.compile("^>+ ?");

    public static final Pattern NO_QUOT_LINE = Pattern.compile("^[^>].*[\\S].*");

    /**
     * Extracts a non quoted message from provided plain text.
     */
    public static String extractFromPlain(String body) {
        String strippedText = body;
        String delimiter = Utils.getDelimiter(body);
        body = preprocess(body, delimiter);
        String[] lines = body.split("\r?\n");
        // don't process too long messages
        if (lines.length > MAX_LINES_COUNT) {
            return strippedText;
        }
        char[] markers = markMessageLines(lines);
        // TODO
        return "";
    }

    /**
     * Mark message lines with markers to distinguish quotation lines.
     * <p>
     * Markers:
     * <ul>
     * <li>e - empty line
     * <li>m - line that starts with quotation marker '>'
     * <li>s - splitter line
     * <li>t - presumably lines from the last message in the conversation
     * </ul>
     */
    static char[] markMessageLines(String[] lines) {
        char[] markers = new char[lines.length];
        int i = 0;
        while (i < lines.length) {
            Matcher quoteMatcher = QUOT_PATTERN.matcher(lines[i]);
            Matcher reFwdMatcher = RE_FWD.matcher(lines[i]);
            if (lines[i].trim().length() == 0) {
                markers[i] = 'e'; // empty line
            } else if (quoteMatcher.find() && quoteMatcher.start() == 0) {
                markers[i] = 'm'; // line with quotation marker
            } else if (reFwdMatcher.find() && reFwdMatcher.start() == 0) {
                markers[i] = 'f'; // # ---- Forwarded message ----
            } else {
                // in case splitter is spread across several lines
                Matcher splitter = isSplitter(Utils.join(Arrays.asList(lines).subList(i, Math.min(lines.length, i + MAX_LINES_COUNT)).toArray(new String[0]), "\n"));
                if (splitter != null) {
                    // append as many splitter markers as lines in splitter
                    String[] splitterLines = splitter.group().split("\r?\n");
                    for (int j = 0; j < splitterLines.length; j++) {
                        markers[i + j] = 's';
                    }
                    // skip splitter lines
                    i += splitterLines.length - 1;
                } else {
                    // probably the line from the last message in the conversation
                    markers[i] = 't';
                }
            }
            i++;
        }
        return markers;
    }

    private static String[] processMarkedLines(String[] lines, char[] markers) {
    }

    /**
     * Returns Matcher object if provided string is a splitter and
     * None otherwise.
     */
    private static Matcher isSplitter(String line) {
        for (Pattern pattern : SPLITTER_PATTERNS) {
            Matcher matcher = pattern.matcher(line);
            if (matcher.find() && matcher.start() == 0) {
                return matcher;
            }
        }
        return null;
    }

    public static String preprocess(String body, String delimiter) {
        return preprocess(body, delimiter, "text/plain");
    }

    /**
     * Normalize links i.e. replace '<', '>' wrapping the link with some symbols
     * so that '>' closing the link couldn't be mistakenly taken for quotation
     * marker.
     */
    private static class LinkWrapper implements CallbackMatcher.ReplaceCallback {
        private String body;

        public LinkWrapper(String body) {
            this.body = body;
        }

        @Override
        public String replaceMatch(MatchResult link) {
            int newlineIndex = body.substring(link.start(), body.length()).lastIndexOf("\n");
            if (body.charAt(newlineIndex + 1) == '>') {
                return link.group();
            } else {
                return String.format("@@%s@@", link.group(1));
            }
        }
    }

    /**
     * Wraps splitter with new line
     */
    private static class SplitterWrapper implements CallbackMatcher.ReplaceCallback {
        private String body;
        private String delimiter;

        public SplitterWrapper(String body, String delimiter) {
            this.body = body;
            this.delimiter = delimiter;
        }

        @Override
        public String replaceMatch(MatchResult splitter) {
            if (splitter.start() > 0 && body.charAt(splitter.start() - 1) != '\n') {
                return String.format("%s%s", delimiter, splitter.group());
            } else {
                return splitter.group();
            }
        }
    }

    /**
     * Prepares msg_body for being stripped.
     * <p>
     * Replaces link brackets so that they couldn't be taken for quotation marker.
     * Splits line in two if splitter pattern preceded by some text on the same
     * line (done only for 'On <date> <person> wrote:' pattern).
     */
    static String preprocess(String body, String delimiter, String contentType) {
        body = new CallbackMatcher(RE_LINK).replaceMatches(body, new LinkWrapper(body));
        if (Objects.equals(contentType, "text/plain")) {
            body = new CallbackMatcher(RE_ON_DATE_SMB_WROTE).replaceMatches(body, new LinkWrapper(body));
        }
        return body;
    }

    /**
     * Make up for changes done at preprocessing message.
     * <p>
     * Replace link brackets back to '<' and '>'.
     */
    static String postprocess(String body) {
        return new CallbackMatcher(RE_NORMALIZED_LINK).replaceMatches(body, new CallbackMatcher.ReplaceCallback() {
            @Override
            public String replaceMatch(MatchResult matchResult) {
                return "<" + matchResult.group(1) + ">";
            }
        }).trim();
    }
}

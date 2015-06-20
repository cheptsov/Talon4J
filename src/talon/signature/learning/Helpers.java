package talon.signature.learning;

import java.util.*;
import java.util.regex.Pattern;

/**
 * The module provides:
 * functions used when evaluating signature's features
 * regexp's constants used when evaluating signature's features
 */
public class Helpers {
    public static final Pattern RE_EMAIL = Pattern.compile("@");
    public static final Pattern RE_RELAX_PHONE = Pattern.compile(".*(\\(? ?[\\d]{2,3} ?\\)?.{0,3}){2,}");
    public static final Pattern RE_URL = Pattern.compile("https?://|www\\.[\\S]+\\.[\\S]");
    public static final Pattern RE_SEPARATOR = Pattern.compile("^[\\s]*---*[\\s]*$");
    public static final Pattern RE_SPECIAL_CHARS = Pattern.compile("^[\\s]*([\\*]|#|[\\+]|[\\^]|-|[\\~]|[\\&]|[///]|[\\$]|_|[\\!]|[\\/]|[\\%]|[\\:]|[\\=]){10,}[\\s]*$");
    public static final Pattern RE_SIGNATURE_WORDS = Pattern.compile("(T|t)hank.*,|(B|b)est|(R|r)egards|^sent[ ]{1}from[ ]{1}my[\\s,!\\w]*$|BR|(S|s)incerely|(C|c)orporation|Group");
    public static final Pattern RE_NAME = Pattern.compile("[A-Z][a-z]+\\s\\s?[A-Z][\\.]?\\s\\s?[A-Z][a-z]+");

    public static final Pattern RE_SENDER_WITH_NAME = Pattern.compile("[^<]+<.*>.*"); // "([\\s]*[\\S]+,?)+[\\s]*<.*>.*");
    public static final Pattern RE_CLUE_LINE_END = Pattern.compile(".*(W|w)rotes?:$");

    public static final Pattern INVALID_WORD_START = Pattern.compile("^\\(|\\+|[\\d].*$");

    private static final Set<String> BAD_SENDER_NAMES = new HashSet<>(Arrays.asList("hotmail",
            "gmail", "yandex", "mail", "yahoo", "mailgun", "mailgunhq", "example", "com", "org",
            "net", "ru", "mailto"));

    public static int SIGNATURE_MAX_LINES = 11;
    public static int TOO_LONG_SIGNATURE_LINE = 60;


    /**
     * Tries to extract sender's names from `From:` header.
     * <p>
     * It could extract not only the actual names but e.g.
     * the name of the company, parts of email, etc.
     */
    public static Set<String> extractNames(String sender) {
        Set<String> set = new HashSet<>();
        // Remove non-alphabetical characters
        for (String word : sender.split("[^a-zA-Z]+")) {
            // Remove too short words and words from "black" list i.e.
            // words like `ru`, `gmail`, `com`, `org`, etc.
            if (word.length() > 1 && !BAD_SENDER_NAMES.contains(word)) {
                set.add(word);
            }
        }
        return set;
    }

    /**
     * Searches sender's name or it's part.
     */
    public static boolean containsSenderNames(String text, String sender) {
        StringBuilder sb = new StringBuilder();
        for (String name : extractNames(sender)) {
            if (sb.length() > 0)
                sb.append("( |$)");
            sb.append(name).append("|").append(capitalize(name));
        }
        return sb.length() > 0 && Pattern.compile(sb.toString()).matcher(text).find();
    }

    private static String capitalize(final String line) {
        return Character.toUpperCase(line.charAt(0)) + line.substring(1);
    }

    /**
     * Returns true if percentage of capitalized words is greater then 66% and false otherwise.
     */
    public static boolean manyCapitalizedWords(String text) {
        return capitalizedWordsPercent(text) > 66;
    }

    /**
     * Returns capitalized words percent.
     */
    static int capitalizedWordsPercent(String text) {
        int capitalizedWordsCounter = 0;
        int validWordsCounter = 0;
        String[] words = text.split("\\s+");
        for (String word : words) {
            if (!INVALID_WORD_START.matcher(word).matches()) {
                validWordsCounter++;
                if (Character.isUpperCase(word.charAt(0))) {
                    capitalizedWordsCounter++;
                }
            }
        }
        if (validWordsCounter > 0 && words.length > 1)
            return 100 * capitalizedWordsCounter / validWordsCounter;
        else return 0;
    }

    /**
     * Returns category characters percent.
     */
    static int categoriesPercent(String text, int... categories) {
        int count = 0;
        for (int i = 0; i < text.length(); i++) {
            for (int category : categories) {
                if (Character.getType(text.charAt(i)) == category) {
                    count++;
                }
            }
        }
        if (text.length() > 0)
            return 100 * count / text.length();
        else
            return 0;
    }

    /**
     * Returns punctuation percent.
     */
    public static int punctuationPercent(String text) {
        return categoriesPercent(text, Character.OTHER_PUNCTUATION);
    }

    /**
     * Checks if the body has signature. Returns True or False.
     */
    public static boolean hasSignature(String body, String sender) {
        List<String> lines = new ArrayList<>();
        for (String line : body.split("\r?\n")) {
            if (!line.trim().isEmpty()) {
                lines.add(line);
            }
        }
        lines = lines.subList(Math.max(0, lines.size() - Helpers.SIGNATURE_MAX_LINES), lines.size());
        int upvotes = 0;
        for (String line : lines) {
            // we check lines for sender's name, phone, email and url,
            // those signature lines don't take more then 27 lines
            if (line.trim().length() <= 27) {
                if (containsSenderNames(line, sender)) {
                    return true;
                } else {
                    if ((RE_RELAX_PHONE.matcher(line).find() ? 1 : 0) +
                            (RE_EMAIL.matcher(line).find() ? 1 : 0) + (RE_URL.matcher(line).find() ? 1 : 0) == 1) {
                        upvotes++;
                    }
                }
            }
        }
        return upvotes > 1;
    }

    interface Feature {
        boolean apply(String text);
    }

    static class SearchFeature implements Feature {
        private Pattern pattern;

        protected SearchFeature(Pattern pattern) {
            this.pattern = pattern;
        }

        @Override
        public boolean apply(String text) {
            return pattern.matcher(text).find();
        }
    }

}

package talon.signature.learning;

import java.util.*;
import java.util.regex.Pattern;

public class Helpers {
    public static final Pattern RE_EMAIL = Pattern.compile("@");
    public static final Pattern RE_RELAX_PHONE = Pattern.compile(".*(\\(? ?[\\d]{2,3} ?\\)?.{0,3}){2,}");
    public static final Pattern RE_URL = Pattern.compile("https?://|www\\.[\\S]+\\.[\\S]");
    public static final Pattern RE_SEPARATOR = Pattern.compile("^[\\s]*---*[\\s]*$");
    public static final Pattern RE_SPECIAL_CHARS = Pattern.compile("^[\\s]*([\\*]|#|[\\+]|[\\^]|-|[\\~]|[\\&]|[///]|[\\$]|_|[\\!]|[\\/]|[\\%]|[\\:]|[\\=]){10,}[\\s]*$");
    public static final Pattern RE_SIGNATURE_WORDS = Pattern.compile("(T|t)hank.*,|(B|b)est|(R|r)egards|^sent[ ]{1}from[ ]{1}my[\\s,!\\w]*$|BR|(S|s)incerely|(C|c)orporation|Group");
    public static final Pattern RE_NAME = Pattern.compile("[A-Z][a-z]+\\s\\s?[A-Z][\\.]?\\s\\s?[A-Z][a-z]+");

    public static final Pattern SENDER_WITH_NAME_PATTERN = Pattern.compile("([\\s]*[\\S]+,?)+[\\s]*<.*>.*");
    public static final Pattern RE_CLUE_LINE_END = Pattern.compile(".*(W|w)rotes?:$");

    public static final Pattern INVALID_WORD_START = Pattern.compile("^\\(|\\+|[\\d].*$");

    private static final Set<String> BAD_SENDER_NAMES = new HashSet<>(Arrays.asList("hotmail",
            "gmail", "yandex", "mail", "yahoo", "mailgun", "mailgunhq", "example", "com", "org",
            "net", "ru", "mailto"));

    public static final int SIGNATURE_MAX_LINES = 11;
    public static final int TOO_LONG_SIGNATURE_LINE = 60;


    /**
     * Tries to extract sender's names from `From:` header.
     * <p/>
     * It could extract not only the actual names but e.g.
     * the name of the company, parts of email, etc.
     */
    public static Set<String> extractNames(String sender) {
        Set<String> set = new HashSet<>();
        // Remove non-alphabetical characters
        for (String word : sender.split("[^a-zA-Z\\d]+")) {
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
    public static Boolean containsSenderNames(String text, String sender) {
        StringBuilder sb = new StringBuilder();
        for (String name : extractNames(sender)) {
            if (sb.length() > 0)
                sb.append("|");
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
    public static Boolean manyCapitalizedWords(String text) {
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

    interface Feature {
        Boolean apply(String text);
    }

    static class SearchFeature implements Feature {
        private Pattern pattern;

        protected SearchFeature(Pattern pattern) {
            this.pattern = pattern;
        }

        @Override
        public Boolean apply(String text) {
            return pattern.matcher(text).find();
        }
    }

    /**
     * Returns a list of signature features.
     */
    public static Feature[] features(final String sender) {
        return new Feature[]{
                // Matches companies names, sender's names, address.
                new Feature() {
                    @Override
                    public Boolean apply(String text) {
                        return manyCapitalizedWords(text);
                    }
                },
                // Line is too long.
                new Feature() {
                    @Override
                    public Boolean apply(String text) {
                        return text.length() > TOO_LONG_SIGNATURE_LINE;
                    }
                },
                // Line contains email pattern.
                new SearchFeature(RE_EMAIL),
                // Line contains url.
                new SearchFeature(RE_URL),
                // Line contains phone number pattern.
                new SearchFeature(RE_RELAX_PHONE),
                // Line matches the regular expression "^[\s]*---*[\s]*$".
                new SearchFeature(RE_SEPARATOR),
                // Line has a sequence of 10 or more special characters.
                new SearchFeature(RE_SPECIAL_CHARS),
                // Line contains any typical signature words.
                new SearchFeature(RE_SIGNATURE_WORDS),
                // Line contains a pattern like Vitor R. Carvalho or William W. Cohen.
                new SearchFeature(RE_NAME),
                // Percentage of punctuation symbols in the line is larger than 50%
                new Feature() {
                    @Override
                    public Boolean apply(String text) {
                        return punctuationPercent(text) > 50;
                    }
                },
                // Percentage of punctuation symbols in the line is larger than 90%
                new Feature() {
                    @Override
                    public Boolean apply(String text) {
                        return punctuationPercent(text) > 50;
                    }
                },
                new Feature() {
                    @Override
                    public Boolean apply(String text) {
                        return containsSenderNames(text, sender);
                    }
                }
        };
    }

    /**
     * Applies features to message body lines.
     *
     * Returns list of lists. Each of the lists corresponds to the body line
     * and is constituted by the numbers of features occurrences (false or true).
     *
     * E.g. if element j of list i equals true this means that
     * feature j occurred in line i (counting from the last line of the body).
     */
    public static Boolean[][] applyFeatures(String body, Feature[] features) {
        List<String> lines = new ArrayList<>();
        // collect all non empty lines
        for (String line : body.split("\n")) {
            String trimmed = line.trim();
            if (trimmed.length() > 0) {
                lines.add(trimmed);
            }
        }
        // take the last SIGNATURE_MAX_LINES
        lines = lines.subList(Math.max(0, lines.size() - SIGNATURE_MAX_LINES), lines.size());
        // apply features, fallback to zeros
        Boolean[][] results = new Boolean[lines.size()][features.length];
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            for (int j = 0; j < features.length; j++) {
                Feature feature = features[j];
                results[i][j] = feature.apply(line);
            }
        }
        return results;
    }

    public static int[] buildPattern(String body, Feature[] features) {
        int[] pattern = new int[features.length];
        Boolean[][] results = applyFeatures(body, features);
        for (int i = 0; i < results.length; i++) {
            for (int j = 0; j < results[i].length; j++) {
                pattern[j] += results[i][j] ? 1 : 0;
            }
        }
        return pattern;
    }
}

package talon.signature.learning;

import java.util.ArrayList;
import java.util.List;

/**
 * The module provides functions for conversion of a message body/body lines
 * into classifiers features space.
 * <p>
 * The body and the message sender string are converted into unicode before
 * applying features to them.
 */
public class FeatureSpace {
    /**
     * Returns a list of signature features.
     */
    public static Helpers.Feature[] features(final String sender) {
        return new Helpers.Feature[]{
                // Matches companies names, sender's names, address.
                new Helpers.Feature() {
                    @Override
                    public boolean apply(String text) {
                        return Helpers.manyCapitalizedWords(text);
                    }
                },
                // Line is too long.
                new Helpers.Feature() {
                    @Override
                    public boolean apply(String text) {
                        return text.length() > Helpers.TOO_LONG_SIGNATURE_LINE;
                    }
                },
                // Line contains email pattern.
                new Helpers.SearchFeature(Helpers.RE_EMAIL),
                // Line contains url.
                new Helpers.SearchFeature(Helpers.RE_URL),
                // Line contains phone number pattern.
                new Helpers.SearchFeature(Helpers.RE_RELAX_PHONE),
                // Line matches the regular expression "^[\s]*---*[\s]*$".
                new Helpers.SearchFeature(Helpers.RE_SEPARATOR),
                // Line has a sequence of 10 or more special characters.
                new Helpers.SearchFeature(Helpers.RE_SPECIAL_CHARS),
                // Line contains any typical signature words.
                new Helpers.SearchFeature(Helpers.RE_SIGNATURE_WORDS),
                // Line contains a pattern like Vitor R. Carvalho or William W. Cohen.
                new Helpers.SearchFeature(Helpers.RE_NAME),
                // Percentage of punctuation symbols in the line is larger than 50%
                new Helpers.Feature() {
                    @Override
                    public boolean apply(String text) {
                        return Helpers.punctuationPercent(text) > 50;
                    }
                },
                // Percentage of punctuation symbols in the line is larger than 90%
                new Helpers.Feature() {
                    @Override
                    public boolean apply(String text) {
                        return Helpers.punctuationPercent(text) > 50;
                    }
                },
                new Helpers.Feature() {
                    @Override
                    public boolean apply(String text) {
                        return Helpers.containsSenderNames(text, sender);
                    }
                }
        };
    }

    /**
     * Applies features to message body lines.
     * <p>
     * Returns list of lists. Each of the lists corresponds to the body line
     * and is constituted by the numbers of features occurrences (false or true).
     * <p>
     * E.g. if element j of list i equals true this means that
     * feature j occurred in line i (counting from the last line of the body).
     */
    public static boolean[][] applyFeatures(String body, Helpers.Feature[] features) {
        List<String> lines = new ArrayList<>();
        // collect all non empty lines
        for (String line : body.split("\r?\n")) {
            String trimmed = line.trim();
            if (trimmed.length() > 0) {
                lines.add(trimmed);
            }
        }
        // take the last SIGNATURE_MAX_LINES
        lines = lines.subList(Math.max(0, lines.size() - Helpers.SIGNATURE_MAX_LINES), lines.size());
        // apply features, fallback to zeros
        boolean[][] results = new boolean[lines.size()][features.length];
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            for (int j = 0; j < features.length; j++) {
                Helpers.Feature feature = features[j];
                results[i][j] = feature.apply(line);
            }
        }
        return results;
    }

    public static int[] buildPattern(String body, Helpers.Feature[] features) {
        int[] pattern = new int[features.length];
        boolean[][] results = applyFeatures(body, features);
        for (boolean[] result : results) {
            for (int j = 0; j < result.length; j++) {
                pattern[j] += result[j] ? 1 : 0;
            }
        }
        return pattern;
    }
}

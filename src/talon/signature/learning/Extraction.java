package talon.signature.learning;

import talon.Utils;
import talon.signature.Bruteforce;
import weka.core.Instance;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Extraction {
    static final Pattern RE_REVERSE_SIGNATURE = Pattern.compile("(e*(te*){0,2}s)+");

    static boolean isSignatureLine(String line, String sender, Classifier classifier) {
        Instance instance = new Instance(13);
        int[] pattern = FeatureSpace.buildPattern(line, FeatureSpace.features(sender));
        instance.setValue(Classifier.MANY_CAPITALIZED_WORDS, pattern[0]);
        instance.setValue(Classifier.TOO_LONG_SIGNATURE_LINE, pattern[1]);
        instance.setValue(Classifier.RE_EMAIL, pattern[2]);
        instance.setValue(Classifier.RE_URL, pattern[3]);
        instance.setValue(Classifier.RE_RELAX_PHONE, pattern[4]);
        instance.setValue(Classifier.RE_SEPARATOR, pattern[5]);
        instance.setValue(Classifier.RE_SPECIAL_CHARS, pattern[6]);
        instance.setValue(Classifier.RE_SIGNATURE_WORDS, pattern[7]);
        instance.setValue(Classifier.RE_NAME, pattern[8]);
        instance.setValue(Classifier.PUNCTUATION_PERCENT_IS_HIGH, pattern[9]);
        instance.setValue(Classifier.PUNCTUATION_PERCENT_IS_VERY_HIGH, pattern[10]);
        instance.setValue(Classifier.CONTAINS_SENDER_NAMES, pattern[11]);
        instance.setDataset(classifier.data);
        double[] distribution;
        try {
            distribution = classifier.libSVM.distributionForInstance(instance);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return distribution[0] < distribution[1];
    }

    /**
     * Strips signature from the body of the message.
     * <p>
     * Returns stripped body and signature as a tuple.
     * If no signature is found the corresponding returned value is None.
     */
    public static ExtractedSignature extract(String body, String sender, Classifier classifier) {
        String delimiter = Utils.getDelimiter(body);

        body = body.trim();

        if (Helpers.hasSignature(body, sender)) {
            String[] lines = body.split("\r?\n");
            char[] markers = markLines(lines, sender, classifier);
            MarkedLines ml = processMarkedLines(lines, markers);

            if (ml.signature.length > 0) {
                String text = join(ml.text, delimiter);
                if (!text.trim().isEmpty()) {
                    return new ExtractedSignature(text, join(ml.signature, delimiter));
                }
            }
        }
        return new ExtractedSignature(body, "");
    }

    static String join(String[] string, String delimiter) {
        StringBuilder sbStr = new StringBuilder();
        for (int i = 0, il = string.length; i < il; i++) {
            if (i > 0)
                sbStr.append(delimiter);
            sbStr.append(string[i]);
        }
        return sbStr.toString();
    }

    public static class ExtractedSignature {
        final String text;
        final String signature;

        public ExtractedSignature(String text, String signature) {
            this.text = text;
            this.signature = signature;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ExtractedSignature that = (ExtractedSignature) o;

            if (!text.equals(that.text)) return false;
            return signature.equals(that.signature);

        }

        @Override
        public int hashCode() {
            int result = text.hashCode();
            result = 31 * result + signature.hashCode();
            return result;
        }

        @Override
        public String toString() {
            return "ExtractedSignature{" +
                    "text='" + text + '\'' +
                    ", signature='" + signature + '\'' +
                    '}';
        }
    }

    /**
     * Mark message lines with markers to distinguish signature lines.
     * <p>
     * Markers:
     * <p>
     * e - empty line<br>
     * s - line identified as signature<br>
     * t - other i.e. ordinary text line
     */
    static char[] markLines(String[] lines, String sender, Classifier classifier) {
        String[] candidate = Bruteforce.getSignatureCandidate(lines);
        // at first consider everything to be text no signature
        char[] markers = new char[lines.length];
        Arrays.fill(markers, 't');
        // mark lines starting from bottom up
        // mark only lines that belong to candidate
        // no need to mark all lines of the message
        String[] reversedCandidate = Arrays.copyOf(candidate, candidate.length);
        int[] reversedIndexes = new int[reversedCandidate.length];
        for (int i = 0; i < reversedIndexes.length; i++) reversedIndexes[i] = i;
        reverseArray(reversedCandidate, reversedIndexes);
        for (int i = 0; i < reversedCandidate.length; i++) {
            String line = reversedCandidate[i];
            // markers correspond to lines not candidate
            // so we need to recalculate our index to be
            // relative to lines not candidate
            int j = lines.length - candidate.length + reversedIndexes[i];
            if (line.trim().isEmpty()) {
                markers[j] = 'e';
            } else if (isSignatureLine(line, sender, classifier)) {
                markers[j] = 's';
            }
        }
        return markers;
    }

    private static void reverseArray(String[] array, int[] indexes) {
        for(int i = 0; i < array.length / 2; i++) {
            String temp = array[i];
            int tempIds = indexes[i];
            array[i] = array[array.length - i - 1];
            indexes[i] = indexes[indexes.length - i - 1];
            array[array.length - i - 1] = temp;
            indexes[indexes.length - i - 1] = tempIds;
        }
    }

    static MarkedLines processMarkedLines(String[] lines, char[] markers) {
        char[] reveredMarkers = Arrays.copyOf(markers, markers.length);
        reverseArray(reveredMarkers);
        Matcher matcher = RE_REVERSE_SIGNATURE.matcher(new String(reveredMarkers));
        if (matcher.find() && matcher.start() == 0) {
            return new MarkedLines(Arrays.asList(lines).subList(0, lines.length - matcher.end()).toArray(new String[0]),
                    Arrays.asList(lines).subList(Math.max(0, lines.length - matcher.end()), lines.length).toArray(new String[0]));
        } else {
            return new MarkedLines(lines, new String[0]);
        }
    }

    private static void reverseArray(char[] array) {
        for(int i = 0; i < array.length / 2; i++) {
            char temp = array[i];
            array[i] = array[array.length - i - 1];
            array[array.length - i - 1] = temp;
        }
    }

    public static class MarkedLines {
        final String[] text;
        final String[] signature;

        public MarkedLines(String[] text, String[] signature) {
            this.text = text;
            this.signature = signature;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof MarkedLines) {
                MarkedLines m = (MarkedLines) obj;
                if (text.length != m.text.length)
                    return false;
                if (signature.length != m.signature.length)
                    return false;
                for (int i = 0; i < text.length; i++) {
                    if (!text[i].equals(m.text[i]))
                        return false;
                }
                for (int i = 0; i < signature.length; i++) {
                    if (!signature[i].equals(m.signature[i]))
                        return false;
                }
                return true;
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            int result = Arrays.hashCode(text);
            result = 31 * result + Arrays.hashCode(signature);
            return result;
        }

        @Override
        public String toString() {
            return "[" + Arrays.asList(text) + ", " + Arrays.asList(signature) + "]";
        }
    }
}

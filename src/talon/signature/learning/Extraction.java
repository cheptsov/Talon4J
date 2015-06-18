package talon.signature.learning;

import talon.Utils;
import weka.core.Instance;

public class Extraction {
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
    public void extract(String body, String sender) {
        String delimiter = Utils.getDelimiter(body);

        body = body.trim();

        if (Helpers.hasSignature(body, sender)) {
            String[] lines = body.split("\n");
            // TODO
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
    static final String markLines(String[] lines, String sender) {
        // TODO
        return "";
    }
}

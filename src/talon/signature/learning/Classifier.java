package talon.signature.learning;

import weka.classifiers.functions.LibSVM;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

import java.io.*;

/**
 * The module's functions could init, train, save and load a classifier.
 * The classifier could be used to detect if a certain line of the message
 * body belongs to the signature.
 */
public class Classifier {
    public LibSVM libSVM;
    public Instances data;

    public Classifier(LibSVM libSVM, Instances data) {
        this.libSVM = libSVM;
        this.data = data;
    }

    public final static Attribute MANY_CAPITALIZED_WORDS = new Attribute("many_capitalized_words");
    public final static Attribute TOO_LONG_SIGNATURE_LINE = new Attribute("too_long_signature_line");
    public final static Attribute RE_EMAIL = new Attribute("re_email");
    public final static Attribute RE_URL = new Attribute("re_url");
    public final static Attribute RE_RELAX_PHONE = new Attribute("re_relax_phone");
    public final static Attribute RE_SEPARATOR = new Attribute("re_separator");
    public final static Attribute RE_SPECIAL_CHARS = new Attribute("re_special_chars");
    public final static Attribute RE_SIGNATURE_WORDS = new Attribute("re_signature_words");
    public final static Attribute RE_NAME = new Attribute("re_name");
    public final static Attribute PUNCTUATION_PERCENT_IS_HIGH = new Attribute("punctuation_percent_is_high");
    public final static Attribute PUNCTUATION_PERCENT_IS_VERY_HIGH = new Attribute("punctuation_percent_is_very_high");
    public final static Attribute CONTAINS_SENDER_NAMES = new Attribute("contains_sender_names");
    public final static Attribute SIGNATURE_CLASS;

    static {
        final FastVector SIGNATURE_CLASS_ATTRIBUTES = new FastVector();
        SIGNATURE_CLASS_ATTRIBUTES.addElement("false");
        SIGNATURE_CLASS_ATTRIBUTES.addElement("true");

        SIGNATURE_CLASS = new Attribute("target", SIGNATURE_CLASS_ATTRIBUTES);
    }



    public static Classifier init() {
        FastVector attributes = new FastVector();
        attributes.addElement(MANY_CAPITALIZED_WORDS);
        attributes.addElement(TOO_LONG_SIGNATURE_LINE);
        attributes.addElement(RE_EMAIL);
        attributes.addElement(RE_URL);
        attributes.addElement(RE_RELAX_PHONE);
        attributes.addElement(RE_SEPARATOR);
        attributes.addElement(RE_SPECIAL_CHARS);
        attributes.addElement(RE_SIGNATURE_WORDS);
        attributes.addElement(RE_NAME);
        attributes.addElement(PUNCTUATION_PERCENT_IS_HIGH);
        attributes.addElement(PUNCTUATION_PERCENT_IS_VERY_HIGH);
        attributes.addElement(CONTAINS_SENDER_NAMES);
        attributes.addElement(SIGNATURE_CLASS);

        Instances data = new Instances("Talon", attributes, 100);
        data.setClassIndex(data.numAttributes() - 1);
        LibSVM libSVM = new LibSVM();
        libSVM.setCost(10);
        return new Classifier(libSVM, data);
    }

    public static void train(Classifier classifier) throws Exception {
        InputStream is = Classifier.class.getClassLoader().getResourceAsStream("talon/signature/data/train.data.txt");
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                Instance instance = new Instance(13);
                instance.setValue(MANY_CAPITALIZED_WORDS, Integer.parseInt(values[0]));
                instance.setValue(TOO_LONG_SIGNATURE_LINE, Integer.parseInt(values[1]));
                instance.setValue(RE_EMAIL, Integer.parseInt(values[2]));
                instance.setValue(RE_URL, Integer.parseInt(values[3]));
                instance.setValue(RE_RELAX_PHONE, Integer.parseInt(values[4]));
                instance.setValue(RE_SEPARATOR, Integer.parseInt(values[5]));
                instance.setValue(RE_SPECIAL_CHARS, Integer.parseInt(values[6]));
                instance.setValue(RE_SIGNATURE_WORDS, Integer.parseInt(values[7]));
                instance.setValue(RE_NAME, Integer.parseInt(values[8]));
                instance.setValue(PUNCTUATION_PERCENT_IS_HIGH, Integer.parseInt(values[9]));
                instance.setValue(PUNCTUATION_PERCENT_IS_VERY_HIGH, Integer.parseInt(values[10]));
                instance.setValue(CONTAINS_SENDER_NAMES, Integer.parseInt(values[11]));
                int sc = Integer.parseInt(values[12]);
                instance.setValue(SIGNATURE_CLASS, sc == 1 ? "true" : "false");
                instance.setDataset(classifier.data);
                classifier.data.add(instance);
            }
        }

        classifier.libSVM.buildClassifier(classifier.data);
    }
}

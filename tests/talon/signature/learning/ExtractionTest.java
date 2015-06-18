package talon.signature.learning;

import org.junit.Test;

public class ExtractionTest {
    @Test
    public void testIsSignatureLine() throws Exception {
        Classifier classifier = Classifier.init();
        Classifier.train(classifier);
        System.out.println(Extraction.isSignatureLine("JetBrains, Inc.", "Andrey Cheptsov <andrey.cheptsov@gmail.com>", classifier));
    }
}

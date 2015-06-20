package talon.signature.learning;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class ExtractionTest {
    @Test
    public void testMarkLines() throws Exception {
        Classifier classifier = Classifier.init();
        Classifier.train(classifier);
        //assertArrayEquals("tes".toCharArray(), Extraction.markLines(new String[]{"Some text", "", "Bob"}, "Bob", classifier));

        // we analyse the 2nd line as well though it's the 6th line
        // (starting from the bottom) because we don't count empty line
        int saveValue = Helpers.SIGNATURE_MAX_LINES;
        Helpers.SIGNATURE_MAX_LINES = 2;
        assertArrayEquals("ttset".toCharArray(), Extraction.markLines(new String[]{"Bob Smith", "Bob Smith", "Bob Smith", "", "some text"}, "Bob Smith", classifier));

        Helpers.SIGNATURE_MAX_LINES = 3;
        // we don't analyse the 1st line because
        // signature cant start from the 1st line
        assertArrayEquals("tset".toCharArray(), Extraction.markLines(new String[]{"Bob Smith", "Bob Smith", "", "some text"}, "Bob Smith", classifier));

        Helpers.SIGNATURE_MAX_LINES = saveValue;
    }

    @Test
    public void testProcessMarkedLines() throws Exception {
        assertEquals(new Extraction.MarkedLines(new String[]{"Some text", ""}, new String[]{"Bob"}),
                Extraction.processMarkedLines(new String[]{"Some text", "", "Bob"}, "tes".toCharArray()));
    }

    @Test
    public void testBasic() throws Exception {
        Classifier classifier = Classifier.init();
        Classifier.train(classifier);
        assertEquals(new Extraction.ExtractedSignature("Blah", "--\r\n\r\nSergey Obukhov"), Extraction.extract("Blah\r\n--\r\n\r\nSergey Obukhov", "Sergey", classifier));
    }
}

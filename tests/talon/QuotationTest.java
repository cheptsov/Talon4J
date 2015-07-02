package talon;

import org.junit.Test;

import static org.junit.Assert.*;

public class QuotationTest {

    @Test
    public void testMarkMessageLines() throws Exception {
        assertArrayEquals("tsem".toCharArray(), Quotation.markMessageLines(new String[]{"answer", "From: foo@bar.com", "", "> question"}));
    }
}
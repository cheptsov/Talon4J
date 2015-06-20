package talon.signature;

import org.junit.Test;
import talon.signature.learning.Helpers;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class BruteforceTest {
    @Test
    public void testMarkCandidateIndexes() throws Exception {
        assertArrayEquals("cdc".toCharArray(), Bruteforce.markCandidateIndexes(new String[]{"Some text", "", "- t", "Bob"}, Arrays.asList(0, 2, 3)));
        int saveValue = Helpers.TOO_LONG_SIGNATURE_LINE;
        Helpers.TOO_LONG_SIGNATURE_LINE = 3;
        // spaces are not considered when checking line length
        assertArrayEquals("clc".toCharArray(), Bruteforce.markCandidateIndexes(new String[]{"BR,  ", "long", "Bob"}, Arrays.asList(0, 1, 2)));
        // only candidate lines are marked
        // if line has only dashes it's a candidate line
        assertArrayEquals("ccdc".toCharArray(), Bruteforce.markCandidateIndexes(new String[]{"-", "long", "-", "- i", "Bob"}, Arrays.asList(0, 2, 3, 4)));
        Helpers.TOO_LONG_SIGNATURE_LINE = saveValue;
    }

    @Test
    public void testProcessMarkedCandidateIndexes() throws Exception {
        assertEquals(Arrays.asList(2, 13, 15), Bruteforce.processMarkedCandidateIndexes(Arrays.asList(2, 13, 15), "dcc".toCharArray()));
        assertEquals(Collections.singletonList(15), Bruteforce.processMarkedCandidateIndexes(Arrays.asList(2, 13, 15), "ddc".toCharArray()));
        assertEquals(Arrays.asList(13, 15), Bruteforce.processMarkedCandidateIndexes(Arrays.asList(13, 15), "cc".toCharArray()));
        assertEquals(Collections.singletonList(15), Bruteforce.processMarkedCandidateIndexes(Collections.singletonList(15), "lc".toCharArray()));
        assertEquals(Collections.singletonList(15), Bruteforce.processMarkedCandidateIndexes(Arrays.asList(13, 15), "ld".toCharArray()));
    }

    @Test
    public void testGetSignature() throws Exception {
        // if there aren't at least 2 non-empty lines there should be no signature
        for (String[] lines : new String[][]{new String[0], new String[]{""},
                new String[]{"", ""}, new String[]{"abc"}}) {
            assertArrayEquals(new String[0], Bruteforce.getSignatureCandidate(lines));
        }

        // first line never included
        assertArrayEquals(new String[]{"signature"}, Bruteforce.getSignatureCandidate(new String[]{"text", "signature"}));

        // test when message is shorter then SIGNATURE_MAX_LINES
        int saveValue = Helpers.SIGNATURE_MAX_LINES;
        Helpers.SIGNATURE_MAX_LINES = 3;
        assertArrayEquals(new String[]{"signature"}, Bruteforce.getSignatureCandidate(new String[]{"text", "", "", "signature"}));

        // test when message is longer then the SIGNATURE_MAX_LINES
        Helpers.SIGNATURE_MAX_LINES = 2;
        assertArrayEquals(new String[]{"signature1", "", "signature2"}, Bruteforce.getSignatureCandidate(new String[]{"text1", "text2", "signature1", "", "signature2"}));
        Helpers.SIGNATURE_MAX_LINES = saveValue;

        saveValue = Helpers.TOO_LONG_SIGNATURE_LINE;
        Helpers.TOO_LONG_SIGNATURE_LINE = 3;
        assertArrayEquals(new String[]{"Bob"}, Bruteforce.getSignatureCandidate(new String[]{"BR", "long", "Bob"}));

        Helpers.TOO_LONG_SIGNATURE_LINE = saveValue;

        // test list (with dashes as bullet points) not included
        assertArrayEquals(new String[]{"--", "Bob"}, Bruteforce.getSignatureCandidate(new String[]{"List:", "- item 1", "- item 2", "--", "Bob"}));
    }
}

package talon.signature.learning;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

public class HelpersTest {

    @Test
    public void testExtractNames() throws Exception {
        Set<String> test1 = Helpers.extractNames("Sergey N.  Obukhov <serobnic@mail.ru>");
        assertEquals(new HashSet<>(Arrays.asList("Sergey", "Obukhov", "serobnic")), test1);

        Set<String> test2 = Helpers.extractNames("");
        assertEquals(new HashSet<>(), test2);
    }

    @Test
    public void testContainsSenderNames() throws Exception {
        assertTrue(Helpers.containsSenderNames("Sergey Obukhov", "Sergey N.  Obukhov <xxx@example.com>"));
        assertTrue(Helpers.containsSenderNames("BR, Sergey N.", "Sergey N.  Obukhov <xxx@example.com>"));
        assertTrue(Helpers.containsSenderNames("Serobnic", "<serobnic@mail.ru>"));
        assertTrue(Helpers.containsSenderNames("serobnic", "<serobnic@mail.ru>"));
    }

    @Test
    public void testManyCapitalizedWords() throws Exception {
        assertEquals(50, Helpers.capitalizedWordsPercent("Aaaa Bbbb cccc dddd"));
    }

    @Test
    public void testManyCapitalizedWords1() throws Exception {
        assertTrue(Helpers.manyCapitalizedWords("Aaaa Bbbb Cccc dddd"));
        assertFalse(Helpers.manyCapitalizedWords("Aaaa Bbbb cccc dddd"));
    }

    @Test
    public void testCategoriesPercent() throws Exception {
        assertEquals(0, Helpers.categoriesPercent("qqq ggg hhh", Character.OTHER_PUNCTUATION));
        assertEquals(50, Helpers.categoriesPercent("q,w.", Character.OTHER_PUNCTUATION));
        assertEquals(0, Helpers.categoriesPercent("qqq ggg hhh", Character.DECIMAL_DIGIT_NUMBER));
        assertEquals(50, Helpers.categoriesPercent("q5", Character.DECIMAL_DIGIT_NUMBER));
        assertEquals(50, Helpers.categoriesPercent("s.s,5s", Character.OTHER_PUNCTUATION, Character.DECIMAL_DIGIT_NUMBER));
    }

    @Test
    public void testPunctuationPercent() throws Exception {
        assertEquals(0, Helpers.punctuationPercent("qqq ggg hhh"));
        assertEquals(50, Helpers.punctuationPercent("q,w."));
    }

    @Test
    public void testApplyFeatures() throws Exception {
        String sender = "John <john@example.com>";
        Helpers.Feature[] features = Helpers.features(sender);
        Boolean[][] result = Helpers.applyFeatures("John Doe\n" +
                "\n" +
                "VP Research and Development, Xxxx Xxxx Xxxxx\n" +
                "\n" +
                "555-226-2345\n" +
                "\n" +
                "john@example.com", features);
        assertArrayEquals(new Boolean[][] {
                new Boolean[] {true, false, false, false, false, false, false, false, false, false, false, true},
                new Boolean[] {true, false, false, false, false, false, false, false, false, false, false, false},
                new Boolean[] {false, false, false, false, true, false, false, false, false, false, false, false},
                new Boolean[] {false, false, true, false, false, false, false, false, false, false, false, true }
        }, result);
    }
    @Test
    public void testBuildPattern() throws Exception {
        String sender = "John <john@example.com>";
        Helpers.Feature[] features = Helpers.features(sender);
        int[] pattern = Helpers.buildPattern("John Doe\n" +
                "\n" +
                "VP Research and Development, Xxxx Xxxx Xxxxx\n" +
                "\n" +
                "555-226-2345\n" +
                "\n" +
                "john@example.com", features);

        assertArrayEquals(new int[] {2, 0, 1, 0, 1, 0, 0, 0, 0, 0, 0, 2}, pattern);
    }
}
package talon.signature.learning;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

public class FeatureSpaceTest {
    @Test
    public void testApplyFeatures() throws Exception {
        String sender = "John <john@example.com>";
        Helpers.Feature[] features = FeatureSpace.features(sender);
        boolean[][] result = FeatureSpace.applyFeatures("John Doe\n" +
                "\n" +
                "VP Research and Development, Xxxx Xxxx Xxxxx\n" +
                "\n" +
                "555-226-2345\n" +
                "\n" +
                "john@example.com", features);
        assertArrayEquals(new boolean[][] {
                new boolean[] {true, false, false, false, false, false, false, false, false, false, false, true},
                new boolean[] {true, false, false, false, false, false, false, false, false, false, false, false},
                new boolean[] {false, false, false, false, true, false, false, false, false, false, false, false},
                new boolean[] {false, false, true, false, false, false, false, false, false, false, false, false }
        }, result);
    }
    @Test
    public void testBuildPattern() throws Exception {
        String sender = "John <john@example.com>";
        Helpers.Feature[] features = FeatureSpace.features(sender);
        int[] pattern = FeatureSpace.buildPattern("John Doe\n" +
                "\n" +
                "VP Research and Development, Xxxx Xxxx Xxxxx\n" +
                "\n" +
                "555-226-2345\n" +
                "\n" +
                "john@example.com", features);

        assertArrayEquals(new int[] {2, 0, 1, 0, 1, 0, 0, 0, 0, 0, 0, 1}, pattern);
    }
}

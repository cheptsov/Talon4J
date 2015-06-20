package talon.signature;

import talon.signature.learning.Helpers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Bruteforce {
    public static Pattern RE_SIGNATURE_CANDIDATE = Pattern.compile("(c+d)[^d]|(c+d)$|(c+)|(d)[^d]|(d)$");

    /**
     * Return lines that could hold signature
     * <p>
     * The lines should:
     * <p>
     * be among last SIGNATURE_MAX_LINES non-empty lines.<br>
     * not include first line<br>
     * be shorter than TOO_LONG_SIGNATURE_LINE<br>
     * not include more than one line that starts with dashes
     */
    public static String[] getSignatureCandidate(String[] lines) {
        // non empty lines indexes
        List<Integer> nonEmtpy = new ArrayList<>();
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            if (!line.trim().isEmpty()) {
                nonEmtpy.add(i);
            }
        }
        // if message is empty or just one line then there is no signature
        if (nonEmtpy.size() <= 1) {
            return new String[0];
        }
        // we don't expect signature to start at the 1st line
        List<Integer> candidate = nonEmtpy.subList(1, nonEmtpy.size());
        // signature shouldn't be longer then SIGNATURE_MAX_LINES
        candidate = candidate.subList(Math.max(0, candidate.size() - Helpers.SIGNATURE_MAX_LINES), candidate.size());
        char[] markers = markCandidateIndexes(lines, candidate);
        candidate = processMarkedCandidateIndexes(candidate, markers);
        if (candidate.size() > 0) {
            List<String> signatureCandidate = Arrays.asList(lines);
            signatureCandidate = signatureCandidate.subList(candidate.get(0), signatureCandidate.size());
            return signatureCandidate.toArray(new String[signatureCandidate.size()]);
        }
        return new String[0];
    }

    /**
     * Mark candidate indexes with markers
     * <p>
     * Markers:
     * <p>
     * c - line that could be a signature line<br>
     * l - long line<br>
     * d - line that starts with dashes but has other chars as well<br>
     */
    static char[] markCandidateIndexes(String[] lines, List<Integer> candidate) {
        char[] markers = new char[candidate.size()];
        Arrays.fill(markers, 'c');
        List<Integer> reverseCandidate = new ArrayList<>(candidate);
        int[] reverseIndexes = new int[reverseCandidate.size()];
        for (int i = 0; i < reverseIndexes.length; i++)
            reverseIndexes[i] = reverseIndexes.length - i - 1;
        Collections.reverse(reverseCandidate);
        // mark lines starting from bottom up
        for (int i = 0; i < reverseCandidate.size(); i++) {
            Integer lineIdx = reverseCandidate.get(i);
            if (lines[lineIdx].trim().length() > Helpers.TOO_LONG_SIGNATURE_LINE) {
                markers[reverseIndexes[i]] = 'l';
            } else {
                String line = lines[lineIdx].trim();
                if (line.startsWith("-") && line.replaceAll("^[-]+", "").replaceAll("[-]+$", "").length() > 0) {
                    markers[reverseIndexes[i]] = 'd';
                }
            }
        }
        return markers;
    }

    /**
     * Run regexes against candidate's marked indexes to strip
     * signature candidate.
     */
    static List<Integer> processMarkedCandidateIndexes(List<Integer> candidate, char[] markers) {
        char[] reversedMarkers = Arrays.copyOf(markers, markers.length);
        reverseArray(reversedMarkers);
        Matcher matcher = Bruteforce.RE_SIGNATURE_CANDIDATE.matcher(new String(reversedMarkers));
        if (matcher.find()) {
            return candidate.subList(Math.max(0, candidate.size() - end(matcher)), candidate.size());
        } else {
            return Collections.emptyList();
        }
    }

    private static void reverseArray(char[] array) {
        for(int i = 0; i < array.length / 2; i++) {
            char temp = array[i];
            array[i] = array[array.length - i - 1];
            array[array.length - i - 1] = temp;
        }
    }

    private static int end(Matcher matcher) {
        for (int i = 1; i <= matcher.groupCount(); i++) {
            int e = matcher.end(i);
            if (e > 0) {
                return e;
            }
        }
        return -1;
    }
}

package talon.signature.learning;

import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class HelpersTest {
    private static final String[] VALID_PHONE_NUMBERS = new String[]{"15615552323",
            "1-561-555-1212",
            "5613333",
            "18008793262",
            "800-879-3262",
            "0-800.879.3262",
            "04 3452488",
            "04 -3452488",
            "04 - 3452499",
            "(610) 310-5555 x5555",
            "533-1123",
            "(021)1234567",
            "(021)123456",
            "(000)000000",
            "+7 920 34 57 23",
            "+7(920) 34 57 23",
            "+7(920)345723",
            "+7920345723",
            "8920345723",
            "21143",
            "2-11-43",
            "2 - 11 - 43"};

    @Test
    public void testMatchValidPhoneNumbers() throws Exception {
        for (String phone : VALID_PHONE_NUMBERS) {
            assertTrue(Helpers.RE_RELAX_PHONE.matcher(phone).matches());
        }
    }

    @Test
    public void testMatchNames() throws Exception {
        assertTrue(Helpers.RE_NAME.matcher("John R. Doe").matches());
    }

    @Test
    public void testSenderWithName() throws Exception {
        String[] okLines = new String[]{"Sergey Obukhov <serobnic@example.com>",
                "\tSergey  <serobnic@example.com>",
                "\"Doe, John (TX)\"<DowJ@example.com>@EXAMPLE<IMCEANOTES-+22Doe+2C+20John+20+28TX+29+22+20+3CDoeJ+40example+2Ecom+3E+40EXAMPLE@EXAMPLE.com>",
                "Company Sleuth <csleuth@email.xxx.com>@EXAMPLE <XXX-Company+20Sleuth+20+3Ccsleuth+40email+2Exxx+2Ecom+3E+40EXAMPLE@EXAMPLE.com>",
                "Doe III, John </O=EXAMPLE/OU=NA/CN=RECIPIENTS/CN=jDOE5>"};
        for (String line : okLines) {
            assertTrue(Helpers.RE_SENDER_WITH_NAME.matcher(line).matches());
        }

        String[] notOkLines = new String[]{"",
                "<serobnic@xxx.ru>",
                "Sergey serobnic@xxx.ru"};
        for (String line : notOkLines) {
            assertFalse(Helpers.RE_SENDER_WITH_NAME.matcher(line).matches());
        }
    }

    @Test
    public void testExtractNames() throws Exception {
        Set<String> test1 = Helpers.extractNames("Sergey N.  Obukhov <serobnic@mail.ru>");
        assertEquals(new HashSet<>(Arrays.asList("Sergey", "Obukhov", "serobnic")), test1);

        Set<String> test2 = Helpers.extractNames("");
        assertEquals(new HashSet<>(), test2);

        Map<String, String[]> senderNames = new HashMap<String, String[]>() {{
            put("Jay Rickerts <eCenter@example.com>@EXAMPLE <XXX-Jay+20Rickerts+20+3CeCenter+40example+2Ecom+3E+40EXAMPLE@EXAMPLE.com>",
                    new String[]{"Jay", "Rickerts"});
            // if `,` is used in sender's name
            put("Williams III, Bill </O=EXAMPLE/OU=NA/CN=RECIPIENTS/CN=BWILLIA5>",
                    new String[]{"Williams", "III", "Bill"});

            // if somehow `'` or `"` are used in sender's name
            put("Laura\" \"Goldberg <laura.goldberg@example.com>",
                    new String[] {"Laura", "Goldberg"});
            // extract from senders email address
            put("<sergey@xxx.ru>", new String[] {"sergey"});
            // extract from sender's email address
            // if dots are used in the email address
            put("<sergey.obukhov@xxx.ru>", new String[] {"sergey", "obukhov"});
            // extract from sender's email address
            // if dashes are used in the email address
            put("<sergey-obukhov@xxx.ru>", new String[] {"sergey", "obukhov"});
            // extract from sender's email address
            // if `_` are used in the email address
            put("<sergey_obukhov@xxx.ru>", new String[] {"sergey", "obukhov"});
            // old style From field, found in jangada dataset
            put("wcl@example.com (Wayne Long)", new String[]{"Wayne", "Long"});
            // if only sender's name provided
            put("Wayne Long", new String[] {"Wayne", "Long"});
            // if middle name is shortened with dot
            put("Sergey N.  Obukhov <serobnic@xxx.ru>", new String[] {"Sergey", "Obukhov"});
            // not only spaces could be used as name splitters
            put("  Sergey  Obukhov  <serobnic@xxx.ru>", new String[] {"Sergey", "Obukhov"});
            // finally normal example
            put("Sergey <serobnic@xxx.ru>", new String[] {"Sergey"});
            // if middle name is shortened with `,`
            put("Sergey N, Obukhov", new String[] {"Sergey", "Obukhov"});
            // if mailto used with email address and sender's name is specified
            put("Sergey N, Obukhov [mailto: serobnic@xxx.ru]", new String[] {"Sergey", "Obukhov"});
            // when only email address is given
            put("serobnic@xxx.ru", new String[] {"serobnic"});
            // when nothing is given
            put("", new String[0]);
            // if phone is specified in the `From:` header
            put("wcl@example.com (Wayne Long +7 920 -256 - 35-09)", new String[] {"Wayne", "Long"});
            // from crash reports `nothing to repeat`
            put("* * * * <the_pod1@example.com>", new String[] {"the", "pod"});
            put("\"**Bobby B**\" <copymycashsystem@example.com>", new String[] {"Bobby", "copymycashsystem"});
            // from crash reports `bad escape`
            put("\"M Ali B Azlan \\(GHSE/PETH\\)\" <aliazlan@example.com>", new String[] {"Ali", "Azlan"});
            put("\"Ridthauddin B A Rahim \\(DD/PCSB\\)\" <ridthauddin_arahim@example.com>", new String[] {"Ridthauddin", "Rahim"});
            put("\"Boland, Patrick \\(Global Xxx Group, Ireland \\)\" <Patrick.Boland@example.com>", new String[] {"Boland", "Patrick"});
            put("\"Mates Rate \\(Wine\\)\" <amen@example.com.com>", new String [] {"Mates", "Rate", "Wine"});
            put("\"Morgan, Paul \\(Business Xxx RI, Xxx Xxx Group\\)\" <paul.morgan@example.com>", new String[] {"Morgan", "Paul"});
            put("\"David DECOSTER \\(Domicile\\)\" <decosterdavid@xxx.be>", new String [] {"David", "DECOSTER", "Domicile"});
        }};

        for (Map.Entry<String, String[]> entry : senderNames.entrySet()) {
            Set<String> extractedNames = Helpers.extractNames(entry.getKey());
            for (String name : entry.getValue()) {
                System.out.println(name);
                assertTrue(extractedNames.contains(name));
            }
        }
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
}
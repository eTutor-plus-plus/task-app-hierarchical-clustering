package at.jku.dke.task_app.hierarchical_clustering.evaluation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;
import org.springframework.context.support.ResourceBundleMessageSource;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SubmissionInputParserTest {

    private final String criterium = "criterium.syntax.";
    private MessageSource messageSource;
    private SubmissionInputParser parserEnglish;
    private SubmissionInputParser parserGerman;

    @BeforeEach
    void setUp() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("messages");
        // should display special german characters correctly with UTF-8 encoding
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setFallbackToSystemLocale(false);
        this.messageSource = messageSource;
        parserEnglish = new SubmissionInputParser(messageSource, Locale.ENGLISH);
        parserGerman = new SubmissionInputParser(messageSource, Locale.GERMAN);
    }

    @Test
    void testStructureAndSorted() {
        // Arrange
        String inputSorted = """
            Distance 1.0: (3,4)
            Distance 2.0: (1,2), (3,4)
            Distance 3.0: (5,6,7), (1,2), (3,4)
            Distance 4.0: (1,2,3,4), (5,6,7)
            Distance 5.0: (1,2,3,4,5,6,7)
        """;

        String inputUnsorted = """
            Distance 1.0: (3,4)
            Distance 5.0: (1,2,3,4,5,6,7)
            Distance 4.0: (1,2,3,4), (5,6,7)
            Distance 3.0: (5,6,7), (1,2), (3,4)
            Distance 2.0: (1,2), (3,4)
        """;

        // Act
        SubmissionInputParser.MergeEventWrapper fromSorted = parserEnglish.parse(inputSorted);
        SubmissionInputParser.MergeEventWrapper fromUnsorted = parserEnglish.parse(inputUnsorted);

        // Assert
        assertTrue(fromSorted.isCorrectOrder());
        assertEquals(5, fromSorted.mergeEvents().size());
        assertEquals(2, fromSorted.mergeEvents().get(BigDecimal.valueOf(3)).inheritedMerges().size());
        assertFalse(fromUnsorted.isCorrectOrder());
        assertEquals(5, fromUnsorted.mergeEvents().size());
        assertEquals(2, fromUnsorted.mergeEvents().get(BigDecimal.valueOf(3)).inheritedMerges().size());
    }

    @Test
    void testValidInput() {
        // Arrange
        String input = """
            Distance 1.0: (3,4)
            Distance 2.0: (5,6)
            Distance 3.0: (7,8,9,10), (11,12)
        """;

        // Act
        SubmissionInputParser.MergeEventWrapper result = parserEnglish.parse(input);

        // Assert
        assertEquals(3, result.mergeEvents().size());
        assertEquals(BigDecimal.ONE, result.mergeEvents().keySet().stream().findFirst().orElse(BigDecimal.valueOf(-1.0)));
        assertEquals(BigDecimal.valueOf(3), result.mergeEvents().keySet().stream().toList().get(2));
        assertEquals("(11,12)", result.mergeEvents().get(BigDecimal.valueOf(3)).newMerges().get(1).getResult().getFullLabel());
    }

    @Test
    void testValidWhitespace() {
        // Arrange
        String input = """
            Distance 1.0:   (3,4)

            Distance 2.0:    (5,  6,,)

            Distance 3.0:   (7,8,9,10), (11,12)
        """;

        // Act
        SubmissionInputParser.MergeEventWrapper eventWrapper = parserEnglish.parse(input);
        Map<BigDecimal, EvaluationService.MergeEventAtDistance> result = eventWrapper.mergeEvents();

        // Assert
        assertTrue(eventWrapper.isCorrectOrder());
        assertEquals(3, result.size());
        assertTrue(result.containsKey(BigDecimal.valueOf(1.0)));
        assertTrue(result.containsKey(BigDecimal.valueOf(2.0)));
        assertTrue(result.containsKey(BigDecimal.valueOf(3.0)));
        assertEquals(1, result.get(BigDecimal.valueOf(1.0)).newMerges().size());
        assertEquals(1, result.get(BigDecimal.valueOf(2.0)).newMerges().size());
        assertEquals(2, result.get(BigDecimal.valueOf(3.0)).newMerges().size());
    }

    @Test
    void testValidSplitDistances() {
        // Arrange
        String input = """
            Distance 1.0: (3,4)
            Distance 2.0: (5,6)
            Distance 3.0: (7,8,9,10)
            Distance 3.0: (11,12)
        """;

        // Act
        SubmissionInputParser.MergeEventWrapper eventWrapper = parserEnglish.parse(input);
        Map<BigDecimal, EvaluationService.MergeEventAtDistance> result = eventWrapper.mergeEvents();

        // Assert
        assertTrue(eventWrapper.isCorrectOrder());
        assertEquals(3, result.size());
        assertTrue(result.containsKey(BigDecimal.valueOf(1.0)));
        assertTrue(result.containsKey(BigDecimal.valueOf(2.0)));
        assertTrue(result.containsKey(BigDecimal.valueOf(3.0)));
        assertEquals(1, result.get(BigDecimal.valueOf(1.0)).newMerges().size());
        assertEquals(1, result.get(BigDecimal.valueOf(2.0)).newMerges().size());
        assertEquals(2, result.get(BigDecimal.valueOf(3.0)).newMerges().size());
    }

    @Test
    void testValidAlternatives() {
        // Arrange
        // "Distance" misspelled or missing
        String misspelledDistanceInput = """
            Distance 1.0: (3,4)
            Disance 2.0: (5,   6)
            3.0: (7,8,9,10), (11,12)""";

        // actual distance not full written with decimal (values like "3." and ".5" also work for syntax)
        String decimalFormatInput = """
            Distance .5: (3,4)
            Distance 2: (5,6)
            Distance 3.: (7,8,9,10), (11,12)
        """;

        // Act & Assert
        assertDoesNotThrow(() -> parserEnglish.parse(misspelledDistanceInput));
        assertDoesNotThrow(() -> parserEnglish.parse(decimalFormatInput));

        Map<BigDecimal, EvaluationService.MergeEventAtDistance> resultMisspelled = parserEnglish.parse(misspelledDistanceInput).mergeEvents();
        Map<BigDecimal, EvaluationService.MergeEventAtDistance> resultDecimalFormat = parserEnglish.parse(decimalFormatInput).mergeEvents();

        assertEquals(3, resultMisspelled.size());
        assertEquals(3, resultDecimalFormat.size());
    }

    @Test
    void testMissingSeparator() {
        // Arrange
        String missing = """
            Distance 1.0 (3,4)
            Distance 2.0: (5,6)
            Distance 3.0: (7,8,9,10), (11,12)
        """;
        String misplaced = """
            Distance 1.0: (3,4)
            Distance 2.0: (5,6)
            Distance 3.0 (:7,8,9,10) (11,12)
        """;

        // Act & Assert
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> parserEnglish.parse(missing));
        assertEquals(getMessage("separator", Locale.ENGLISH, 1), e.getMessage());
        e = assertThrows(IllegalArgumentException.class, () -> parserGerman.parse(missing));
        assertEquals(getMessage("separator", Locale.GERMAN, 1), e.getMessage());

        // parser recognizes everything before the separator as the distance, hence this behavior is valid
        e = assertThrows(IllegalArgumentException.class, () -> parserEnglish.parse(misplaced));
        assertEquals(getMessage("distance", Locale.ENGLISH, "3.0(", 3), e.getMessage());
        e = assertThrows(IllegalArgumentException.class, () -> parserGerman.parse(misplaced));
        assertEquals(getMessage("distance", Locale.GERMAN, "3.0(", 3), e.getMessage());
    }

    @Test
    void testInvalidNumberFormat() {
        // Arrange
        // also check that it breaks on the first syntax error
        String doubleDotsBeforeNumbers = """
            Distance ..5: (3,4)
            Distance 2.0: (5,6)
            Distance ..0: (7,8,9,10), (11,12)
        """;
        String missingNumberBeforeAndAfterDecimalPoint = """
            Distance 1.0: (3,4)
            Distance .: (5,6)
            Distance 3.0: (7,8,9,10), (11,12)
        """;
        String missingNumberCompletely = """
            Distance 1.0: (3,4)
            Distance 2.0: (5,6)
            Distance : (7,8,9,10), (11,12)
        """;
        String separatorBeforeNumbers = """
            Distance 1.0: (3,4)
            Distance 2.0: (5,6)
            Distance :3.0 (7,8,9,10), (11,12)
        """;

        // Act & Assert
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> parserEnglish.parse(doubleDotsBeforeNumbers));
        assertEquals(getMessage("distance", Locale.ENGLISH, "..5", 1), e.getMessage());
        e = assertThrows(IllegalArgumentException.class, () -> parserGerman.parse(doubleDotsBeforeNumbers));
        assertEquals(getMessage("distance", Locale.GERMAN, "..5", 1), e.getMessage());

        e = assertThrows(IllegalArgumentException.class, () -> parserEnglish.parse(missingNumberBeforeAndAfterDecimalPoint));
        assertEquals(getMessage("distance", Locale.ENGLISH, ".", 2), e.getMessage());
        e = assertThrows(IllegalArgumentException.class, () -> parserGerman.parse(missingNumberBeforeAndAfterDecimalPoint));
        assertEquals(getMessage("distance", Locale.GERMAN, ".", 2), e.getMessage());

        e = assertThrows(IllegalArgumentException.class, () -> parserEnglish.parse(missingNumberCompletely));
        assertEquals(getMessage("distance", Locale.ENGLISH, "", 3), e.getMessage());
        e = assertThrows(IllegalArgumentException.class, () -> parserGerman.parse(missingNumberCompletely));
        assertEquals(getMessage("distance", Locale.GERMAN, "", 3), e.getMessage());

        e = assertThrows(IllegalArgumentException.class, () -> parserEnglish.parse(separatorBeforeNumbers));
        assertEquals(getMessage("distance", Locale.ENGLISH, "", 3), e.getMessage());
        e = assertThrows(IllegalArgumentException.class, () -> parserGerman.parse(separatorBeforeNumbers));
        assertEquals(getMessage("distance", Locale.GERMAN, "", 3), e.getMessage());
    }

    @Test
    void testMissingBrackets() {
        // Arrange
        String missingOpeningBracket = """
            Distance 1.0: 3,4)
            Distance 2.0: (5,6)
            Distance 3.0: (7,8,9,10), (11,12)
        """;
        String misplacedOpeningBracket = """
            Distance 1.0: (3,4)
            Distance 2.0: 5(,6)
            Distance 3.0: (7,8,9,10), (11,12)
        """;
        String missingClosingBracket = """
            Distance 1.0: (3,4)
            Distance 2.0: (5,6)
            Distance 3.0: (7,8,9,10, (11,12)
        """;
        String misplacedClosingBracket1 = """
            Distance 1.0: (3,4)
            Distance 2.0: (5,6)
            Distance 3.0: (7,8,9,), 10(11,12)
        """;
        String misplacedClosingBracket2 = """
            Distance 1.0: (3,4)
            Distance 2.0: (5,6)
            Distance 3.0: (7,8,9,)10, (11,12)
        """;

        // Act & Assert
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> parserEnglish.parse(missingOpeningBracket));
        assertEquals(getMessage("openingBracket", Locale.ENGLISH, 1), e.getMessage());
        e = assertThrows(IllegalArgumentException.class, () -> parserGerman.parse(missingOpeningBracket));
        assertEquals(getMessage("openingBracket", Locale.GERMAN, 1), e.getMessage());
        e = assertThrows(IllegalArgumentException.class, () -> parserEnglish.parse(missingClosingBracket));
        assertEquals(getMessage("closingBracket", Locale.ENGLISH, 3), e.getMessage());
        e = assertThrows(IllegalArgumentException.class, () -> parserGerman.parse(missingClosingBracket));
        assertEquals(getMessage("closingBracket", Locale.GERMAN, 3), e.getMessage());

        e = assertThrows(IllegalArgumentException.class, () -> parserEnglish.parse(misplacedOpeningBracket));
        assertEquals(getMessage("openingBracket", Locale.ENGLISH, 2), e.getMessage());
        e = assertThrows(IllegalArgumentException.class, () -> parserGerman.parse(misplacedOpeningBracket));
        assertEquals(getMessage("openingBracket", Locale.GERMAN, 2), e.getMessage());
        e = assertThrows(IllegalArgumentException.class, () -> parserEnglish.parse(misplacedClosingBracket1));
        assertEquals(getMessage("openingBracket", Locale.ENGLISH, 3), e.getMessage());
        e = assertThrows(IllegalArgumentException.class, () -> parserGerman.parse(misplacedClosingBracket1));
        assertEquals(getMessage("openingBracket", Locale.GERMAN, 3), e.getMessage());
        e = assertThrows(IllegalArgumentException.class, () -> parserEnglish.parse(misplacedClosingBracket2));
        assertEquals(getMessage("comma", Locale.ENGLISH, 3), e.getMessage());
        e = assertThrows(IllegalArgumentException.class, () -> parserGerman.parse(misplacedClosingBracket2));
        assertEquals(getMessage("comma", Locale.GERMAN, 3), e.getMessage());
    }

    @Test
    void testMissingCommaBetweenClusters() {
        // Arrange
        String input = """
            1.0: (3,4)
            2.0: (5,6)
            3.0: (7,8,9,10) (11,12)
        """;

        // Act & Assert
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> parserEnglish.parse(input));
        assertEquals(getMessage("comma", Locale.ENGLISH, 3), e.getMessage());
        e = assertThrows(IllegalArgumentException.class, () -> parserGerman.parse(input));
        assertEquals(getMessage("comma", Locale.GERMAN, 3), e.getMessage());
    }

    @Test
    void testEmptyCluster() {
        // Arrange
        String input = """
            1.0: (3,4)
            2.0: ()
            3.0: (7,8,9,10), (11,12)
        """;

        // Act & Assert
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> parserEnglish.parse(input));
        assertEquals(getMessage("emptyCluster", Locale.ENGLISH, 2), e.getMessage());
        e = assertThrows(IllegalArgumentException.class, () -> parserGerman.parse(input));
        assertEquals(getMessage("emptyCluster", Locale.GERMAN, 2), e.getMessage());
    }

    private String getMessage(String criterium, Locale locale, Object... args) {
        return this.messageSource.getMessage(this.criterium + criterium, args, locale);
    }
}

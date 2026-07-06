package at.jku.dke.task_app.hierarchical_clustering.evaluation;

import at.jku.dke.task_app.hierarchical_clustering.data.entities.HierarchicalClusteringCluster;
import at.jku.dke.task_app.hierarchical_clustering.data.entities.HierarchicalClusteringMerge;
import org.springframework.context.MessageSource;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Class for parsing a submission's input to a merge event history
 * for evaluation.
 */
public class SubmissionInputParser {

    private final MessageSource messageSource;
    private final Locale locale;

    private int lineNumber;
    private int stepNumber;
    private BigDecimal previousDistance;

    /**
     * Creates a new instance of class {@linkplain SubmissionInputParser}.
     *
     * @param messageSource The message source (for emitting syntax errors).
     * @param locale        The locale/language for messages.
     */
    public SubmissionInputParser(MessageSource messageSource, Locale locale) {
        this.messageSource = messageSource;
        this.locale = locale;
    }

    /**
     * Builds a merge event history from an input by parsing each line
     * into usable merges and subsequently mapping them to distances
     * (including inherited merges).
     *
     * @param input The input to be parsed.
     * @return A wrapper containing the merge event history and the
     *         information whether the input was ordered correctly
     *         by distances ascending.
     */
    public MergeEventWrapper parse(String input) {
        List<HierarchicalClusteringMerge> merges = new ArrayList<>();
        MergeEventWrapper eventWrapper = new MergeEventWrapper();
        eventWrapper.isCorrectOrder = true;

        lineNumber = 1;
        stepNumber = 1;
        previousDistance = new BigDecimal("-1.0");

        for (String line : input.split("\n")) {
            if (!line.isEmpty()) {
                parseLine(line, merges, eventWrapper);
                lineNumber++;
            }
        }

        buildMergeEvents(merges, eventWrapper);

        return eventWrapper;
    }

    /**
     * Parses a line from the input to build raw merges from the contained
     * distance and clusters. Does nothing if normalized line is empty.
     * <p>
     * First splits the line into distance and clusters. Then attempts to
     * parse the distance as a {@code double} and checks whether the order
     * is still correct by comparing to the previously parsed distance.
     * Subsequently, parses all clusters at the distance to build cluster
     * and merge data. Missing or wrong parts of the syntax lead to syntax
     * errors being thrown.
     *
     * @param input        The input line to be parsed.
     * @param merges       The list of merges in the input.
     * @param eventWrapper The merge event history wrapper.
     */
    private void parseLine(String input, List<HierarchicalClusteringMerge> merges, MergeEventWrapper eventWrapper) {
        String normalized = normalizeLine(input);

        if (normalized.isEmpty()) {
            return;
        }

        validateBrackets(normalized);

        String[] parts = normalized.split(":", 2);
        if (parts.length != 2) {
            throwSyntaxError("separator", lineNumber);
        }

        BigDecimal distance = BigDecimal.ZERO;
        try {
            distance = new BigDecimal(parts[0]).stripTrailingZeros();
        } catch (NumberFormatException e) {
            throwSyntaxError("distance", parts[0], lineNumber);
        }

        // ordering should be ascending, so new distance has to be equal or higher for correct order
        if (distance.compareTo(previousDistance) < 0) {
            eventWrapper.isCorrectOrder = false;
        }

        previousDistance = distance;

        String clusterPart = parts[1];

        int i = 0;
        while (i < clusterPart.length()) {
            if (clusterPart.charAt(i) != '(') {
                throwSyntaxError("openingBracket", lineNumber);
            }

            int end = findMatchingParenthesis(clusterPart, i);
            if (end == -1) {
                throwSyntaxError("closingBracket", lineNumber);
            }

            String inside = clusterPart.substring(i + 1, end);
            HierarchicalClusteringMerge merge = new HierarchicalClusteringMerge();
            merge.setDistance(distance);
            merge.setStep(stepNumber++);

            HierarchicalClusteringCluster result = new HierarchicalClusteringCluster();
            result.setDataPoints(parsePoints(inside));
            merge.setResult(result);
            merges.add(merge);

            i = end + 1;

            if (i < clusterPart.length()) {
                if (clusterPart.charAt(i) == ')') {
                    throwSyntaxError("superfluousBracket", clusterPart.charAt(i), lineNumber);
                } else if (clusterPart.charAt(i) != ',') {
                    throwSyntaxError("comma", lineNumber);
                }

                i++;
            }
        }
    }

    /**
     * Helper method to normalize an input line.
     * <p>
     * Removes everything before the first number, dot or colon (to allow
     * misspellings in or leaving out the "Distance" part in an input, but
     * stop at either the distance number, a dot (e.g. for ".5" input for 0.5),
     * or the colon separator). Also removes all whitespace in the line.
     *
     * @param input The line to be normalized.
     * @return The normalized line without unnecessary characters or whitespace.
     */
    private String normalizeLine(String input) {
        return input
            .replaceFirst("^[^0-9.:-]*", "") // also allows decimal points and minuses to be parsed (no syntax error, but potential semantic error), and also catches empty distances by not cutting the ":" separator
            .replaceAll("\\s+", "");
    }

    /**
     * Helper method to check whether an input contains invalid brackets.
     *
     * @param input The input.
     */
    private void validateBrackets(String input) {
        for (char c : input.toCharArray()) {
            if (c == '[' || c == ']' || c == '{' || c == '}') {
                throwSyntaxError("invalidBracket", c, lineNumber);
            }
        }
    }

    /**
     * Helper method that finds the matching outer parenthesis of a cluster
     * when the data points inside the cluster are nested tuples (e.g. in format
     * ((1,2), (3,4)) instead of (1,2,3,4)).
     * <p>
     * Returns the index of the closing parenthesis that matches the opening
     * parenthesis at the given start index. Consequently, any missing brackets in
     * the input can be detected through this function as well.
     *
     * @param input The input.
     * @param start The index of the opening parenthesis to match.
     * @return The index of the matching closing parenthesis.
     */
    private int findMatchingParenthesis(String input, int start) {
        int depth = 0;

        for (int i = start; i < input.length(); i++) {
            char c = input.charAt(i);

            if (c == '(') {
                depth++;
                if (depth > 1) {
                    throwSyntaxError("superfluousBracket", c, lineNumber);
                }
            } else if (c == ')') {
                depth--;
                if (depth == 0) {
                    return i;
                }
            }
        }

        return -1;
    }

    /**
     * Helper method to parse the points inside a cluster.
     * <p>
     * Splits the input cluster by commas to get the data points.
     * An empty cluster leads to a syntax error. Superfluous commas
     * (e.g. (1,2,,,3)) however do not lead to a syntax error, the empty "points"
     * in between commas are simply omitted.
     *
     * @param input The cluster/data points to be parsed.
     * @return The list of data points (strings) in the cluster.
     */
    private List<String> parsePoints(String input) {
        if (input.isEmpty()) {
            throwSyntaxError("emptyCluster", lineNumber);
        }

        String[] parts = input.split(",");

        return Stream.of(parts).sorted().toList();
    }

    /**
     * Helper method to transform raw merges to a merge event history including
     * newly formed and inherited clusters for evaluation.
     * <p>
     * Creates a map entry for every distance found in the merge history containing
     * the newly formed clusters as well as inherited clusters at this distance.
     *
     * @param merges       The list of raw merges.
     * @param eventWrapper The wrapper for the merge event history.
     */
    private void buildMergeEvents(List<HierarchicalClusteringMerge> merges, MergeEventWrapper eventWrapper) {
        SortedMap<BigDecimal, EvaluationService.MergeEventAtDistance> mergeEvents = new TreeMap<>();
        SortedMap<BigDecimal, List<HierarchicalClusteringMerge>> mergesByDistance = merges.stream()
                .collect(Collectors.groupingBy(HierarchicalClusteringMerge::getDistance, TreeMap::new, Collectors.toList()));

        for (BigDecimal distance: mergesByDistance.keySet()) {
            List<HierarchicalClusteringMerge> mergeList = mergesByDistance.get(distance);
            List<HierarchicalClusteringMerge> newMerges = new ArrayList<>();
            List<HierarchicalClusteringMerge> inheritedMerges = new ArrayList<>();

            for (HierarchicalClusteringMerge merge : mergeList) {
                boolean inherited = false;

                for (BigDecimal key : mergeEvents.keySet()) {
                    // only checks all new merges of lower distances as inherited merges always correspond to a previous new merge to avoid redundancy
                    if (key.compareTo(distance) < 0 && mergeEvents.get(key).newMerges().stream()
                            .anyMatch(m -> m.getResult().getDataPoints().equals(merge.getResult().getDataPoints()))) {
                        inherited = true;
                        break;
                    }
                }

                if (inherited) {
                    inheritedMerges.add(merge);
                } else {
                    newMerges.add(merge);
                }
            }

            mergeEvents.put(distance, new EvaluationService.MergeEventAtDistance(newMerges, inheritedMerges, null));
        }

        eventWrapper.mergeEvents = mergeEvents;
    }

    /**
     * Helper method for easily building syntax error messages.
     *
     * @param syntaxCriterion The key/code of the message.
     * @param args            The parameters of the message.
     */
    private void throwSyntaxError(String syntaxCriterion, Object... args) {
        throw new IllegalArgumentException(this.messageSource.getMessage(
            "criterium.syntax." + syntaxCriterion,
            args,
            locale
        ));
    }

    /**
     * A wrapper for merge event histories containing additional information
     * on whether the merges of an input were sorted correctly by distances
     * ascending.
     */
    public static class MergeEventWrapper {
        private boolean isCorrectOrder;
        private SortedMap<BigDecimal, EvaluationService.MergeEventAtDistance> mergeEvents;

        /**
         * Creates a new instance of class {@linkplain MergeEventWrapper}.
         * Can only be accessed by itself and outer class {@link SubmissionInputParser}.
         */
        private MergeEventWrapper() {}

        /**
         * Gets the correct order boolean.
         *
         * @return {@code} true, if the input was ordered correctly.
         */
        public boolean isCorrectOrder() {
            return isCorrectOrder;
        }

        /**
         * Gets the merge event history.
         *
         * @return The {@link SortedMap} of merge events mapped to their distance.
         */
        public SortedMap<BigDecimal, EvaluationService.MergeEventAtDistance> mergeEvents() {
            return mergeEvents;
        }
    }

}

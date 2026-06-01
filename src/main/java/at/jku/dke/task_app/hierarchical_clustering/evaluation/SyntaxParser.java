package at.jku.dke.task_app.hierarchical_clustering.evaluation;

import at.jku.dke.task_app.hierarchical_clustering.data.entities.HierarchicalClusteringCluster;
import at.jku.dke.task_app.hierarchical_clustering.data.entities.HierarchicalClusteringMerge;
import org.springframework.context.MessageSource;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SyntaxParser {

    private final MessageSource messageSource;
    private final Locale locale;

    private int lineNumber;
    private int stepNumber;
    private double previousDistance;

    public SyntaxParser(MessageSource messageSource, Locale locale) {
        this.messageSource = messageSource;
        this.locale = locale;
    }

    public MergeEventWrapper parse(String input) {
        List<HierarchicalClusteringMerge> merges = new ArrayList<>();
        MergeEventWrapper eventWrapper = new MergeEventWrapper();
        eventWrapper.isCorrectOrder = true;

        lineNumber = 1;
        stepNumber = 1;
        previousDistance = -1.0;

        for (String line : input.split("\n")) {
            if (!line.isEmpty()) {
                parseLine(line, merges, eventWrapper);
                lineNumber++;
            }
        }

        buildMergeEvents(merges, eventWrapper);

        return eventWrapper;
    }

    private void parseLine(String input, List<HierarchicalClusteringMerge> merges, MergeEventWrapper eventWrapper) {
        String normalized = normalizeLine(input);

        if (normalized.isEmpty()) {
            return;
        }

        String[] parts = normalized.split(":", 2);
        if (parts.length != 2) {
            throwSyntaxError("separator", lineNumber);
        }

        double distance = -1;
        try {
            distance = Double.parseDouble(parts[0]);
        } catch (NumberFormatException e) {
            throwSyntaxError("distance", locale, parts[0], lineNumber);
        }

        // ordering should be ascending, so new distance has to be equal or higher for correct order
        if (distance < previousDistance) {
            eventWrapper.isCorrectOrder = false;
        }

        previousDistance = distance;

        String clusterPart = parts[1];

        int i = 0;
        while (i < clusterPart.length()) {
            if (clusterPart.charAt(i) != '(') {
                throwSyntaxError("openingBracket", locale, lineNumber);
            }

            int end = clusterPart.indexOf(')', i);
            if (end == -1) {
                throwSyntaxError("closingBracket", locale, lineNumber);
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
                if (clusterPart.charAt(i) != ',') {
                    throwSyntaxError("comma", locale, i, lineNumber);
                }
                i++;
            }
        }
    }

    private String normalizeLine(String input) {
        return input
            .replaceFirst("^[^0-9.:-]*", "") // also allows decimal points and minuses to be parsed (no syntax error, but potential semantic error), and also catches empty distances by not cutting the ":" separator
            .replaceAll("\\s+", "");
    }

    private List<String> parsePoints(String input) {
        if (input.isEmpty()) {
            throwSyntaxError("emptyCluster", locale, lineNumber);
        }

        String[] parts = input.split(",");

        return Stream.of(parts).sorted().toList();
    }

    private void buildMergeEvents(List<HierarchicalClusteringMerge> merges, MergeEventWrapper eventWrapper) {
        SortedMap<Double, EvaluationService.MergeEventAtDistance> mergeEvents = new TreeMap<>();
        SortedMap<Double, List<HierarchicalClusteringMerge>> mergesByDistance = merges.stream()
                .collect(Collectors.groupingBy(HierarchicalClusteringMerge::getDistance, TreeMap::new, Collectors.toList()));

        for (double distance: mergesByDistance.keySet()) {
            List<HierarchicalClusteringMerge> mergeList = mergesByDistance.get(distance);
            List<HierarchicalClusteringMerge> newMerges = new ArrayList<>();
            List<HierarchicalClusteringMerge> inheritedMerges = new ArrayList<>();

            for (HierarchicalClusteringMerge merge : mergeList) {
                boolean inherited = false;

                for (double key : mergeEvents.keySet()) {
                    // only traverses all new merges of lower distances because inherited merges at lower distances always correspond to an existing new merge
                    if (key < distance && mergeEvents.get(key).newMerges().stream()
                            .anyMatch(m -> m.getResult().equals(merge.getResult()))) {
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

    private void throwSyntaxError(String syntaxCriterion, Object... args) {
        throw new IllegalArgumentException(this.messageSource.getMessage(
            "criterium.syntax." + syntaxCriterion,
            args,
            locale
        ));
    }

    public static class MergeEventWrapper {
        private boolean isCorrectOrder;
        private SortedMap<Double, EvaluationService.MergeEventAtDistance> mergeEvents;

        private MergeEventWrapper() {}

        public boolean isCorrectOrder() {
            return isCorrectOrder;
        }

        public SortedMap<Double, EvaluationService.MergeEventAtDistance> mergeEvents() {
            return mergeEvents;
        }
    }

}

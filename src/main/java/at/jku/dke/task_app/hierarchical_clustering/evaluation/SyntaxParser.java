package at.jku.dke.task_app.hierarchical_clustering.evaluation;

import at.jku.dke.task_app.hierarchical_clustering.data.entities.HierarchicalClusteringCluster;
import at.jku.dke.task_app.hierarchical_clustering.data.entities.HierarchicalClusteringMerge;
import org.springframework.context.MessageSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class SyntaxParser {

    private final MessageSource messageSource;
    private int lineNumber;
    private int stepNumber;

    public SyntaxParser(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public List<HierarchicalClusteringMergeWrapper> parse(String input, Locale locale) {
        List<HierarchicalClusteringMergeWrapper> result = new ArrayList<>();
        lineNumber = 1;
        stepNumber = 1;

        for (String line : input.split("\n")) {
            if (!line.isEmpty()) {
                result.addAll(parseLine(line, locale));
                lineNumber++;
            }
        }

        return result;
    }

    private List<HierarchicalClusteringMergeWrapper> parseLine(String input, Locale locale) {
        String normalized = normalizeLine(input);

        if (normalized.isEmpty()) {
            return List.of();
        }

        String[] parts = normalized.split(":", 2);
        if (parts.length != 2) {
            throwSyntaxError("separator", locale, lineNumber);
        }

        double distance = -1;
        try {
            distance = Double.parseDouble(parts[0]);
        } catch (NumberFormatException e) {
            throwSyntaxError("distance", locale, parts[0], lineNumber);
        }

        List<HierarchicalClusteringMergeWrapper> merges = new ArrayList<>();
        String clusterPart = parts[1];

        int i = 0;
        while (i < clusterPart.length()) {
            if (clusterPart.charAt(i) != '{') {
                throwSyntaxError("openingBracket", locale, i, lineNumber);
            }

            int end = clusterPart.indexOf('}', i);
            if (end == -1) {
                throwSyntaxError("closingBracket", locale, lineNumber);
            }

            String inside = clusterPart.substring(i + 1, end);
            HierarchicalClusteringMerge merge = new HierarchicalClusteringMerge();
            merge.setDistance(distance);
            merge.setStep(stepNumber++);

            HierarchicalClusteringCluster result = new HierarchicalClusteringCluster();
            result.setDataPoints(parsePoints(inside, locale));
            merge.setResult(result);

            merges.add(new HierarchicalClusteringMergeWrapper(lineNumber, merge));

            i = end + 1;

            if (i < clusterPart.length()) {
                if (clusterPart.charAt(i) != ',') {
                    throwSyntaxError("comma", locale, i, lineNumber);
                }
                i++;
            }
        }

        return merges;
    }

    private String normalizeLine(String input) {
        return input
            .replaceFirst("^[^0-9-]*", "")
            .replaceAll("\\s+", "");
    }

    private List<String> parsePoints(String input, Locale locale) {
        if (input.isEmpty()) {
            throwSyntaxError("emptyCluster", locale, lineNumber);
        }

        String[] parts = input.split(",");

        return List.of(parts);
    }

    private void throwSyntaxError(String syntaxCriterion, Locale locale, Object... args) {
        throw new IllegalArgumentException(this.messageSource.getMessage(
            "criterium.syntax." + syntaxCriterion,
            args,
            locale
        ));
    }

    public record HierarchicalClusteringMergeWrapper(int line, HierarchicalClusteringMerge merge) {
    }

}

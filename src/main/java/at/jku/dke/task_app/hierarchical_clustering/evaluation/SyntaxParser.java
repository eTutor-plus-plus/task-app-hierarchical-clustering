package at.jku.dke.task_app.hierarchical_clustering.evaluation;

import at.jku.dke.task_app.hierarchical_clustering.data.entities.HierarchicalClusteringCluster;
import at.jku.dke.task_app.hierarchical_clustering.data.entities.HierarchicalClusteringMerge;

import java.util.ArrayList;
import java.util.List;

public class SyntaxParser {

    public static List<HierarchicalClusteringMergeWrapper> parse(String input) {
        List<HierarchicalClusteringMergeWrapper> result = new ArrayList<>();
        int lineNumber = 1;
        int stepNumber = 1;

        for (String line : input.split("\n")) {
            if (!line.isEmpty()) {
                result.addAll(parseLine(lineNumber++, stepNumber, line));
            }
        }

        return result;
    }

    private static List<HierarchicalClusteringMergeWrapper> parseLine(int lineNumber, int stepNumber, String input) {
        String normalized = normalizeLine(input);

        String[] parts = normalized.split(":", 2);
        if (parts.length != 2) {
            throw new IllegalArgumentException("Missing ':' separator");
        }

        double distance;
        try {
            distance = Double.parseDouble(parts[0]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid coordinates: " + parts[0]);
        }

        List<HierarchicalClusteringMergeWrapper> merges = new ArrayList<>();
        String clusterPart = parts[1];

        int i = 0;
        while (i < clusterPart.length()) {
            if (clusterPart.charAt(i) != '{') {
                throw new IllegalArgumentException("Expected '{' at position " + i);
            }

            int end = clusterPart.indexOf('}', i);
            if (end == -1) {
                throw new IllegalArgumentException("Missing closing '}'");
            }

            String inside = clusterPart.substring(i + 1, end);
            HierarchicalClusteringMerge merge = new HierarchicalClusteringMerge();
            merge.setDistance(distance);
            merge.setStep(stepNumber++);

            HierarchicalClusteringCluster result = new HierarchicalClusteringCluster();
            result.setLabel(inside);
            result.setDataPoints(parsePoints(inside));
            merge.setResult(result);

            merges.add(new HierarchicalClusteringMergeWrapper(lineNumber, merge));

            i = end + 1;

            if (i < clusterPart.length()) {
                if (clusterPart.charAt(i) != ',') {
                    throw new IllegalArgumentException("Expected ',' between clusters at position " + i);
                }
                i++;
            }
        }

        return merges;
    }

    private static String normalizeLine(String input) {
        return input
            .replaceFirst("^[^0-9-]*", "")
            .replaceAll("\\s+", "");
    }

    private static List<String> parsePoints(String input) {
        if (input.isEmpty()) {
            throw new IllegalArgumentException("Empty cluster {} not allowed");
        }

        String[] parts = input.split(",");

        // extra parse step to ensure that points are of correct format (currently not implemented)
//        List<String> points = new ArrayList<>();
//
//        for (String p : parts) {
//            int id;
//            try {
//                id = Integer.parseInt(p);
//            } catch (NumberFormatException e) {
//                throw new IllegalArgumentException("Data point " + p + " is of wrong format");
//            }
//
//            if (p.isEmpty()) {
//                throw new IllegalArgumentException("Empty point in cluster");
//            } else if (!points.add(id)) {
//                throw new IllegalArgumentException("Duplicate point in cluster");
//            }
//        }
//
//        return points;

        return List.of(parts);
    }

    public record HierarchicalClusteringMergeWrapper(int line, HierarchicalClusteringMerge merge) {
    }

}

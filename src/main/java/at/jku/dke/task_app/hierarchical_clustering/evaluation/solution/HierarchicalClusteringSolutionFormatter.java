package at.jku.dke.task_app.hierarchical_clustering.evaluation.solution;

import at.jku.dke.task_app.hierarchical_clustering.data.entities.HierarchicalClusteringCluster;
import at.jku.dke.task_app.hierarchical_clustering.data.entities.HierarchicalClusteringMerge;

import java.util.*;

public class HierarchicalClusteringSolutionFormatter {

    private HierarchicalClusteringSolutionFormatter() {}

    public static HierarchicalClusteringCluster leafCluster(String label) {
        HierarchicalClusteringCluster cluster = new HierarchicalClusteringCluster();
        cluster.setLabel(label);
        cluster.setDataPoints(Collections.singletonList(label));
        return cluster;
    }

    public static HierarchicalClusteringCluster mergeCluster(HierarchicalClusteringCluster left, HierarchicalClusteringCluster right) {
        List<String> combined = new ArrayList<>();
        combined.addAll(left.getDataPoints());
        combined.addAll(right.getDataPoints());
        Collections.sort(combined);
        String label = String.join(",", combined);
        HierarchicalClusteringCluster cluster = new HierarchicalClusteringCluster();
        cluster.setLabel(label);
        cluster.setDataPoints(combined);
        return cluster;
    }

    public static List<HierarchicalClusteringMerge> format(List<RawMerge> rawMerges) {
        List<HierarchicalClusteringMerge> result = new ArrayList<>();
        Map<List<String>, HierarchicalClusteringCluster> clusterLookup = new HashMap<>();

        for (RawMerge raw : rawMerges) {
            List<String> sortedLeft = new ArrayList<>(raw.leftPoints);
            Collections.sort(sortedLeft);
            HierarchicalClusteringCluster left = clusterLookup.get(sortedLeft);
            if (left == null) {
                left = buildCluster(sortedLeft);
                clusterLookup.put(sortedLeft, left);
            }

            List<String> sortedRight = new ArrayList<>(raw.rightPoints);
            Collections.sort(sortedRight);
            HierarchicalClusteringCluster right = clusterLookup.get(sortedRight);
            if (right == null) {
                right = buildCluster(sortedRight);
                clusterLookup.put(sortedRight, right);
            }

            HierarchicalClusteringCluster merged = mergeCluster(left, right);
            clusterLookup.put(merged.getDataPoints(), merged);
            result.add(new HierarchicalClusteringMerge(left, right, merged, raw.distance, raw.step));
        }

        return result;
    }

    private static HierarchicalClusteringCluster buildCluster(List<String> points) {
        String label = String.join(",", points);
        HierarchicalClusteringCluster cluster = new HierarchicalClusteringCluster();
        cluster.setDataPoints(points);
        cluster.setLabel(label);
        return cluster;
    }

    public record RawMerge(List<String> leftPoints, List<String> rightPoints, double distance, int step) {
    }
}

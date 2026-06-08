package at.jku.dke.task_app.hierarchical_clustering.clustering;

import at.jku.dke.task_app.hierarchical_clustering.data.entities.HierarchicalClusteringCluster;
import at.jku.dke.task_app.hierarchical_clustering.data.entities.HierarchicalClusteringMerge;

import java.util.*;

public class SolutionFormatter {

    private SolutionFormatter() {}

    public static HierarchicalClusteringCluster mergeCluster(HierarchicalClusteringCluster left, HierarchicalClusteringCluster right) {
        List<String> combined = new ArrayList<>();
        combined.addAll(left.getDataPoints());
        combined.addAll(right.getDataPoints());
        Collections.sort(combined);
        HierarchicalClusteringCluster cluster = new HierarchicalClusteringCluster();
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
        HierarchicalClusteringCluster cluster = new HierarchicalClusteringCluster();
        cluster.setDataPoints(points);
        return cluster;
    }

    public record RawMerge(List<String> leftPoints, List<String> rightPoints, double distance, int step) {
    }
}

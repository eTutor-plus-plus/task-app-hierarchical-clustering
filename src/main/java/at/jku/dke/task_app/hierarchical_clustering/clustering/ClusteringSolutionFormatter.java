package at.jku.dke.task_app.hierarchical_clustering.clustering;

import at.jku.dke.task_app.hierarchical_clustering.data.entities.HierarchicalClusteringCluster;
import at.jku.dke.task_app.hierarchical_clustering.data.entities.HierarchicalClusteringMerge;

import java.util.*;

/**
 * Utility class for formatting a list of raw merges into a
 * merge history containing proper merges.
 */
public class ClusteringSolutionFormatter {

    /**
     * Creates a new instance of class {@linkplain ClusteringSolutionFormatter}.
     * <p>
     * Private accessor as this is a static utility class.
     */
    private ClusteringSolutionFormatter() {}

    /**
     * Merges two clusters. More formally, combines the data points of two clusters
     * to form a new cluster which acts as the result of a merge.
     *
     * @param left  The first source cluster.
     * @param right The second source cluster.
     * @return The cluster resulting from merging the source clusters.
     */
    public static HierarchicalClusteringCluster mergeCluster(HierarchicalClusteringCluster left, HierarchicalClusteringCluster right) {
        List<String> combined = new ArrayList<>();
        combined.addAll(left.getDataPoints());
        combined.addAll(right.getDataPoints());
        Collections.sort(combined);
        HierarchicalClusteringCluster cluster = new HierarchicalClusteringCluster();
        cluster.setDataPoints(combined);
        return cluster;
    }

    /**
     * Formats a list of raw merges into actual merges for a merge history.
     *
     * @param rawMerges The raw merges to be formatted.
     * @return The list of actual merges.
     */
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

    /**
     * Constructs a new cluster object with the given data points.
     *
     * @param points The data points.
     * @return A new cluster object.
     */
    private static HierarchicalClusteringCluster buildCluster(List<String> points) {
        HierarchicalClusteringCluster cluster = new HierarchicalClusteringCluster();
        cluster.setDataPoints(points);
        return cluster;
    }

    /**
     * Represents a raw merge of two sets of points along with metadata about the merge.
     *
     * @param leftPoints  The list of points for the first source cluster.
     * @param rightPoints The list of points for the second source cluster.
     * @param distance    The distance at which the merge occurs.
     * @param step        The step of the merge.
     */
    public record RawMerge(List<String> leftPoints, List<String> rightPoints, java.math.BigDecimal distance, int step) {
    }
}

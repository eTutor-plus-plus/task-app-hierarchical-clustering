package at.jku.dke.task_app.hierarchical_clustering.clustering;

import at.jku.dke.task_app.hierarchical_clustering.generators.matrix.DistanceMetric;

import java.math.BigDecimal;
import java.util.List;

/**
 * Enum that represents different linkage methods.
 * <p>
 * A linkage method needs to implement the linkage function of
 * the functional interface {@linkplain Linkage} which
 * takes two clusters and a distance matrix to determine the new
 * distance between the clusters based on the distances of
 * their points, depending on the linkage method.
 */
public enum LinkageMethod {
    SINGLE((a, b, dist) -> {
        BigDecimal min = BigDecimal.valueOf(Double.MAX_VALUE);

        for (int i : a) {
            for (int j : b) {
                if (dist[i][j].compareTo(min) < 0) {
                    min = dist[i][j];
                }
            }
        }

        return min;
    }),

    COMPLETE((a, b, dist) -> {
        BigDecimal max = BigDecimal.valueOf(Double.MIN_VALUE);

        for (int i : a) {
            for (int j : b) {
                if (dist[i][j].compareTo(max) > 0) {
                    max = dist[i][j];
                }
            }
        }

        return max;
    });


    private final Linkage linkage;

    /**
     * Constructs a {@linkplain DistanceMetric} with the specific linkage function.
     *
     * @param linkage The linkage function of the metric.
     */
    LinkageMethod(Linkage linkage) {
        this.linkage = linkage;
    }

    /**
     * Calculates the new distance between two clusters using the distance matrix and this
     * distance metric's linkage function.
     *
     * @param a    The first cluster.
     * @param b    The second cluster.
     * @param dist The distance matrix.
     * @return The new distance between the two clusters.
     */
    public BigDecimal linkage(List<Integer> a, List<Integer> b, BigDecimal[][] dist) {
        return linkage.linkage(a, b, dist);
    }


    /**
     * Functional interface defining the function for linkage methods.
     */
    @FunctionalInterface
    private interface Linkage {
        BigDecimal linkage(List<Integer> a, List<Integer> b, BigDecimal[][] dist);
    }

}

package at.jku.dke.task_app.hierarchical_clustering.generators.matrix;

import at.jku.dke.task_app.hierarchical_clustering.data.entities.HierarchicalClusteringTask;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.function.BiFunction;

/**
 * Enum that represents different distance metrics.
 * <p>
 * A metric needs to implement a distance function which
 * takes two points in a coordinate system and calculates
 * the distance between them using the metric-specific
 * calculation.
 */
public enum DistanceMetric {

    EUCLIDEAN((p1, p2) -> {
        BigDecimal sum = BigDecimal.ZERO;

        BigDecimal diffX = p1.getX().subtract(p2.getX());
        sum = sum.add(diffX.multiply(diffX));
        BigDecimal diffY = p1.getY().subtract(p2.getY());
        sum = sum.add(diffY.multiply(diffY));

        return sum.sqrt(MathContext.DECIMAL128).stripTrailingZeros();
    }),

    MANHATTAN((p1, p2) -> {
        BigDecimal sum = BigDecimal.ZERO;

        sum = sum.add(p1.getX().subtract(p2.getX()).abs());
        sum = sum.add(p1.getY().subtract(p2.getY()).abs());

        return sum.stripTrailingZeros();
    });


    private final BiFunction<HierarchicalClusteringTask.CoordinatePoint, HierarchicalClusteringTask.CoordinatePoint, BigDecimal> distanceFunction;

    /**
     * Constructs a {@linkplain DistanceMetric} with the specific distance function.
     *
     * @param distanceFunction The distance function of the metric.
     */
    DistanceMetric(BiFunction<HierarchicalClusteringTask.CoordinatePoint, HierarchicalClusteringTask.CoordinatePoint, BigDecimal> distanceFunction) {
        this.distanceFunction = distanceFunction;
    }

    /**
     * Calculates the distance between two points in a coordinate system using this
     * distance metric's distance function.
     *
     * @param p1 The first point.
     * @param p2 The second point.
     * @return The distance between the points.
     */
    public BigDecimal distance(HierarchicalClusteringTask.CoordinatePoint p1, HierarchicalClusteringTask.CoordinatePoint p2) {
        return this.distanceFunction.apply(p1, p2);
    }

}

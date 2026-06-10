package at.jku.dke.task_app.hierarchical_clustering.generators.matrix;

import at.jku.dke.task_app.hierarchical_clustering.data.entities.HierarchicalClusteringTask;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.function.BiFunction;

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

    DistanceMetric(BiFunction<HierarchicalClusteringTask.CoordinatePoint, HierarchicalClusteringTask.CoordinatePoint, BigDecimal> distanceFunction) {
        this.distanceFunction = distanceFunction;
    }

    public BigDecimal distance(HierarchicalClusteringTask.CoordinatePoint p1, HierarchicalClusteringTask.CoordinatePoint p2) {
        return this.distanceFunction.apply(p1, p2);
    }

}

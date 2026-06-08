package at.jku.dke.task_app.hierarchical_clustering.generators.matrix;

import at.jku.dke.task_app.hierarchical_clustering.data.entities.HierarchicalClusteringTask;

import java.util.function.BiFunction;

public enum DistanceMetric {

    EUCLIDEAN((p1, p2) -> {
        double sum = 0.0;

        double diffX = p1.getX() - p2.getX();
        sum += diffX * diffX;
        double diffY = p1.getY() - p2.getY();
        sum += diffY * diffY;

        return Math.sqrt(sum);
    }),

    MANHATTAN((p1, p2) -> {
        double sum = 0.0;

        sum += Math.abs(p1.getX() - p2.getX());
        sum += Math.abs(p1.getY() - p2.getY());

        return ((double) Math.round(sum * 10)) / 10; // rounded to eliminate floating point calculation problems
    });


    private final BiFunction<HierarchicalClusteringTask.CoordinatePoint, HierarchicalClusteringTask.CoordinatePoint, Double> distanceFunction;

    DistanceMetric(BiFunction<HierarchicalClusteringTask.CoordinatePoint, HierarchicalClusteringTask.CoordinatePoint, Double> distanceFunction) {
        this.distanceFunction = distanceFunction;
    }

    public double distance(HierarchicalClusteringTask.CoordinatePoint p1, HierarchicalClusteringTask.CoordinatePoint p2) {
        return this.distanceFunction.apply(p1, p2);
    }

}

package at.jku.dke.task_app.hierarchical_clustering.generators;

import at.jku.dke.task_app.hierarchical_clustering.data.entities.HierarchicalClusteringTask;
import at.jku.dke.task_app.hierarchical_clustering.generators.matrix.DistanceMetric;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DistanceMetricTest {

    @Test
    void testEuclideanDistance() {
        // Arrange
        DistanceMetric euclideanMetric = DistanceMetric.EUCLIDEAN;
        HierarchicalClusteringTask.CoordinatePoint p1 = new HierarchicalClusteringTask.CoordinatePoint("1", BigDecimal.valueOf(2.0), BigDecimal.valueOf(3.5));
        HierarchicalClusteringTask.CoordinatePoint p2 = new HierarchicalClusteringTask.CoordinatePoint("2", BigDecimal.valueOf(4.7), BigDecimal.valueOf(9.3));
        HierarchicalClusteringTask.CoordinatePoint p3 = new HierarchicalClusteringTask.CoordinatePoint("3", BigDecimal.valueOf(10.8), BigDecimal.valueOf(2.0));
        HierarchicalClusteringTask.CoordinatePoint p4 = new HierarchicalClusteringTask.CoordinatePoint("4", BigDecimal.valueOf(68.2), BigDecimal.valueOf(100.0));

        // Act
        BigDecimal dist1 = euclideanMetric.distance(p1, p2).setScale(15, RoundingMode.HALF_UP);
        BigDecimal dist2 = euclideanMetric.distance(p1, p3).setScale(15, RoundingMode.HALF_UP);
        BigDecimal dist3 = euclideanMetric.distance(p2, p3).setScale(15, RoundingMode.HALF_UP);
        BigDecimal dist4 = euclideanMetric.distance(p3, p4).setScale(15, RoundingMode.HALF_UP);

        // Assert - use distance calculated using the euclidean distance formula
        assertEquals(new BigDecimal("6.397655820689325"), dist1.stripTrailingZeros());
        assertEquals(new BigDecimal("8.926925562588724"), dist2.stripTrailingZeros());
        assertEquals(new BigDecimal("9.513148795220224"), dist3.stripTrailingZeros());
        assertEquals(new BigDecimal("113.572707989199588"), dist4.stripTrailingZeros());
    }

    @Test
    void testManhattanDistance() {
        // Arrange
        DistanceMetric manhattanMetric = DistanceMetric.MANHATTAN;
        HierarchicalClusteringTask.CoordinatePoint p1 = new HierarchicalClusteringTask.CoordinatePoint("1", BigDecimal.valueOf(2.0), BigDecimal.valueOf(3.5));
        HierarchicalClusteringTask.CoordinatePoint p2 = new HierarchicalClusteringTask.CoordinatePoint("2", BigDecimal.valueOf(4.7), BigDecimal.valueOf(9.3));
        HierarchicalClusteringTask.CoordinatePoint p3 = new HierarchicalClusteringTask.CoordinatePoint("3", BigDecimal.valueOf(10.8), BigDecimal.valueOf(2.0));
        HierarchicalClusteringTask.CoordinatePoint p4 = new HierarchicalClusteringTask.CoordinatePoint("4", BigDecimal.valueOf(68.2), BigDecimal.valueOf(100.0));

        // Act - rounded to two decimal places to eliminate floating point errors
        BigDecimal dist1 = manhattanMetric.distance(p1, p2);
        BigDecimal dist2 = manhattanMetric.distance(p1, p3);
        BigDecimal dist3 = manhattanMetric.distance(p2, p3);
        BigDecimal dist4 = manhattanMetric.distance(p3, p4);

        // Assert - use distance calculated using the manhattan distance formula
        assertEquals(new BigDecimal("8.5"), dist1);
        assertEquals(new BigDecimal("10.3"), dist2);
        assertEquals(new BigDecimal("13.4"), dist3);
        assertEquals(new BigDecimal("155.4"), dist4);
    }

    @Test
    void testNegativeCoordinates() {
        // Arrange
        HierarchicalClusteringTask.CoordinatePoint p1 = new HierarchicalClusteringTask.CoordinatePoint("1", BigDecimal.valueOf(2.0), BigDecimal.valueOf(-3.5));
        HierarchicalClusteringTask.CoordinatePoint p2 = new HierarchicalClusteringTask.CoordinatePoint("1", BigDecimal.valueOf(-8.9), BigDecimal.valueOf(5.8));

        // Act
        BigDecimal distEuclidean = DistanceMetric.EUCLIDEAN.distance(p1, p2).setScale(15, RoundingMode.HALF_UP);
        // rounded to two decimal places to eliminate floating point errors
        BigDecimal distManhattan = DistanceMetric.MANHATTAN.distance(p1, p2).setScale(15, RoundingMode.HALF_UP);

        // Assert
        assertTrue(distEuclidean.compareTo(BigDecimal.ZERO) > 0);
        assertEquals(BigDecimal.valueOf(14.328293687665674), distEuclidean.stripTrailingZeros());
        assertTrue(distManhattan.compareTo(BigDecimal.ZERO) > 0);
        assertEquals(BigDecimal.valueOf(20.2), distManhattan.stripTrailingZeros());
    }

}

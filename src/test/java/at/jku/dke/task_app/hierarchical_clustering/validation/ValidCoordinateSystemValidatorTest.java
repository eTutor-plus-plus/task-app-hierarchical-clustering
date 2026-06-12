package at.jku.dke.task_app.hierarchical_clustering.validation;

import at.jku.dke.task_app.hierarchical_clustering.data.entities.HierarchicalClusteringTask;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValidCoordinateSystemValidatorTest {

    private final ValidCoordinateSystemValidator validator = new ValidCoordinateSystemValidator();

    @Test
    void isValidNullList() {
        // Act & Assert
        assertTrue(validator.isValid(null, null));
    }

    @Test
    void isValidAllPositiveCoordinates() {
        // Arrange
        var points = List.of(new HierarchicalClusteringTask.CoordinatePoint("A", BigDecimal.valueOf(1), BigDecimal.valueOf(2)),
            new HierarchicalClusteringTask.CoordinatePoint("B", BigDecimal.valueOf(3), BigDecimal.valueOf(4)));
        var coordinateSystem = new HierarchicalClusteringTask.CoordinateSystem(0, 10, 0, 10, points);

        // Act & Assert
        assertTrue(validator.isValid(coordinateSystem, null));
    }

    @Test
    void isValidZeroCoordinates() {
        // Arrange
        var points = List.of(new HierarchicalClusteringTask.CoordinatePoint("A", BigDecimal.ZERO, BigDecimal.ZERO));
        var coordinateSystem = new HierarchicalClusteringTask.CoordinateSystem(0, 10, 0, 10, points);

        // Act & Assert
        assertTrue(validator.isValid(coordinateSystem, null));
    }

    @Test
    void isValidNegativeCoordinates() {
        // Arrange
        var points1 = List.of(new HierarchicalClusteringTask.CoordinatePoint("A", BigDecimal.valueOf(-1), BigDecimal.valueOf(2)));
        var coordinateSystem1 = new HierarchicalClusteringTask.CoordinateSystem(0, 10, 0, 10, points1);
        var points2 = List.of(new HierarchicalClusteringTask.CoordinatePoint("A", BigDecimal.valueOf(1), BigDecimal.valueOf(-2)));
        var coordinateSystem2 = new HierarchicalClusteringTask.CoordinateSystem(0, 10, 0, 10, points2);
        var points3 = List.of(new HierarchicalClusteringTask.CoordinatePoint("A", BigDecimal.valueOf(1), BigDecimal.valueOf(2)),
            new HierarchicalClusteringTask.CoordinatePoint("B", BigDecimal.valueOf(-1), BigDecimal.valueOf(2)));
        var coordinateSystem3 = new HierarchicalClusteringTask.CoordinateSystem(0, 10, 0, 10, points3);

        // Act & Assert
        assertTrue(validator.isValid(coordinateSystem1, null));
        assertTrue(validator.isValid(coordinateSystem2, null));
        assertTrue(validator.isValid(coordinateSystem3, null));
    }

    @Test
    void isValidAxisLength() {
        // Arrange
        HierarchicalClusteringTask.CoordinateSystem coordinateSystem1 = new HierarchicalClusteringTask.CoordinateSystem(0, 10, 0, 10, null);
        HierarchicalClusteringTask.CoordinateSystem coordinateSystem2 = new HierarchicalClusteringTask.CoordinateSystem(-10, 10, -10, 10, null);
        HierarchicalClusteringTask.CoordinateSystem coordinateSystem3 = new HierarchicalClusteringTask.CoordinateSystem(-10, 0, -10, 0, null);

        // Act & Assert
        assertTrue(validator.isValid(coordinateSystem1, null));
        assertTrue(validator.isValid(coordinateSystem2, null));
        assertTrue(validator.isValid(coordinateSystem3, null));
    }

    @Test
    void isInvalidAxisLength() {
        // Arrange
        HierarchicalClusteringTask.CoordinateSystem coordinateSystem1 = new HierarchicalClusteringTask.CoordinateSystem(10, 0, 0, 10, null);
        HierarchicalClusteringTask.CoordinateSystem coordinateSystem2 = new HierarchicalClusteringTask.CoordinateSystem(0, 10, 10, 0, null);
        HierarchicalClusteringTask.CoordinateSystem coordinateSystem3 = new HierarchicalClusteringTask.CoordinateSystem(10, 0, 10, 0, null);
        HierarchicalClusteringTask.CoordinateSystem coordinateSystem4 = new HierarchicalClusteringTask.CoordinateSystem(20, 10, 0, 0, null);

        // Act & Assert
        assertFalse(validator.isValid(coordinateSystem1, null));
        assertFalse(validator.isValid(coordinateSystem2, null));
        assertFalse(validator.isValid(coordinateSystem3, null));
        assertFalse(validator.isValid(coordinateSystem4, null));
    }
}

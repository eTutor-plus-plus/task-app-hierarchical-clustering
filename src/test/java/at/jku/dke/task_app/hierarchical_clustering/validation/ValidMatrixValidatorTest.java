package at.jku.dke.task_app.hierarchical_clustering.validation;

import at.jku.dke.task_app.hierarchical_clustering.data.entities.HierarchicalClusteringTask;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ValidMatrixValidatorTest {

    private final ValidMatrixValidator validator = new ValidMatrixValidator();

    @Test
    void isValid() {
        // Arrange
        var matrix = new HierarchicalClusteringTask.DistanceMatrix(
            List.of("A", "B", "C"),
            new BigDecimal[][]{
                {BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.valueOf(2)},
                {BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.valueOf(3)},
                {BigDecimal.valueOf(2), BigDecimal.valueOf(3), BigDecimal.ZERO}
            }
        );

        // Act & Assert
        assertTrue(validator.isValid(matrix, null));
    }

    @Test
    void isInvalidDimension() {
        // Arrange
        var matrix1 = new HierarchicalClusteringTask.DistanceMatrix(
            List.of("A", "B", "C"),
            new BigDecimal[][]{
                {BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.valueOf(2)},
                {BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.valueOf(3)},
                {BigDecimal.valueOf(2), BigDecimal.valueOf(3), BigDecimal.ZERO},
                {BigDecimal.valueOf(5), BigDecimal.valueOf(6), BigDecimal.ZERO}
            }
        );

        var matrix2 = new HierarchicalClusteringTask.DistanceMatrix(
            List.of("A", "B", "C"),
            new BigDecimal[][]{
                {BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.valueOf(2), BigDecimal.valueOf(6), BigDecimal.valueOf(4)},
                {BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.valueOf(3), BigDecimal.ONE, BigDecimal.valueOf(2)},
                {BigDecimal.valueOf(3), BigDecimal.valueOf(4), BigDecimal.valueOf(2), BigDecimal.valueOf(3), BigDecimal.ZERO}
            }
        );

        var matrix3 = new HierarchicalClusteringTask.DistanceMatrix(
            List.of("A", "B", "C"),
            new BigDecimal[][]{
                {BigDecimal.ZERO, BigDecimal.valueOf(4)},
                {BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.valueOf(3), BigDecimal.ONE, BigDecimal.valueOf(2), BigDecimal.valueOf(9), BigDecimal.valueOf(10)},
                {BigDecimal.valueOf(3), BigDecimal.valueOf(4), BigDecimal.valueOf(2)}
            }
        );

        // Act & Assert
        assertFalse(validator.isValid(matrix1, null));
        assertFalse(validator.isValid(matrix2, null));
        assertFalse(validator.isValid(matrix3, null));
    }

    @Test
    void isInvalidLabels() {
        // Arrange
        var matrix = new HierarchicalClusteringTask.DistanceMatrix(
            List.of("A", "B"),
            new BigDecimal[][]{
                {BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.valueOf(2)},
                {BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.valueOf(3)},
                {BigDecimal.valueOf(2), BigDecimal.valueOf(3), BigDecimal.ZERO}
            }
        );

        // Act & Assert
        assertFalse(validator.isValid(matrix, null));
    }

    @Test
    void isInvalidDiagonal() {
        // Arrange
        var matrix = new HierarchicalClusteringTask.DistanceMatrix(
            List.of("A", "B", "C"),
            new BigDecimal[][]{
                {BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.valueOf(2)},
                {BigDecimal.ONE, BigDecimal.ONE, BigDecimal.valueOf(3)},
                {BigDecimal.valueOf(2), BigDecimal.valueOf(3), BigDecimal.ZERO}
            }
        );

        // Act & Assert
        assertFalse(validator.isValid(matrix, null));
    }

    @Test
    void isInvalidSymmetry() {
        // Arrange
        var matrix = new HierarchicalClusteringTask.DistanceMatrix(
            List.of("A", "B", "C"),
            new BigDecimal[][]{
                {BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ONE},
                {BigDecimal.valueOf(2), BigDecimal.ZERO, BigDecimal.ONE},
                {BigDecimal.valueOf(3), BigDecimal.valueOf(4), BigDecimal.ZERO}
            }
        );

        // Act & Assert
        assertFalse(validator.isValid(matrix, null));
    }

    @Test
    void isInvalidZeroDistances() {
        // Arrange
        var matrix = new HierarchicalClusteringTask.DistanceMatrix(
            List.of("A", "B", "C"),
            new BigDecimal[][]{
                {BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO},
                {BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO},
                {BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO}
            }
        );

        // Act & Assert
        assertFalse(validator.isValid(matrix, null));
    }

    @Test
    void isInvalidNegativeDistance() {
        // Arrange
        var matrix = new HierarchicalClusteringTask.DistanceMatrix(
            List.of("A", "B", "C"),
            new BigDecimal[][]{
                {BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.valueOf(2)},
                {BigDecimal.valueOf(-1), BigDecimal.ZERO, BigDecimal.valueOf(3)},
                {BigDecimal.valueOf(2), BigDecimal.valueOf(3), BigDecimal.ZERO}
            }
        );

        // Act & Assert
        assertFalse(validator.isValid(matrix, null));
    }

    @Test
    void isValidDuplicateDistance() {
        // Arrange
        var matrix = new HierarchicalClusteringTask.DistanceMatrix(
            List.of("A", "B", "C"),
            new BigDecimal[][]{
                {BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.valueOf(2)},
                {BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ONE},
                {BigDecimal.valueOf(2), BigDecimal.ONE, BigDecimal.ZERO}
            }
        );

        // Act & Assert
        assertTrue(validator.isValid(matrix, null));
    }
}

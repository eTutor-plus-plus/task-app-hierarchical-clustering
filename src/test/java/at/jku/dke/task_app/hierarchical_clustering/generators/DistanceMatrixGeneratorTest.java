package at.jku.dke.task_app.hierarchical_clustering.generators;

import at.jku.dke.task_app.hierarchical_clustering.data.entities.HierarchicalClusteringTask;
import at.jku.dke.task_app.hierarchical_clustering.generators.matrix.DistanceMetric;
import at.jku.dke.task_app.hierarchical_clustering.generators.matrix.DistanceMatrixGenerator;
import at.jku.dke.task_app.hierarchical_clustering.validation.ValidMatrixValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class DistanceMatrixGeneratorTest {

    private final ValidMatrixValidator validator = new ValidMatrixValidator();

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(
            DistanceMatrixGenerator.Config.class,
            "step",
            new BigDecimal("0.5")
        );
    }

    @Test
    void testGenerateRandomMatrixSize() {
        // Arrange
        int n = 5;

        // Act
        HierarchicalClusteringTask.DistanceMatrix matrix = new DistanceMatrixGenerator().generate(n, new Random(1L));

        // Assert
        assertEquals(n, matrix.getLabels().size());
        assertEquals(n, matrix.getDistances().length);
        for (int i = 0; i < n; i++) {
            assertEquals(n, matrix.getDistances()[i].length);
        }
    }

    @Test
    void testGenerateRandomMatrixValid() {
        // Arrange
        int n = 10;

        // Act
        HierarchicalClusteringTask.DistanceMatrix matrix = new DistanceMatrixGenerator().generate(n, new Random(2L));

        // Assert
        assertTrue(validator.isValid(matrix, null));

        // no duplicates check in one triangle
        Set<BigDecimal> seenDistances = new HashSet<>();
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                assertTrue(seenDistances.add(matrix.getDistances()[i][j]));
            }
        }
    }

    @Test
    void testValidityAtScale() {
        // Arrange
        int n = 10;

        // Act & Assert
        for (int run = 0; run < 10000; run++) {
            HierarchicalClusteringTask.DistanceMatrix matrix = new DistanceMatrixGenerator().generate(n);

            assertTrue(validator.isValid(matrix, null));
            Set<BigDecimal> seenDistances = new HashSet<>();
            for (int i = 0; i < n; i++) {
                for (int j = i + 1; j < n; j++) {
                    assertTrue(seenDistances.add(matrix.getDistances()[i][j]));
                }
            }
        }
    }

    @Test
    void testGenerateRandomMatrixValidity() {
        // Arrange
        int n = 1;
        int m = 2;

        // Act
        HierarchicalClusteringTask.DistanceMatrix matrix1 = new DistanceMatrixGenerator().generate(n, new Random(4L));
        HierarchicalClusteringTask.DistanceMatrix matrix2 = new DistanceMatrixGenerator().generate(m, new Random(5L));

        // Assert
        assertTrue(validator.isValid(matrix1, null));
        assertTrue(validator.isValid(matrix2, null));
    }

    @Test
    void testGenerateRandomMatrixWithSeed() {
        // Arrange
        DistanceMatrixGenerator generator = new DistanceMatrixGenerator();
        // the matrix generated from the seed:
        HierarchicalClusteringTask.DistanceMatrix expected = new HierarchicalClusteringTask.DistanceMatrix(
            List.of("1", "2", "3", "4", "5"),
            toBigDecimalMatrix(new double[][]{
                { 0.0,  1.5,  2.5,  0.5,  6.0},
                { 1.5,  0.0,  5.5,  2.0,  5.0},
                { 2.5,  5.5,  0.0,  7.5,  8.0},
                { 0.5,  2.0,  7.5,  0.0,  7.0},
                { 6.0,  5.0,  8.0,  7.0,  0.0,}
            }
        ));

        // Act
        HierarchicalClusteringTask.DistanceMatrix matrix = generator.generate(5, new Random(3L));


        // Assert
        for (int i = 0; i < matrix.getDistances().length; i++) {
            for (int j = 0; j < matrix.getDistances()[i].length; j++) {
                assertEquals(expected.getDistances()[i][j].stripTrailingZeros(), matrix.getDistances()[i][j].stripTrailingZeros());
            }
        }
    }

    @Test
    void testComputeMatrixFromCoordinatesValid() {
        // Arrange - coordinate list is made to result in proper distance matrices from either euclidean or manhattan distance calculation
        List<HierarchicalClusteringTask.CoordinatePoint> coordinateList = List.of(
            new HierarchicalClusteringTask.CoordinatePoint("1", BigDecimal.valueOf(3.9), BigDecimal.valueOf(-1.2)),
            new HierarchicalClusteringTask.CoordinatePoint("2", BigDecimal.valueOf(-7.3), BigDecimal.valueOf(-7.8)),
            new HierarchicalClusteringTask.CoordinatePoint("3", BigDecimal.valueOf(2.7), BigDecimal.valueOf(-0.3)),
            new HierarchicalClusteringTask.CoordinatePoint("4", BigDecimal.valueOf(-0.1), BigDecimal.valueOf(1.8)),
            new HierarchicalClusteringTask.CoordinatePoint("5", BigDecimal.valueOf(7.1), BigDecimal.valueOf(-3.6))
        );

        BigDecimal[][] expectedMatrixEuclidean = toBigDecimalMatrix(new double[][]{
            { 0.0, 13.0,  1.5,  5.0,  4.0},
            {13.0,  0.0, 12.5, 12.0, 15.0},
            { 1.5, 12.5,  0.0,  3.5,  5.5},
            { 5.0, 12.0,  3.5,  0.0,  9.0},
            { 4.0, 15.0,  5.5,  9.0,  0.0}
        });
        BigDecimal[][] expectedMatrixManhattan = toBigDecimalMatrix(new double[][]{
            { 0.0, 17.8,  2.1,  7.0,  5.6},
            {17.8,  0.0, 17.5, 16.8, 18.6},
            { 2.1, 17.5,  0.0,  4.9,  7.7},
            { 7.0, 16.8,  4.9,  0.0, 12.6},
            { 5.6, 18.6,  7.7, 12.6,  0.0}
        });

        // Act
        HierarchicalClusteringTask.DistanceMatrix euclideanMatrix = new DistanceMatrixGenerator().calculateMatrixFromCoordinates(coordinateList, DistanceMetric.EUCLIDEAN);
        HierarchicalClusteringTask.DistanceMatrix manhattanMatrix = new DistanceMatrixGenerator().calculateMatrixFromCoordinates(coordinateList, DistanceMetric.MANHATTAN);

        // Assert
        assertEquals(5, euclideanMatrix.getLabels().size());
        assertEquals(5, euclideanMatrix.getDistances().length);
        assertTrue(validator.isValid(euclideanMatrix, null));

        for (int i = 0; i < euclideanMatrix.getDistances().length; i++) {
            for (int j = 0; j < euclideanMatrix.getDistances()[i].length; j++) {
                assertEquals(expectedMatrixEuclidean[i][j].stripTrailingZeros(), euclideanMatrix.getDistances()[i][j].stripTrailingZeros());
            }
        }

        assertEquals(5, manhattanMatrix.getLabels().size());
        assertEquals(5, manhattanMatrix.getDistances().length);
        assertTrue(validator.isValid(manhattanMatrix, null));

        for (int i = 0; i < manhattanMatrix.getDistances().length; i++) {
            for (int j = 0; j < manhattanMatrix.getDistances()[i].length; j++) {
                assertEquals(expectedMatrixManhattan[i][j].stripTrailingZeros(), manhattanMatrix.getDistances()[i][j].stripTrailingZeros());
            }
       }
    }

    @Test
    void testComputeMatrixFromArbitraryCoordinates() {
        // Arrange
        List<HierarchicalClusteringTask.CoordinatePoint> coordinateList = List.of(
            new HierarchicalClusteringTask.CoordinatePoint("1", BigDecimal.valueOf(4.565), BigDecimal.valueOf(2.1)),
            new HierarchicalClusteringTask.CoordinatePoint("2", BigDecimal.valueOf(9.2), BigDecimal.valueOf(3.5)),
            new HierarchicalClusteringTask.CoordinatePoint("3", BigDecimal.valueOf(111.11), BigDecimal.valueOf(94)),
            new HierarchicalClusteringTask.CoordinatePoint("4", BigDecimal.valueOf(8.1), BigDecimal.valueOf(5.9)),
            new HierarchicalClusteringTask.CoordinatePoint("5", BigDecimal.valueOf(12.34567), BigDecimal.valueOf(21.98453))
        );

        BigDecimal[][] expectedMatrixEuclidean = toBigDecimalMatrix(new double[][]{
            { 0.0,    4.84,  140.7,   5.19,  21.35},
            { 4.84,    0.0, 136.29,   2.64,  18.75},
            {140.7, 136.29,    0.0, 135.55, 122.23},
            { 5.19,   2.64, 135.55,    0.0,  16.64},
            {21.35,  18.75, 122.23,  16.64,    0.0}
        });
        BigDecimal[][] expectedMatrixManhattan = toBigDecimalMatrix(new double[][]{
            {   0.0,   6.04, 198.45,   7.34,  27.67},
            {  6.04,    0.0, 192.41,    3.5,  21.63},
            {198.45, 192.41,    0.0, 191.11, 170.78},
            {  7.34,    3.5, 191.11,    0.0,  20.33},
            { 27.67,  21.63, 170.78,  20.33,    0.0}
        });

        // Act
        HierarchicalClusteringTask.DistanceMatrix euclideanMatrix = new DistanceMatrixGenerator().calculateMatrixFromCoordinates(coordinateList, DistanceMetric.EUCLIDEAN);
        HierarchicalClusteringTask.DistanceMatrix manhattanMatrix = new DistanceMatrixGenerator().calculateMatrixFromCoordinates(coordinateList, DistanceMetric.MANHATTAN);

        // Assert
        assertEquals(5, euclideanMatrix.getLabels().size());
        assertEquals(5, euclideanMatrix.getDistances().length);
        assertTrue(validator.isValid(euclideanMatrix, null));

        for (int i = 0; i < euclideanMatrix.getDistances().length; i++) {
            for (int j = 0; j < euclideanMatrix.getDistances()[i].length; j++) {
                assertEquals(expectedMatrixEuclidean[i][j].stripTrailingZeros(), euclideanMatrix.getDistances()[i][j].stripTrailingZeros());
            }
        }

        assertEquals(5, manhattanMatrix.getLabels().size());
        assertEquals(5, manhattanMatrix.getDistances().length);
        assertTrue(validator.isValid(manhattanMatrix, null));

        for (int i = 0; i < manhattanMatrix.getDistances().length; i++) {
            for (int j = 0; j < manhattanMatrix.getDistances()[i].length; j++) {
                assertEquals(expectedMatrixManhattan[i][j].stripTrailingZeros(), manhattanMatrix.getDistances()[i][j].stripTrailingZeros());
            }
        }
    }

    private BigDecimal[][] toBigDecimalMatrix(double[][] values) {
        BigDecimal[][] result = new BigDecimal[values.length][];
        for (int i = 0; i < values.length; i++) {
            result[i] = new BigDecimal[values[i].length];
            for (int j = 0; j < values[i].length; j++) {
                result[i][j] = BigDecimal.valueOf(values[i][j]);
            }
        }
        return result;
    }
}

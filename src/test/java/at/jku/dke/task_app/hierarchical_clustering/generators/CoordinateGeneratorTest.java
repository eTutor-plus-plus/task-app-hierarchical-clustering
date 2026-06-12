package at.jku.dke.task_app.hierarchical_clustering.generators;

import at.jku.dke.task_app.hierarchical_clustering.data.entities.HierarchicalClusteringTask;
import at.jku.dke.task_app.hierarchical_clustering.generators.coordinates.CoordinateGenerator;
import at.jku.dke.task_app.hierarchical_clustering.generators.coordinates.EuclideanCoordinateGenerator;
import at.jku.dke.task_app.hierarchical_clustering.generators.coordinates.ManhattanCoordinateGenerator;
import at.jku.dke.task_app.hierarchical_clustering.validation.ValidCoordinateSystemValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class CoordinateGeneratorTest {

    private final ValidCoordinateSystemValidator validator = new ValidCoordinateSystemValidator();

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(
            ManhattanCoordinateGenerator.Config.class,
            "maxRestarts",
            150
        );

        ReflectionTestUtils.setField(
            ManhattanCoordinateGenerator.Config.class,
            "maxAttemptsPerPoint",
            200_000
        );

        ReflectionTestUtils.setField(
            EuclideanCoordinateGenerator.Config.class,
            "maxRestarts",
            50
        );

        ReflectionTestUtils.setField(
            EuclideanCoordinateGenerator.Config.class,
            "maxAttemptsPerPoint",
            200_000
        );
    }

    @Test
    void testGenerateCoordinatesSize() {
        // Arrange
        int n = 5;
        int m = 10;
        CoordinateGenerator euclideanGenerator = new EuclideanCoordinateGenerator();
        CoordinateGenerator manhattanGenerator = new ManhattanCoordinateGenerator();

        // Act
        List<HierarchicalClusteringTask.CoordinatePoint> euclideanCoordinateList = euclideanGenerator.generate(n, m);
        List<HierarchicalClusteringTask.CoordinatePoint> manhattanCoordinateList = manhattanGenerator.generate(n, m);

        // Assert
        assertEquals(5, euclideanCoordinateList.size());
        assertEquals(5, manhattanCoordinateList.size());
    }

    @Test
    void testGenerateCoordinatesValid() {
        // Arrange
        int n = 5;
        int length = 10;
        CoordinateGenerator euclideanGenerator = new EuclideanCoordinateGenerator();
        CoordinateGenerator manhattanGenerator = new ManhattanCoordinateGenerator();

        // Act
        List<HierarchicalClusteringTask.CoordinatePoint> euclideanCoordinateList = euclideanGenerator.generate(n, length);
        List<HierarchicalClusteringTask.CoordinatePoint> manhattanCoordinateList = manhattanGenerator.generate(n, length);

        // Assert
        assertTrue(validator.isValid(new HierarchicalClusteringTask.CoordinateSystem(0, 10, 0, 10, euclideanCoordinateList), null));
        assertTrue(validator.isValid(new HierarchicalClusteringTask.CoordinateSystem(0, 10, 0, 10, manhattanCoordinateList), null));
    }

    @Test
    void testGenerateCoordinatesAtScale() {
        // Arrange
        int n = 10;
        int min = -50;
        int max = 50;
        CoordinateGenerator euclideanGenerator = new EuclideanCoordinateGenerator();
        CoordinateGenerator manhattanGenerator = new ManhattanCoordinateGenerator();

        // Act & Assert - out-commented because it can take 20+ minutes to complete
//        for (int run = 0; run < 1000; run++) {
//            List<HierarchicalClusteringTask.CoordinatePoint> euclideanCoordinateList = euclideanGenerator.generate(n, min, max, min, max);
//            List<HierarchicalClusteringTask.CoordinatePoint> manhattanCoordinateList = manhattanGenerator.generate(n, min, max, min, max);
//
//            for (HierarchicalClusteringTask.CoordinatePoint p : euclideanCoordinateList) {
//                assertTrue(p.getX() >= min && p.getX() <= max);
//                assertTrue(p.getY() >= min && p.getY() <= max);
//            }
//
//            for (HierarchicalClusteringTask.CoordinatePoint p : manhattanCoordinateList) {
//                assertTrue(p.getX() >= min && p.getX() <= max);
//                assertTrue(p.getY() >= min && p.getY() <= max);
//            }
//        }
    }

    @Test
    void testGenerateCoordinatesWithTooManyPoints() {
        // Arrange
        int n = 100;
        int m = 10;
        CoordinateGenerator euclideanGenerator = new EuclideanCoordinateGenerator();
        CoordinateGenerator manhattanGenerator = new ManhattanCoordinateGenerator();

        // Act & Assert
        assertThrows(RuntimeException.class, () -> euclideanGenerator.generate(n, m));
        assertThrows(RuntimeException.class, () -> manhattanGenerator.generate(n, m));
    }

    @Test
    void testGenerateCoordinatesWithSeed() {
        // Arrange
        int n = 5;
        CoordinateGenerator euclideanGenerator = new EuclideanCoordinateGenerator();
        // coordinates generated with euclidean from seed:
        List<HierarchicalClusteringTask.CoordinatePoint> expectedEuclidean = List.of(
            new HierarchicalClusteringTask.CoordinatePoint("1", BigDecimal.valueOf(4.1), BigDecimal.valueOf(8.8)),
            new HierarchicalClusteringTask.CoordinatePoint("2", BigDecimal.valueOf(7.4), BigDecimal.valueOf(4.4)),
            new HierarchicalClusteringTask.CoordinatePoint("3", BigDecimal.valueOf(9.5), BigDecimal.valueOf(1.6)),
            new HierarchicalClusteringTask.CoordinatePoint("4", BigDecimal.valueOf(7.7), BigDecimal.valueOf(4.0)),
            new HierarchicalClusteringTask.CoordinatePoint("5", BigDecimal.valueOf(8.3), BigDecimal.valueOf(3.2))
        );

        CoordinateGenerator manhattanGenerator = new ManhattanCoordinateGenerator();
        // coordinates generated with manhattan from seed:
        List<HierarchicalClusteringTask.CoordinatePoint> expectedManhattan = List.of(
            new HierarchicalClusteringTask.CoordinatePoint("1", BigDecimal.valueOf(9.8), BigDecimal.valueOf(2.8)),
            new HierarchicalClusteringTask.CoordinatePoint("2", BigDecimal.valueOf(8.3), BigDecimal.valueOf(9.9)),
            new HierarchicalClusteringTask.CoordinatePoint("3", BigDecimal.valueOf(7.1), BigDecimal.valueOf(8.9)),
            new HierarchicalClusteringTask.CoordinatePoint("4", BigDecimal.valueOf(0.8), BigDecimal.valueOf(4.2)),
            new HierarchicalClusteringTask.CoordinatePoint("5", BigDecimal.valueOf(5.7), BigDecimal.valueOf(5.4))
        );

        // Act
        List<HierarchicalClusteringTask.CoordinatePoint> euclideanCoordinateList = euclideanGenerator.generate(n, new Random(1L));
        List<HierarchicalClusteringTask.CoordinatePoint> manhattanCoordinateList = manhattanGenerator.generate(n, new Random(2L));

        // Assert
        for (int i = 0; i < euclideanCoordinateList.size(); i++) {
            HierarchicalClusteringTask.CoordinatePoint expectedPoint = expectedEuclidean.get(i);
            HierarchicalClusteringTask.CoordinatePoint actualPoint = euclideanCoordinateList.get(i);

            assertEquals(expectedPoint.getLabel(), actualPoint.getLabel());
            assertEquals(expectedPoint.getX().stripTrailingZeros(), actualPoint.getX().stripTrailingZeros());
            assertEquals(expectedPoint.getY().stripTrailingZeros(), actualPoint.getY().stripTrailingZeros());
        }

        for (int i = 0; i < manhattanCoordinateList.size(); i++) {
            HierarchicalClusteringTask.CoordinatePoint expectedPoint = expectedManhattan.get(i);
            HierarchicalClusteringTask.CoordinatePoint actualPoint = manhattanCoordinateList.get(i);

            assertEquals(expectedPoint.getLabel(), actualPoint.getLabel());
            assertEquals(expectedPoint.getX().stripTrailingZeros(), actualPoint.getX().stripTrailingZeros());
            assertEquals(expectedPoint.getY().stripTrailingZeros(), actualPoint.getY().stripTrailingZeros());
        }
    }

}

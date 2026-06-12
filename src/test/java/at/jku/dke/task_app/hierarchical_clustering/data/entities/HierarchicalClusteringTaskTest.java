package at.jku.dke.task_app.hierarchical_clustering.data.entities;

import at.jku.dke.etutor.task_app.dto.TaskStatus;
import at.jku.dke.task_app.hierarchical_clustering.generators.matrix.DistanceMetric;
import at.jku.dke.task_app.hierarchical_clustering.clustering.LinkageMethod;
import at.jku.dke.task_app.hierarchical_clustering.dendrogram.DendrogramModel;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HierarchicalClusteringTaskTest {

    @Test
    void testDefaultConstructor() {
        // Arrange & Act
        var task = new HierarchicalClusteringTask();

        // Assert
        assertNull(task.getDistanceMetric());
        assertNull(task.getLinkageMethod());
        assertNull(task.getCoordinateSystem());
        assertNull(task.getDistanceMatrix());
        assertNull(task.getPointsPerCorrectCluster());
        assertNull(task.getWrongOrderPenalty());
        assertNotNull(task.getSolutionMergeHistory());
        assertTrue(task.getSolutionMergeHistory().isEmpty());
        assertNull(task.getDendrogramModel());
    }

    @Test
    void testConstructorWithMaxPointsAndStatus() {
        // Arrange
        final BigDecimal maxPoints = BigDecimal.TEN;
        final TaskStatus status = TaskStatus.APPROVED;

        // Act
        var task = new HierarchicalClusteringTask(maxPoints, status);

        // Assert
        assertEquals(maxPoints, task.getMaxPoints());
        assertEquals(status, task.getStatus());
    }

    @Test
    void testConstructorWithDistanceMatrix() {
        // Arrange
        final BigDecimal maxPoints = BigDecimal.TEN;
        final TaskStatus status = TaskStatus.APPROVED;
        final var matrix = new HierarchicalClusteringTask.DistanceMatrix(
            List.of("A", "B"), new BigDecimal[][]{{BigDecimal.ZERO, BigDecimal.ONE}, {BigDecimal.ONE, BigDecimal.ZERO}});
        final BigDecimal pointsPerCorrectCluster = new BigDecimal("2.50");

        // Act
        var task = new HierarchicalClusteringTask(maxPoints, status, matrix, pointsPerCorrectCluster);

        // Assert
        assertEquals(maxPoints, task.getMaxPoints());
        assertEquals(status, task.getStatus());
        assertEquals(matrix, task.getDistanceMatrix());
        assertEquals(pointsPerCorrectCluster, task.getPointsPerCorrectCluster());
    }

    @Test
    void testConstructorWithId() {
        // Arrange
        final Long id = 1L;
        final BigDecimal maxPoints = BigDecimal.TEN;
        final TaskStatus status = TaskStatus.APPROVED;
        final var matrix = new HierarchicalClusteringTask.DistanceMatrix(
            List.of("A", "B"), new BigDecimal[][]{{BigDecimal.ZERO, BigDecimal.ONE}, {BigDecimal.ONE, BigDecimal.ZERO}});
        final BigDecimal pointsPerCorrectCluster = new BigDecimal("2.50");

        // Act
        var task = new HierarchicalClusteringTask(id, maxPoints, status, matrix, pointsPerCorrectCluster);

        // Assert
        assertEquals(id, task.getId());
        assertEquals(maxPoints, task.getMaxPoints());
        assertEquals(status, task.getStatus());
        assertEquals(matrix, task.getDistanceMatrix());
        assertEquals(pointsPerCorrectCluster, task.getPointsPerCorrectCluster());
    }

    @Test
    void testGetSetDistanceMetric() {
        // Arrange
        var task = new HierarchicalClusteringTask();

        // Act
        task.setDistanceMetric(DistanceMetric.EUCLIDEAN);
        task.setDistanceMetric(DistanceMetric.MANHATTAN);

        // Assert
        assertEquals(DistanceMetric.MANHATTAN, task.getDistanceMetric());
    }

    @Test
    void testGetSetLinkageMethod() {
        // Arrange
        var task = new HierarchicalClusteringTask();

        // Act
        task.setLinkageMethod(LinkageMethod.SINGLE);
        task.setLinkageMethod(LinkageMethod.COMPLETE);

        // Assert
        assertEquals(LinkageMethod.COMPLETE, task.getLinkageMethod());
    }

    @Test
    void testGetSetCoordinateSystem() {
        // Arrange
        var task = new HierarchicalClusteringTask();
        var point1 = new HierarchicalClusteringTask.CoordinatePoint("A", BigDecimal.valueOf(1.0), BigDecimal.valueOf(2.0));
        var point2 = new HierarchicalClusteringTask.CoordinatePoint("B", BigDecimal.valueOf(3.0), BigDecimal.valueOf(4.0));
        var coordinateList = new HierarchicalClusteringTask.CoordinateSystem(0, 2, 0, 2, List.of(point1, point2));

        // Act
        task.setCoordinateSystem(coordinateList);

        // Assert
        assertEquals(coordinateList, task.getCoordinateSystem());
        assertEquals(2, task.getCoordinateSystem().getMaxX());
        assertEquals(2, task.getCoordinateSystem().getMaxY());
        assertEquals(List.of(point1, point2), task.getCoordinateSystem().getCoordinateList());
    }

    @Test
    void testGetSetDistanceMatrix() {
        // Arrange
        var task = new HierarchicalClusteringTask();
        var matrix = new HierarchicalClusteringTask.DistanceMatrix(
            List.of("A", "B", "C"), new BigDecimal[][]{
            {BigDecimal.valueOf(0), BigDecimal.valueOf(1), BigDecimal.valueOf(2)},
            {BigDecimal.valueOf(1), BigDecimal.valueOf(0), BigDecimal.valueOf(3)},
            {BigDecimal.valueOf(2), BigDecimal.valueOf(3), BigDecimal.valueOf(0)}
        });

        // Act
        task.setDistanceMatrix(matrix);

        // Assert
        assertEquals(matrix, task.getDistanceMatrix());
        assertEquals(List.of("A", "B", "C"), task.getDistanceMatrix().getLabels());
        assertEquals(3, task.getDistanceMatrix().getLabels().size());
        assertEquals(3, task.getDistanceMatrix().getDistances()[1].length);
    }

    @Test
    void testGetSetPointsPerCorrectCluster() {
        // Arrange
        var task1 = new HierarchicalClusteringTask();
        var task2 = new HierarchicalClusteringTask();

        // Act
        task1.setPointsPerCorrectCluster(new BigDecimal("1.50"));
        task2.setPointsPerCorrectCluster(BigDecimal.ZERO);

        // Assert
        assertEquals(new BigDecimal("1.50"), task1.getPointsPerCorrectCluster());
        assertEquals(0, task1.getPointsPerCorrectCluster().compareTo(new BigDecimal("1.5")));
        assertEquals(1.5, task1.getPointsPerCorrectCluster().doubleValue());
        assertEquals(BigDecimal.ZERO, task2.getPointsPerCorrectCluster());
    }

    @Test
    void testGetSetWrongOrderPenalty() {
        // Arrange
        var task1 = new HierarchicalClusteringTask();
        var task2 = new HierarchicalClusteringTask();

        // Act
        task1.setWrongOrderPenalty(new BigDecimal("2"));
        task2.setWrongOrderPenalty(null);

        // Assert
        assertEquals(new BigDecimal("2"), task1.getWrongOrderPenalty());
        assertEquals(0, task1.getWrongOrderPenalty().compareTo(new BigDecimal("2.0")));
        assertEquals(2, task1.getWrongOrderPenalty().doubleValue());
        assertNull(task2.getWrongOrderPenalty());
    }

    @Test
    void testGetSetSolutionMergeHistory() {
        // Arrange
        var task = new HierarchicalClusteringTask();
        var merge1 = new HierarchicalClusteringMerge();
        var merge2 = new HierarchicalClusteringMerge();

        // Act
        task.setSolutionMergeHistory(List.of(merge1, merge2));

        // Assert
        assertEquals(List.of(merge1, merge2), task.getSolutionMergeHistory());
    }

    @Test
    void testGetSetDendrogramModel() {
        // Arrange
        var task = new HierarchicalClusteringTask();
        var model = new DendrogramModel(List.of("A", "B", "C"), null);

        // Act
        task.setDendrogramModel(model);

        // Assert
        assertEquals(model, task.getDendrogramModel());
    }

    @Test
    void testCoordinatePoint() {
        // Arrange
        var point = new HierarchicalClusteringTask.CoordinatePoint("A", BigDecimal.valueOf(1), BigDecimal.valueOf(2));

        // Act
        point.setLabel("B");
        point.setX(BigDecimal.valueOf(3));
        point.setY(BigDecimal.valueOf(4));

        // Assert
        assertEquals("B", point.getLabel());
        assertEquals(BigDecimal.valueOf(3), point.getX());
        assertEquals(BigDecimal.valueOf(4), point.getY());
    }

    @Test
    void testCoordinateList() {
        // Arrange
        var point1 = new HierarchicalClusteringTask.CoordinatePoint("A", BigDecimal.valueOf(1), BigDecimal.valueOf(2));
        var point2 = new HierarchicalClusteringTask.CoordinatePoint("B", BigDecimal.valueOf(3), BigDecimal.valueOf(4));
        var coordinateList = new HierarchicalClusteringTask.CoordinateSystem(0, 2, 0, 2, List.of(point1, point2));

        // Act
        coordinateList.setMaxX(5);
        coordinateList.setMaxY(10);
        coordinateList.setCoordinateList(List.of(point2));

        // Assert
        assertEquals(5, coordinateList.getMaxX());
        assertEquals(10, coordinateList.getMaxY());
        assertEquals(List.of(point2), coordinateList.getCoordinateList());
    }

    @Test
    void testDistanceMatrix() {
        // Arrange
        var matrix = new HierarchicalClusteringTask.DistanceMatrix(
            List.of("A", "B"),
            new BigDecimal[][]{
                {BigDecimal.ZERO, BigDecimal.ONE},
                {BigDecimal.ONE, BigDecimal.ZERO}
            }
        );

        BigDecimal[][] expected = new BigDecimal[][]{
            {BigDecimal.ZERO, BigDecimal.valueOf(2), BigDecimal.valueOf(3)},
            {BigDecimal.valueOf(2), BigDecimal.ZERO, BigDecimal.valueOf(4)},
            {BigDecimal.valueOf(3), BigDecimal.valueOf(4), BigDecimal.ZERO}
        };

        // Act
        matrix.setLabels(List.of("X", "Y", "Z"));
        matrix.setDistances(expected);

        // Assert
        assertEquals(List.of("X", "Y", "Z"), matrix.getLabels());
        assertTrue(Arrays.deepEquals(expected, matrix.getDistances()));
    }
}

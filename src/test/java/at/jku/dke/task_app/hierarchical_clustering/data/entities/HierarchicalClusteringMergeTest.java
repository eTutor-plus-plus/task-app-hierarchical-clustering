package at.jku.dke.task_app.hierarchical_clustering.data.entities;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class HierarchicalClusteringMergeTest {

    @Test
    void testDefaultConstructor() {
        // Arrange & Act
        var merge = new HierarchicalClusteringMerge();

        // Assert
        assertNull(merge.getId());
        assertNull(merge.getSourceCluster1());
        assertNull(merge.getSourceCluster2());
        assertNull(merge.getResult());
        assertNull(merge.getDistance());
        assertNull(merge.getStep());
        assertNull(merge.getTask());
    }

    @Test
    void testParameterizedConstructor() {
        // Arrange
        var left = new HierarchicalClusteringCluster();
        var right = new HierarchicalClusteringCluster();
        var result = new HierarchicalClusteringCluster();
        final BigDecimal distance = BigDecimal.valueOf(3.14);
        final int step = 2;

        // Act
        var merge = new HierarchicalClusteringMerge(left, right, result, distance, step);

        // Assert
        assertEquals(left, merge.getSourceCluster1());
        assertEquals(right, merge.getSourceCluster2());
        assertEquals(result, merge.getResult());
        assertEquals(distance, merge.getDistance());
        assertEquals(step, merge.getStep());
    }

    @Test
    void testGetSetId() {
        // Arrange
        var merge1 = new HierarchicalClusteringMerge();
        final UUID expected = UUID.randomUUID();
        var merge2 = new HierarchicalClusteringMerge();

        // Act
        merge1.setId(expected);
        merge2.setId(UUID.randomUUID());
        merge2.setId(null);

        // Assert
        assertEquals(expected, merge1.getId());
        assertNull(merge2.getId());
    }

    @Test
    void testGetSetClusterLeft() {
        // Arrange
        var merge = new HierarchicalClusteringMerge();
        var cluster1 = new HierarchicalClusteringCluster();
        var cluster2 = new HierarchicalClusteringCluster();
        List<String> expected = List.of("1", "2", "3");

        // Act
        cluster2.setDataPoints(expected);
        merge.setSourceCluster1(cluster1);
        merge.setSourceCluster1(cluster2);

        // Assert
        assertEquals(cluster2, merge.getSourceCluster1());
        assertEquals(expected, merge.getSourceCluster1().getDataPoints());
    }

    @Test
    void testGetSetClusterRight() {
        // Arrange
        var merge = new HierarchicalClusteringMerge();
        var cluster1 = new HierarchicalClusteringCluster();
        var cluster2 = new HierarchicalClusteringCluster();
        List<String> expected = List.of("1", "2", "3");

        // Act
        cluster2.setDataPoints(expected);
        merge.setSourceCluster2(cluster1);
        merge.setSourceCluster2(cluster2);

        // Assert
        assertEquals(cluster2, merge.getSourceCluster2());
        assertEquals(expected, merge.getSourceCluster2().getDataPoints());
    }

    @Test
    void testGetSetResult() {
        // Arrange
        var merge = new HierarchicalClusteringMerge();
        var cluster1 = new HierarchicalClusteringCluster();
        var cluster2 = new HierarchicalClusteringCluster();
        List<String> expected = List.of("1", "2", "3");

        // Act
        cluster2.setDataPoints(expected);
        merge.setResult(cluster1);
        merge.setResult(cluster2);

        // Assert
        assertEquals(cluster2, merge.getResult());
        assertEquals(expected, merge.getResult().getDataPoints());
    }

    @Test
    void testGetSetDistance() {
        // Arrange
        var merge1 = new HierarchicalClusteringMerge();
        var merge2 = new HierarchicalClusteringMerge();
        var merge3 = new HierarchicalClusteringMerge();

        // Act
        merge1.setDistance(BigDecimal.valueOf(3.14));
        merge2.setDistance(BigDecimal.ZERO);
        merge3.setDistance(BigDecimal.valueOf(-1.5)); // negative distances theoretically possible, but unrealistic since algorithms are robust

        // Assert
        assertEquals(BigDecimal.valueOf(3.14), merge1.getDistance());
        assertEquals(BigDecimal.ZERO, merge2.getDistance());
        assertEquals(BigDecimal.valueOf(-1.5), merge3.getDistance());
    }

    @Test
    void testGetSetStep() {
        // Arrange
        var merge1 = new HierarchicalClusteringMerge();
        var merge2 = new HierarchicalClusteringMerge();
        var merge3 = new HierarchicalClusteringMerge();

        // Act
        merge1.setStep(1);
        merge2.setStep(0);
        merge3.setStep(-1); // negative steps theoretically possible, but unrealistic since algorithms are robust

        // Assert
        assertEquals(1, merge1.getStep());
        assertEquals(0, merge2.getStep());
        assertEquals(-1, merge3.getStep());
    }

    @Test
    void testGetSetTask() {
        // Arrange
        var merge1 = new HierarchicalClusteringMerge();
        var merge2 = new HierarchicalClusteringMerge();
        var task = new HierarchicalClusteringTask();

        // Act
        merge1.setTask(task);
        merge2.setTask(task);
        merge2.setTask(null);

        // Assert
        assertEquals(task, merge1.getTask());
        assertNull(merge2.getTask());
    }

//    @Test
//    void testToString() {
//        // Arrange & Act
//        var result = new HierarchicalClusteringCluster();
//        result.setDataPoints(List.of("A", "B", "C"));
//        var merge1 = new HierarchicalClusteringMerge(new HierarchicalClusteringCluster(), new HierarchicalClusteringCluster(), result, 3.14, 1);
//        var merge2 = new HierarchicalClusteringMerge(new HierarchicalClusteringCluster(), new HierarchicalClusteringCluster(), result, 0.0, 1);
//
//        // Assert
//        assertEquals("Distance 3.14: (A,B,C)", merge1.toString());
//        assertEquals("Distance 0.0: (A,B,C)", merge2.toString());
//    }
}

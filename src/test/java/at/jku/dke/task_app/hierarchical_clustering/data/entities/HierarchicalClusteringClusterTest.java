package at.jku.dke.task_app.hierarchical_clustering.data.entities;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;


class HierarchicalClusteringClusterTest {

    @Test
    void testConstructor() {
        // Arrange & Act
        var cluster1 = new HierarchicalClusteringCluster();

        // Assert
        assertNull(cluster1.getId());
        assertNull(cluster1.getLabel());
        assertNull(cluster1.getDataPoints());
    }


    @Test
    void testGetSetId() {
        // Arrange
        var cluster1 = new HierarchicalClusteringCluster();
        final UUID expected = UUID.randomUUID();
        var cluster2 = new HierarchicalClusteringCluster();

        // Act
        cluster1.setId(expected);
        cluster2.setId(UUID.randomUUID());
        cluster2.setId(null);

        // Assert
        assertEquals(expected, cluster1.getId());
        assertNull(cluster2.getId());
    }

    @Test
    void testGetSetDataPoints() {
        // Arrange
        var cluster1 = new HierarchicalClusteringCluster();
        final List<String> expected = List.of("A", "B", "C");
        var cluster2 = new HierarchicalClusteringCluster();

        // Act
        cluster1.setDataPoints(expected);
        cluster2.setDataPoints(List.of("old1", "old2"));
        cluster2.setDataPoints(List.of("new1", "new2", "new3"));

        // Assert
        assertEquals(expected, cluster1.getDataPoints());
        assertEquals(List.of("new1", "new2", "new3"), cluster2.getDataPoints());
    }

    @Test
    void testLabelBehaviour() {
        // Arrange
        var cluster1 = new HierarchicalClusteringCluster();
        var cluster2 = new HierarchicalClusteringCluster();
        var cluster3 = new HierarchicalClusteringCluster();
        var cluster4 = new HierarchicalClusteringCluster();
        var cluster5 = new HierarchicalClusteringCluster();
        var cluster6 = new HierarchicalClusteringCluster();
        var cluster7 = new HierarchicalClusteringCluster();

        // Act
        cluster2.setDataPoints(List.of());
        cluster3.setDataPoints(List.of("OnlyPoint"));
        cluster4.setDataPoints(List.of("A", "B", "C"));
        cluster5.setDataPoints(List.of("Z", "A", "M"));
        cluster6.setDataPoints(List.of("W", "D", "C"));
        cluster6.setDataPoints(List.of("X", "Y", "Z"));
        cluster7.setDataPoints(List.of("W", "W", "W"));

        // Assert
        assertNull(cluster1.getLabel());
        assertEquals("()", cluster2.getFullLabel());
        assertEquals("(OnlyPoint)", cluster3.getFullLabel());
        assertEquals("(A,B,C)", cluster4.getFullLabel());
        assertEquals("(Z,A,M)", cluster5.getFullLabel());
        assertEquals("(X,Y,Z)", cluster6.getFullLabel());
        assertEquals("(W,W,W)", cluster7.getFullLabel());
    }
}

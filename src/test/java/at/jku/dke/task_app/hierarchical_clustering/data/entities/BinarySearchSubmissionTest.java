package at.jku.dke.task_app.hierarchical_clustering.data.entities;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BinarySearchSubmissionTest {

    @Test
    void testConstructor() {
        // Arrange
        var expected = "test";

        // Act
        var submission = new HierarchicalClusteringSubmission(expected);
        var actual = submission.getSubmission();

        // Assert
        assertEquals(expected, actual);
    }

    @Test
    void testGetSetSubmission() {
        // Arrange
        var submission = new HierarchicalClusteringSubmission();
        var expected = "test";

        // Act
        submission.setSubmission(expected);
        var actual = submission.getSubmission();

        // Assert
        assertEquals(expected, actual);
    }

}

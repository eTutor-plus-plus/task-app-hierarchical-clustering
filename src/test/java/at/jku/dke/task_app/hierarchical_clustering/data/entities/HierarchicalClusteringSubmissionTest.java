package at.jku.dke.task_app.hierarchical_clustering.data.entities;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class HierarchicalClusteringSubmissionTest {

    @Test
    void testDefaultConstructor() {
        // Arrange & Act
        var submission = new HierarchicalClusteringSubmission();

        // Assert
        assertNull(submission.getSubmission());
    }

    @Test
    void testConstructorWithSubmission() {
        // Arrange
        final String expected = "Distance 3.14: {A,B,C}";

        // Act
        var submission = new HierarchicalClusteringSubmission(expected);

        // Assert
        assertEquals(expected, submission.getSubmission());
    }

    @Test
    void testGetSetSubmission() {
        // Arrange
        var submission1 = new HierarchicalClusteringSubmission();
        var submission2 = new HierarchicalClusteringSubmission();

        // Act
        submission1.setSubmission("Distance 3.14: {A,B,C}");
        submission2.setSubmission("Distance 3.14: {A,B,C}\nDistance 1.0: {A,B,C,D}");

        // Assert
        assertEquals("Distance 3.14: {A,B,C}", submission1.getSubmission());
        assertEquals("Distance 3.14: {A,B,C}\nDistance 1.0: {A,B,C,D}", submission2.getSubmission());
    }
}

package at.jku.dke.task_app.hierarchical_clustering.services;

import at.jku.dke.etutor.task_app.dto.SubmissionMode;
import at.jku.dke.etutor.task_app.dto.SubmitSubmissionDto;
import at.jku.dke.task_app.hierarchical_clustering.data.entities.HierarchicalClusteringSubmission;
import at.jku.dke.task_app.hierarchical_clustering.dto.HierarchicalClusteringSubmissionDto;
import at.jku.dke.task_app.hierarchical_clustering.evaluation.EvaluationService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class BinarySearchSubmissionServiceTest {

    @Test
    void createSubmissionEntity() {
        // Arrange
        SubmitSubmissionDto<HierarchicalClusteringSubmissionDto> dto = new SubmitSubmissionDto<>("test-user", "test-quiz", 3L, "de", SubmissionMode.SUBMIT, 2, new HierarchicalClusteringSubmissionDto("33"));
        HierarchicalClusteringSubmissionService service = new HierarchicalClusteringSubmissionService(null, null, null);

        // Act
        HierarchicalClusteringSubmission submission = service.createSubmissionEntity(dto);

        // Assert
        assertEquals(dto.submission().input(), submission.getSubmission());
    }

    @Test
    void mapSubmissionToSubmissionData() {
        // Arrange
        HierarchicalClusteringSubmission submission = new HierarchicalClusteringSubmission("33");
        HierarchicalClusteringSubmissionService service = new HierarchicalClusteringSubmissionService(null, null, null);

        // Act
        HierarchicalClusteringSubmissionDto dto = service.mapSubmissionToSubmissionData(submission);

        // Assert
        assertEquals(submission.getSubmission(), dto.input());
    }

    @Test
    void evaluate() {
        // Arrange
        var evalService = mock(EvaluationService.class);
        SubmitSubmissionDto<HierarchicalClusteringSubmissionDto> dto = new SubmitSubmissionDto<>("test-user", "test-quiz", 3L, "de", SubmissionMode.SUBMIT, 2, new HierarchicalClusteringSubmissionDto("33"));
        HierarchicalClusteringSubmissionService service = new HierarchicalClusteringSubmissionService(null, null, evalService);

        // Act
        var result = service.evaluate(dto);

        // Assert
        verify(evalService).evaluate(dto);
    }

}

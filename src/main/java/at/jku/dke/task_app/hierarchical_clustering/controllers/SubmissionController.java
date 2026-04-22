package at.jku.dke.task_app.hierarchical_clustering.controllers;

import at.jku.dke.etutor.task_app.controllers.BaseSubmissionController;
import at.jku.dke.task_app.hierarchical_clustering.data.entities.HierarchicalClusteringSubmission;
import at.jku.dke.task_app.hierarchical_clustering.dto.HierarchicalClusteringSubmissionDto;
import at.jku.dke.task_app.hierarchical_clustering.services.HierarchicalClusteringSubmissionService;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for managing {@link HierarchicalClusteringSubmission}s.
 */
@RestController
public class SubmissionController extends BaseSubmissionController<HierarchicalClusteringSubmissionDto> {
    /**
     * Creates a new instance of class {@link SubmissionController}.
     *
     * @param submissionService The input service.
     */
    public SubmissionController(HierarchicalClusteringSubmissionService submissionService) {
        super(submissionService);
    }
}

package at.jku.dke.task_app.hierarchical_clustering.services;

import at.jku.dke.etutor.task_app.dto.GradingDto;
import at.jku.dke.etutor.task_app.dto.SubmitSubmissionDto;
import at.jku.dke.etutor.task_app.services.BaseSubmissionService;
import at.jku.dke.task_app.hierarchical_clustering.data.entities.BinarySearchSubmission;
import at.jku.dke.task_app.hierarchical_clustering.data.entities.BinarySearchTask;
import at.jku.dke.task_app.hierarchical_clustering.data.repositories.BinarySearchSubmissionRepository;
import at.jku.dke.task_app.hierarchical_clustering.data.repositories.BinarySearchTaskRepository;
import at.jku.dke.task_app.hierarchical_clustering.dto.BinarySearchSubmissionDto;
import at.jku.dke.task_app.hierarchical_clustering.evaluation.EvaluationService;
import org.springframework.stereotype.Service;

/**
 * This class provides methods for managing {@link BinarySearchSubmission}s.
 */
@Service
public class BinarySearchSubmissionService extends BaseSubmissionService<BinarySearchTask, BinarySearchSubmission, BinarySearchSubmissionDto> {

    private final EvaluationService evaluationService;

    /**
     * Creates a new instance of class {@link BinarySearchSubmissionService}.
     *
     * @param submissionRepository The input repository.
     * @param taskRepository       The task repository.
     * @param evaluationService    The evaluation service.
     */
    public BinarySearchSubmissionService(BinarySearchSubmissionRepository submissionRepository, BinarySearchTaskRepository taskRepository, EvaluationService evaluationService) {
        super(submissionRepository, taskRepository);
        this.evaluationService = evaluationService;
    }

    @Override
    protected BinarySearchSubmission createSubmissionEntity(SubmitSubmissionDto<BinarySearchSubmissionDto> submitSubmissionDto) {
        return new BinarySearchSubmission(submitSubmissionDto.submission().input());
    }

    @Override
    protected GradingDto evaluate(SubmitSubmissionDto<BinarySearchSubmissionDto> submitSubmissionDto) {
        return this.evaluationService.evaluate(submitSubmissionDto);
    }

    @Override
    protected BinarySearchSubmissionDto mapSubmissionToSubmissionData(BinarySearchSubmission submission) {
        return new BinarySearchSubmissionDto(submission.getSubmission());
    }

}

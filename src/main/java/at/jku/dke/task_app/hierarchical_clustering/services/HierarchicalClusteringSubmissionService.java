package at.jku.dke.task_app.hierarchical_clustering.services;

import at.jku.dke.etutor.task_app.dto.GradingDto;
import at.jku.dke.etutor.task_app.dto.SubmitSubmissionDto;
import at.jku.dke.etutor.task_app.services.BaseSubmissionService;
import at.jku.dke.task_app.hierarchical_clustering.data.entities.HierarchicalClusteringSubmission;
import at.jku.dke.task_app.hierarchical_clustering.data.entities.HierarchicalClusteringTask;
import at.jku.dke.task_app.hierarchical_clustering.data.repositories.HierarchicalClusteringSubmissionRepository;
import at.jku.dke.task_app.hierarchical_clustering.data.repositories.HierarchicalClusteringTaskRepository;
import at.jku.dke.task_app.hierarchical_clustering.dto.HierarchicalClusteringSubmissionDto;
import at.jku.dke.task_app.hierarchical_clustering.evaluation.EvaluationService;
import org.springframework.stereotype.Service;

/**
 * This class provides methods for managing {@link HierarchicalClusteringSubmission}s.
 */
@Service
public class HierarchicalClusteringSubmissionService extends BaseSubmissionService<HierarchicalClusteringTask, HierarchicalClusteringSubmission, HierarchicalClusteringSubmissionDto> {

    private final EvaluationService evaluationService;

    /**
     * Creates a new instance of class {@link HierarchicalClusteringSubmissionService}.
     *
     * @param submissionRepository The input repository.
     * @param taskRepository       The task repository.
     * @param evaluationService    The evaluation service.
     */
    public HierarchicalClusteringSubmissionService(HierarchicalClusteringSubmissionRepository submissionRepository, HierarchicalClusteringTaskRepository taskRepository, EvaluationService evaluationService) {
        super(submissionRepository, taskRepository);
        this.evaluationService = evaluationService;
    }

    @Override
    protected HierarchicalClusteringSubmission createSubmissionEntity(SubmitSubmissionDto<HierarchicalClusteringSubmissionDto> submitSubmissionDto) {
        return new HierarchicalClusteringSubmission(submitSubmissionDto.submission().input());
    }

    @Override
    protected GradingDto evaluate(SubmitSubmissionDto<HierarchicalClusteringSubmissionDto> submitSubmissionDto) {
        return this.evaluationService.evaluate(submitSubmissionDto);
    }

    @Override
    protected HierarchicalClusteringSubmissionDto mapSubmissionToSubmissionData(HierarchicalClusteringSubmission submission) {
        return new HierarchicalClusteringSubmissionDto(submission.getSubmission());
    }

}

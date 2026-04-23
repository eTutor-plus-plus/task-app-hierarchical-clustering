package at.jku.dke.task_app.hierarchical_clustering.evaluation;

import at.jku.dke.etutor.task_app.dto.CriterionDto;
import at.jku.dke.etutor.task_app.dto.GradingDto;
import at.jku.dke.etutor.task_app.dto.SubmitSubmissionDto;
import at.jku.dke.task_app.hierarchical_clustering.data.entities.HierarchicalClusteringMerge;
import at.jku.dke.task_app.hierarchical_clustering.data.entities.HierarchicalClusteringTask;
import at.jku.dke.task_app.hierarchical_clustering.data.repositories.HierarchicalClusteringTaskRepository;
import at.jku.dke.task_app.hierarchical_clustering.dto.HierarchicalClusteringSubmissionDto;
import at.jku.dke.task_app.hierarchical_clustering.evaluation.solution.HierarchicalClusteringSolution;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

/**
 * Service that evaluates submissions.
 */
@Service
public class EvaluationService {
    private static final Logger LOG = LoggerFactory.getLogger(EvaluationService.class);

    private final HierarchicalClusteringTaskRepository taskRepository;
    private final MessageSource messageSource;

    /**
     * Creates a new instance of class {@link EvaluationService}.
     *
     * @param taskRepository The task repository.
     * @param messageSource  The message source.
     */
    public EvaluationService(HierarchicalClusteringTaskRepository taskRepository, MessageSource messageSource) {
        this.taskRepository = taskRepository;
        this.messageSource = messageSource;
    }

    /**
     * Evaluates an input.
     *
     * @param submission The input to evaluate.
     * @return The evaluation result.
     */
    @Transactional
    public GradingDto evaluate(SubmitSubmissionDto<HierarchicalClusteringSubmissionDto> submission) {
        // find task
        var task = this.taskRepository.findById(submission.taskId()).orElseThrow(() -> new EntityNotFoundException("Task " + submission.taskId() + " does not exist."));

        // evaluate input
        LOG.info("Evaluating input for task {} with mode {} and feedback-level {}", submission.taskId(), submission.mode(), submission.feedbackLevel());
        Locale locale = Locale.of(submission.language());
        BigDecimal points = BigDecimal.ZERO;
        List<CriterionDto> criteria = new ArrayList<>();
        String feedback;

        // parse input
        List<SyntaxParser.HierarchicalClusteringMergeWrapper> input = null;
        IllegalArgumentException error = null;
        try {
            input = SyntaxParser.parse(submission.submission().input());
        } catch (IllegalArgumentException ex) {
            error = ex;
        }

        if (error != null) {
            criteria.add(new CriterionDto(
                this.messageSource.getMessage("criterium.syntax", null, locale),
                null,
                false,
                error.getMessage()));
        } else {
            criteria.add(new CriterionDto(
                this.messageSource.getMessage("criterium.syntax", null, locale),
                null,
                true,
                this.messageSource.getMessage("criterium.syntax.valid", null, locale)));
        }

        // evaluate and grade
        switch (submission.mode()) {
            case RUN:
                feedback = this.messageSource.getMessage("input", new Object[]{submission.submission().input()}, locale);
                break;
            case DIAGNOSE:
                if (error == null) {
                    points = evaluatePointsAndFeedback(task, input, submission.feedbackLevel(), criteria, locale);
                    feedback = this.messageSource.getMessage(Objects.equals(points, task.getMaxPoints()) ? "correct" : "incorrect", null, locale);
                } else {
                    feedback = this.messageSource.getMessage("incorrect", null, locale);
                }
                break;
            case SUBMIT:
                if (error == null) {
                    points = evaluatePoints(task, input);
                    feedback = this.messageSource.getMessage(Objects.equals(points, task.getMaxPoints()) ? "correct" : "incorrect", null, locale);
                } else
                    feedback = this.messageSource.getMessage("incorrect", null, locale);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + submission.mode());
        }

        return new GradingDto(task.getMaxPoints(), points, feedback, criteria);
    }

    private BigDecimal evaluatePoints(HierarchicalClusteringTask task,
                                      List<SyntaxParser.HierarchicalClusteringMergeWrapper> input) {
        return evaluatePointsAndFeedback(task, input, 0, null, null);
    }

    private BigDecimal evaluatePointsAndFeedback(HierarchicalClusteringTask task,
                                                 List<SyntaxParser.HierarchicalClusteringMergeWrapper> input,
                                                 Integer feedbackLevel, List<CriterionDto> criteria, Locale locale) {
        List<HierarchicalClusteringMerge> correctSolution = HierarchicalClusteringSolution.getSolution();
        BigDecimal achievedPoints = BigDecimal.ZERO;

        for (SyntaxParser.HierarchicalClusteringMergeWrapper merge : input) {
            if (correctSolution.stream().anyMatch(m -> matchesSolution(m, merge.merge(), feedbackLevel, criteria, locale))) {
                achievedPoints = achievedPoints.add(task.getPointsPerCorrectCluster());
            }
        }

        return achievedPoints;
    }

    private boolean matchesSolution(HierarchicalClusteringMerge solutionMerge, HierarchicalClusteringMerge inputMerge,
                                    Integer feedbackLevel, List<CriterionDto> criteria, Locale locale) {
        Set<String> solutionDataPoints = new HashSet<>(solutionMerge.getResult().getDataPoints());
        Set<String> inputDataPoints = new HashSet<>(inputMerge.getResult().getDataPoints());

        if (inputDataPoints.size() < inputMerge.getResult().getDataPoints().size()) {
            if (criteria != null && locale != null && feedbackLevel > 0) {
                criteria.add(new CriterionDto(
                    this.messageSource.getMessage("criterium.duplicate", null, locale),
                    BigDecimal.ZERO,
                    false,
                    this.messageSource.getMessage("criterium.duplicate.feedback", null, locale)
                ));
            }
            return false;
        }

        return solutionMerge.getDistance() == inputMerge.getDistance() && solutionDataPoints.equals(inputDataPoints);
    }

}

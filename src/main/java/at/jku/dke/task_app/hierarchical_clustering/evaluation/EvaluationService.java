package at.jku.dke.task_app.hierarchical_clustering.evaluation;

import at.jku.dke.etutor.task_app.dto.CriterionDto;
import at.jku.dke.etutor.task_app.dto.GradingDto;
import at.jku.dke.etutor.task_app.dto.SubmitSubmissionDto;
import at.jku.dke.task_app.hierarchical_clustering.data.entities.HierarchicalClusteringCluster;
import at.jku.dke.task_app.hierarchical_clustering.data.entities.HierarchicalClusteringMerge;
import at.jku.dke.task_app.hierarchical_clustering.data.entities.HierarchicalClusteringTask;
import at.jku.dke.task_app.hierarchical_clustering.data.repositories.HierarchicalClusteringTaskRepository;
import at.jku.dke.task_app.hierarchical_clustering.dto.HierarchicalClusteringSubmissionDto;
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

    private Integer feedbackLevel;
    private List<CriterionDto> criteria;
    private Locale locale;

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

        locale = Locale.of(submission.language());
        feedbackLevel = submission.feedbackLevel();
        criteria = new ArrayList<>();

        BigDecimal awardedPoints = BigDecimal.ZERO;
        String feedback;

        SyntaxParser parser = new SyntaxParser(this.messageSource);

        // parse input
        List<SyntaxParser.HierarchicalClusteringMergeWrapper> inputMergeHistory = null;
        IllegalArgumentException error = null;
        try {
            inputMergeHistory = parser.parse(submission.submission().input(), locale);
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
            case DIAGNOSE, SUBMIT:
                if (error == null) {
                    awardedPoints = evaluateWithFeedback(task, inputMergeHistory);
                    feedback = this.messageSource.getMessage(Objects.equals(awardedPoints, task.getMaxPoints()) ?
                        "correct" : awardedPoints.doubleValue() > 0 ? "partiallyCorrect": "incorrect", null, locale);
                } else {
                    feedback = this.messageSource.getMessage("incorrect", null, locale);
                }
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + submission.mode());
        }

        return new GradingDto(task.getMaxPoints(), awardedPoints, feedback, criteria);
    }

    private BigDecimal evaluateWithFeedback(HierarchicalClusteringTask task, List<SyntaxParser.HierarchicalClusteringMergeWrapper> inputMergeHistory) {
        List<HierarchicalClusteringMerge> solutionMergeHistory = task.getSolutionMergeHistory();
        BigDecimal achievedPoints = BigDecimal.ZERO;

        if (task.getWrongOrderPenalty() != null && task.getWrongOrderPenalty().compareTo(BigDecimal.ZERO) != 0) {
            inputMergeHistory = inputMergeHistory.stream()
                .sorted(Comparator.comparingDouble(w -> w.merge().getDistance()))
                .toList();
            boolean isCorrectOrder = true;

            for (int i = 0; i < inputMergeHistory.size(); i++) {
                HierarchicalClusteringMerge merge = inputMergeHistory.get(i).merge();

                int oldStep = merge.getStep();
                int newStep = i + 1;

                if (oldStep != newStep) {
                    isCorrectOrder = false;
                    if (feedbackLevel >= 2) {
                        wrongOrderFeedbackSpecific(merge, newStep);
                    }
                }

                merge.setStep(newStep);
            }

            if (!isCorrectOrder) {
                achievedPoints = achievedPoints.subtract(task.getWrongOrderPenalty());
                if (feedbackLevel == 1) {
                    addCriterion("criterium.order", task.getWrongOrderPenalty().negate(), "criterium.order.feedback");
                }
            }
        }

        List<SyntaxParser.HierarchicalClusteringMergeWrapper> wrongOrRedundantDistanceMerges = new ArrayList<>();
        Set<Double> foundDistances = new HashSet<>();

        List<HierarchicalClusteringMerge> foundSolutionMerges = new ArrayList<>();
        List<HierarchicalClusteringMerge> partiallyFoundSolutionMerges = new ArrayList<>();
        Map<HierarchicalClusteringMerge, List<HierarchicalClusteringMerge>> missingMerges = new HashMap<>();
        Set<HierarchicalClusteringMerge> redundantMerges = new HashSet<>();

        Map<HierarchicalClusteringMerge, Map<HierarchicalClusteringMerge, List<String>>> missingDataPoints = new HashMap<>();
        Map<HierarchicalClusteringMerge, Map<HierarchicalClusteringMerge, List<String>>> redundantDataPoints = new HashMap<>();
        Map<HierarchicalClusteringMerge, Map<HierarchicalClusteringMerge, List<String>>> duplicateDataPoints = new HashMap<>();

        for (SyntaxParser.HierarchicalClusteringMergeWrapper mergeWrapper : inputMergeHistory) {
            HierarchicalClusteringMerge inputMerge = mergeWrapper.merge();
            boolean correct = true;

            if (solutionMergeHistory.stream().noneMatch(m -> m.getDistance() == inputMerge.getDistance())) {
                correct = false;
                wrongOrRedundantDistanceMerges.add(mergeWrapper);
            } else {
                foundDistances.add(inputMerge.getDistance());

                for (HierarchicalClusteringMerge solutionMerge : solutionMergeHistory) {
                    if (solutionMerge.getDistance() != inputMerge.getDistance()) {
                        continue;
                    }

                    List<String> solutionDataPoints = new ArrayList<>(solutionMerge.getResult().getDataPoints());
                    List<String> inputDataPoints = new ArrayList<>(inputMerge.getResult().getDataPoints());

                    Collections.sort(solutionDataPoints);
                    Collections.sort(inputDataPoints);

                    if (solutionDataPoints.equals(inputDataPoints)) {
                        if (foundSolutionMerges.stream().anyMatch(m -> m.getResult().getDataPoints().equals(inputDataPoints))) {
                            correct = false;
                            redundantMerges.add(inputMerge);
                        } else {
                            foundSolutionMerges.add(solutionMerge);
                            break;
                        }
                    } else {
                        correct = false;
                        if (feedbackLevel > 0) {
                            if (foundSolutionMerges.contains(solutionMerge) || partiallyFoundSolutionMerges.contains(solutionMerge) ||
                                foundSolutionMerges.stream().anyMatch(
                                    m -> m.getResult().getDataPoints().stream().sorted().toList().equals(inputDataPoints)
                                ) || partiallyFoundSolutionMerges.stream().anyMatch(
                                    m -> m.getResult().getDataPoints().stream().sorted().toList().equals(inputDataPoints)
                            )) {
                                // if all input data points found in the solution are in a merge that has already been
                                // marked as a found merge -> mark merge as redundant
                                redundantMerges.add(inputMerge);
                            } else {
                                buildClusterFeedbackLists(missingDataPoints,
                                    redundantDataPoints,
                                    duplicateDataPoints,
                                    partiallyFoundSolutionMerges,
                                    missingMerges,
                                    redundantMerges,
                                    solutionDataPoints,
                                    inputDataPoints,
                                    solutionMerge,
                                    inputMerge);
                            }
                        }
                    }
                }
            }

            if (correct) {
                achievedPoints = achievedPoints.add(task.getPointsPerCorrectCluster());
            }
        }

        if (feedbackLevel >= 1) {
            for (HierarchicalClusteringMerge merge : foundSolutionMerges) {
                if (duplicateDataPoints.containsKey(merge)) {
                    redundantMerges.addAll(duplicateDataPoints.remove(merge).keySet());
                }

                if (missingDataPoints.containsKey(merge)) {
                    redundantMerges.addAll(missingDataPoints.remove(merge).keySet());
                }

                if (redundantDataPoints.containsKey(merge)) {
                    redundantMerges.addAll(redundantDataPoints.remove(merge).keySet());
                }
            }

            List<HierarchicalClusteringMerge> combinedFoundSolutionMerges = new ArrayList<>();
            combinedFoundSolutionMerges.addAll(foundSolutionMerges);
            combinedFoundSolutionMerges.addAll(partiallyFoundSolutionMerges);

            for (HierarchicalClusteringMerge merge : combinedFoundSolutionMerges) {
                if (missingMerges.containsKey(merge)) {
                    redundantMerges.addAll(missingMerges.remove(merge));
                }
            }


            if (!wrongOrRedundantDistanceMerges.isEmpty()) {
                wrongOrRedundantDistanceFeedback(wrongOrRedundantDistanceMerges);
            }

            missingDistancesFeedback(foundDistances, solutionMergeHistory);

            if (!duplicateDataPoints.isEmpty()) {
                duplicateDataPointsFeedback(duplicateDataPoints);
            }
            if (!missingDataPoints.isEmpty()) {
                missingPointsInClusterFeedback(missingDataPoints);
            }
            if (!redundantDataPoints.isEmpty()) {
                redundantPointsInClusterFeedback(redundantDataPoints);
            }

            if (!missingMerges.isEmpty()) {
                missingClusterFeedback(missingMerges.keySet());
            }
            if (!redundantMerges.isEmpty()) {
                redundantClusterFeedback(redundantMerges);
            }
        }

        return achievedPoints;
    }

    private void buildClusterFeedbackLists(Map<HierarchicalClusteringMerge, Map<HierarchicalClusteringMerge, List<String>>> missingDataPoints,
                                           Map<HierarchicalClusteringMerge, Map<HierarchicalClusteringMerge, List<String>>> redundantDataPoints,
                                           Map<HierarchicalClusteringMerge, Map<HierarchicalClusteringMerge, List<String>>> duplicateDataPoints,
                                           List<HierarchicalClusteringMerge> partiallyFoundSolutions,
                                           Map<HierarchicalClusteringMerge, List<HierarchicalClusteringMerge>> missingMerges,
                                           Set<HierarchicalClusteringMerge> redundantMerges,
                                           List<String> solutionDataPoints, List<String> inputDataPoints,
                                           HierarchicalClusteringMerge solutionMerge, HierarchicalClusteringMerge inputMerge) {
        Set<String> foundPoints = new HashSet<>();
        List<String> missingPoints = new ArrayList<>();
        Set<String> redundantPoints = new HashSet<>();
        List<String> duplicates = new ArrayList<>();

        for (String point : inputDataPoints) {
            if (solutionDataPoints.contains(point)) {
                if (!foundPoints.add(point)) {
                    duplicates.add(point);
                }
            } else if (!redundantPoints.add(point)) {
                duplicates.add(point);
            }
        }

        for (String point : solutionDataPoints) {
            if (!foundPoints.contains(point)) {
                missingPoints.add(point);
            }
        }

        if (foundPoints.isEmpty()) {
            // if not a single point from the input merge is found in the solution -> mark merge as completely missing
            missingMerges.computeIfAbsent(solutionMerge, k -> new ArrayList<>()).add(inputMerge);
            redundantMerges.add(inputMerge);
        } else {
            // else add missing points and mark merge as partially found solution
            partiallyFoundSolutions.add(solutionMerge);
            if (!missingPoints.isEmpty()) {
                missingDataPoints.computeIfAbsent(solutionMerge, k -> new HashMap<>()).put(inputMerge, missingPoints);
            }
            if (!redundantPoints.isEmpty()) {
                redundantDataPoints.computeIfAbsent(solutionMerge, k -> new HashMap<>()).put(inputMerge, redundantPoints.stream().toList());
            }
            if (!duplicates.isEmpty()) {
                duplicateDataPoints.computeIfAbsent(solutionMerge, k -> new HashMap<>()).put(inputMerge, duplicates);
            }
        }
    }

    private void wrongOrderFeedbackSpecific(HierarchicalClusteringMerge merge, int newStep) {
        String criterium = "criterium.order";

        String solution = feedbackLevel == 3 ?
            " " + this.messageSource.getMessage(criterium + ".feedback.solution", new Object[]{newStep}, locale) : "";

        addCriterion(criterium, criterium + ".feedback.step", merge.getDistance(), solution);
    }

    private void wrongOrRedundantDistanceFeedback(List<SyntaxParser.HierarchicalClusteringMergeWrapper> wrongOrRedundantDistances) {
        String criterium = "criterium.distance";

        switch (this.feedbackLevel) {
            case 1:
                // only give the general "wrong distance" feedback once if feedback level is 1
                addCriterion(criterium, criterium + ".feedback", wrongOrRedundantDistances.size());
                break;
            case 2, 3:
                for (SyntaxParser.HierarchicalClusteringMergeWrapper wrapper : wrongOrRedundantDistances) {
                    addCriterion(criterium, criterium + ".feedback.distance", wrapper.merge().getDistance(), wrapper.line());
                }
                break;
        }
    }

    private void duplicateDataPointsFeedback(Map<HierarchicalClusteringMerge, Map<HierarchicalClusteringMerge, List<String>>> duplicateDataPoints) {
        String criterium = "criterium.duplicatePointInMerge";

        switch (this.feedbackLevel) {
            case 1:
                addCriterion(criterium, criterium + ".feedback", duplicateDataPoints.size());
                break;
            case 2:
                for (Map<HierarchicalClusteringMerge, List<String>> duplicateMap : duplicateDataPoints.values()) {
                    // for feedback level 2: display all distances where at least one merge contains a duplicate
                    // -> eliminate duplicate distances beforehand
                    Set<Double> distances = new HashSet<>(duplicateMap.keySet().stream()
                        .map(HierarchicalClusteringMerge::getDistance)
                        .distinct()
                        .toList());

                    for (double distance : distances) {
                        addCriterion(criterium, criterium + ".feedback.distance", distance);
                    }
                }
                break;
            case 3:
                for (Map<HierarchicalClusteringMerge, List<String>> duplicateMap : duplicateDataPoints.values()) {
                    for (HierarchicalClusteringMerge merge : duplicateMap.keySet()) {
                        HierarchicalClusteringCluster duplicatePointsCluster = new HierarchicalClusteringCluster();
                        duplicatePointsCluster.setDataPoints(duplicateMap.get(merge));
                        addCriterion(criterium, criterium + ".feedback.point", merge.getResult().getLabel(), merge.getDistance(), duplicatePointsCluster.getLabel());
                    }
                }
                break;
        }
    }

    private void missingPointsInClusterFeedback(Map<HierarchicalClusteringMerge, Map<HierarchicalClusteringMerge, List<String>>> missingDataPoints) {
        String criterium = "criterium.missingPointInMerge";

        switch (this.feedbackLevel) {
            case 1:
                addCriterion(criterium, criterium + ".feedback", missingDataPoints.size());
                break;
            case 2:
                for (Map<HierarchicalClusteringMerge, List<String>> missingMap : missingDataPoints.values()) {
                    Set<Double> distances = new HashSet<>(missingMap.keySet().stream()
                        .map(HierarchicalClusteringMerge::getDistance)
                        .distinct()
                        .toList());

                    for (double distance : distances) {
                        addCriterion(criterium, criterium + ".feedback.distance", distance);
                    }
                }
                break;
            case 3:
                for (Map<HierarchicalClusteringMerge, List<String>> missingMap : missingDataPoints.values()) {
                    for (HierarchicalClusteringMerge merge : missingMap.keySet()) {
                        HierarchicalClusteringCluster missingPointsCluster = new HierarchicalClusteringCluster();
                        missingPointsCluster.setDataPoints(missingMap.get(merge));
                        addCriterion(criterium, criterium + ".feedback.missing", merge.getResult().getLabel(), merge.getDistance(), missingPointsCluster.getLabel());
                    }
                }
                break;
        }
    }

    private void redundantPointsInClusterFeedback(Map<HierarchicalClusteringMerge, Map<HierarchicalClusteringMerge, List<String>>> redundantDataPoints) {
        String criterium = "criterium.redundantPointInMerge";

        switch (this.feedbackLevel) {
            case 1:
                addCriterion(criterium, criterium + ".feedback", redundantDataPoints.size());
                break;
            case 2:
                for (Map<HierarchicalClusteringMerge, List<String>> redundantMap : redundantDataPoints.values()) {
                    Set<Double> distances = new HashSet<>(redundantMap.keySet().stream()
                        .map(HierarchicalClusteringMerge::getDistance)
                        .distinct()
                        .toList());

                    for (double distance : distances) {
                        addCriterion(criterium, criterium + ".feedback.distance", distance);
                    }
                }
                break;
            case 3:
                for (Map<HierarchicalClusteringMerge, List<String>> redundantMap : redundantDataPoints.values()) {
                    for (HierarchicalClusteringMerge merge : redundantMap.keySet()) {
                        HierarchicalClusteringCluster missingPointsCluster = new HierarchicalClusteringCluster();
                        missingPointsCluster.setDataPoints(redundantMap.get(merge));
                        addCriterion(criterium, criterium + ".feedback.redundant", merge.getResult().getLabel(), merge.getDistance(), missingPointsCluster.getLabel());
                    }
                }
                break;
        }
    }

    private void missingDistancesFeedback(Set<Double> foundDistances, List<HierarchicalClusteringMerge> solutionMergeHistory) {
        String criterium = "criterium.missingDistance";
        List<Double> missingDistances = new ArrayList<>();

        for (HierarchicalClusteringMerge merge : solutionMergeHistory) {
            if (!foundDistances.contains(merge.getDistance())) {
                missingDistances.add(merge.getDistance());
            }
        }

        if (!missingDistances.isEmpty()) {
            switch (feedbackLevel) {
                case 1:
                    addCriterion("criterium.distance", criterium + ".feedback", missingDistances.size());
                    break;
                case 2:
                    for (HierarchicalClusteringMerge merge : solutionMergeHistory) {
                        if (missingDistances.contains(merge.getDistance())) {
                            addCriterion("criterium.distance", criterium + ".feedback.step", merge.getStep());
                        }
                    }
                    break;
                case 3:
                    for (HierarchicalClusteringMerge merge : solutionMergeHistory) {
                        if (missingDistances.contains(merge.getDistance())) {
                            addCriterion("criterium.distance", criterium + ".feedback.distance", merge.getDistance(), merge.getResult().getLabel(), merge.getStep());
                        }
                    }
                    break;
            }
        }
    }

    private void missingClusterFeedback(Set<HierarchicalClusteringMerge> missingMerges) {
        String criterium = "criterium.missingMerge";

        switch (feedbackLevel) {
            case 1:
                addCriterion(criterium, criterium + ".feedback", missingMerges.size());
                break;
            case 2:
                Set<Double> distances = new HashSet<>(missingMerges.stream()
                    .map(HierarchicalClusteringMerge::getDistance)
                    .distinct()
                    .toList());

                for (double distance : distances) {
                    addCriterion(criterium, criterium + ".feedback.distance", distance);
                }
                break;
            case 3:
                for (HierarchicalClusteringMerge merge : missingMerges) {
                    addCriterion(criterium, criterium + ".feedback.solution", merge.getDistance(), merge.getResult().getLabel());
                }
                break;
        }
    }

    private void redundantClusterFeedback(Set<HierarchicalClusteringMerge> redundantMerges) {
        String criterium = "criterium.redundantMerge";

        switch (this.feedbackLevel) {
            case 1:
                addCriterion(criterium, criterium + ".feedback", redundantMerges.size());
                break;
            case 2:
                Set<Double> distances = new HashSet<>(redundantMerges.stream()
                    .map(HierarchicalClusteringMerge::getDistance)
                    .distinct()
                    .toList());

                for (double distance : distances) {
                    addCriterion(criterium, criterium + ".feedback.distance", distance);
                }
                break;
            case 3:
                for (HierarchicalClusteringMerge merge : redundantMerges) {
                    addCriterion(criterium, criterium + ".feedback.solution", merge.getDistance(), merge.getResult().getLabel());
                }
                break;
        }
    }

    private void addCriterion(String criterionName, String feedback, Object... args) {
        addCriterion(criterionName, BigDecimal.ZERO, feedback, args);
    }

    private void addCriterion(String criterionName, BigDecimal points, String feedback, Object... args) {
        criteria.add(new CriterionDto(
            this.messageSource.getMessage(criterionName, null, locale),
            points,
            false,
            this.messageSource.getMessage(feedback, args, locale)
        ));
    }
}

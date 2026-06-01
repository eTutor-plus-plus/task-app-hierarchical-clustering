package at.jku.dke.task_app.hierarchical_clustering.evaluation;

import at.jku.dke.etutor.task_app.dto.CriterionDto;
import at.jku.dke.etutor.task_app.dto.GradingDto;
import at.jku.dke.etutor.task_app.dto.SubmitSubmissionDto;
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
import java.util.stream.Collectors;

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

        SyntaxParser parser = new SyntaxParser(this.messageSource, locale);

        // parse input
        SyntaxParser.MergeEventWrapper inputMergeHistory = null;
        IllegalArgumentException error = null;
        try {
            inputMergeHistory = parser.parse(submission.submission().input());
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

    private BigDecimal evaluateWithFeedback(HierarchicalClusteringTask task, SyntaxParser.MergeEventWrapper eventWrapper) {
        BigDecimal awardedPoints = BigDecimal.ZERO;
        EvaluationFeedbackBuilder feedbackBuilder = new EvaluationFeedbackBuilder(task, this.messageSource, this.locale, this.feedbackLevel, this.criteria);

        if (task.getWrongOrderPenalty() != null && task.getWrongOrderPenalty().compareTo(BigDecimal.ZERO) != 0 && !eventWrapper.isCorrectOrder()) {
            // TODO: find a way to give specific feedback concerning order of input
            awardedPoints = awardedPoints.subtract(task.getWrongOrderPenalty());
            if (feedbackLevel >= 1) { // should be == 1 if more specific order feedback is added
                feedbackBuilder.withWrongOrderGeneral();
            }
        }


        SortedMap<Double, MergeEventAtDistance> solutionMergeEvents = buildEvaluationMergeHistoryForTask(task);
        SortedMap<Double, MergeEventAtDistance> inputMergeEvents = eventWrapper.mergeEvents();

        List<Double> wrongOrSuperfluousDistances = new ArrayList<>();
        Set<Double> foundDistances = new HashSet<>();

        SortedMap<Double, List<HierarchicalClusteringMerge>> foundSolutionMerges = new TreeMap<>();
        SortedMap<Double, List<HierarchicalClusteringMerge>> partiallyFoundSolutionMerges = new TreeMap<>();

        SortedMap<Double, List<HierarchicalClusteringMerge>> superfluousMerges = new TreeMap<>();
        SortedMap<Double, List<HierarchicalClusteringMerge>> redundantMerges = new TreeMap<>();
        SortedMap<Double, List<HierarchicalClusteringMerge>> missingMerges = new TreeMap<>();

        SortedMap<Double, Map<HierarchicalClusteringMerge, List<String>>> missingDataPoints = new TreeMap<>();
        SortedMap<Double, Map<HierarchicalClusteringMerge, List<String>>> superfluousDataPoints = new TreeMap<>();
        SortedMap<Double, Map<HierarchicalClusteringMerge, List<String>>> duplicateDataPoints = new TreeMap<>();


        for (double distance : inputMergeEvents.keySet()) {
            boolean correct = true;

            if (!solutionMergeEvents.containsKey(distance)) {
                correct = false;
                wrongOrSuperfluousDistances.add(distance);
            } else {
                foundDistances.add(distance);

                List<HierarchicalClusteringMerge> incorrectMerges = new ArrayList<>();

                List<HierarchicalClusteringMerge> solutionMerges = solutionMergeEvents.get(distance).newMerges;
                solutionMerges.addAll(solutionMergeEvents.get(distance).inheritedMerges);
                List<HierarchicalClusteringMerge> inputMerges = inputMergeEvents.get(distance).newMerges;
                inputMerges.addAll(inputMergeEvents.get(distance).inheritedMerges);

                for (HierarchicalClusteringMerge inputMerge : inputMerges) {
                    if (solutionMerges.stream().anyMatch(m -> m.getResult().equals(inputMerge.getResult()))) {
                        if (foundSolutionMerges.get(distance) != null &&
                            foundSolutionMerges.get(distance).stream()
                                .anyMatch(m -> m.getResult().equals(inputMerge.getResult()))) {
                            correct = false; // TODO: decide whether superfluous/redundant clusters should count as wrong when rest of distance is correct
                            redundantMerges.computeIfAbsent(distance, k -> new ArrayList<>()).add(inputMerge);
                        } else {
                            HierarchicalClusteringMerge solutionMerge = solutionMerges.stream()
                                .filter(m -> m.getResult().equals(inputMerge.getResult()))
                                .findFirst().orElse(null);
                            foundSolutionMerges.computeIfAbsent(distance, k -> new ArrayList<>()).add(solutionMerge);
                        }
                    } else {
                        correct = false;
                        incorrectMerges.add(inputMerge);
                    }
                }


                if (feedbackLevel > 0) {
                    // build feedback for clusters marked as incorrect
                    Map<List<String>, List<HierarchicalClusteringMerge>> groupedByDataPoints = incorrectMerges.stream()
                            .collect(Collectors.groupingBy(m -> m.getResult().getDataPoints()));

                    for (List<HierarchicalClusteringMerge> group : groupedByDataPoints.values()) {
                        if (group.size() > 1) {
                            List<HierarchicalClusteringMerge> redundant = group.subList(1, group.size());
                            redundantMerges.computeIfAbsent(distance, k -> new ArrayList<>()).addAll(redundant);
                            incorrectMerges.removeAll(redundant);
                        }
                    }

                    for (HierarchicalClusteringMerge merge : incorrectMerges) {
                        buildClusterFeedbackLists(
                            merge,
                            solutionMerges,
                            foundSolutionMerges,
                            partiallyFoundSolutionMerges,
                            superfluousMerges,
                            missingDataPoints,
                            superfluousDataPoints,
                            duplicateDataPoints);
                    }

                    // add all clusters/merges that are still missing after considering found and partially found solutions
                    for (HierarchicalClusteringMerge merge : solutionMerges) {
                        if ((foundSolutionMerges.get(distance) == null || (foundSolutionMerges.get(distance) != null &&
                                foundSolutionMerges.get(distance).stream().noneMatch(m -> m != null && m.equals(merge)))) &&
                            (partiallyFoundSolutionMerges.get(distance) == null || (partiallyFoundSolutionMerges.get(distance) != null &&
                                partiallyFoundSolutionMerges.get(distance).stream().noneMatch(m -> m != null && m.equals(merge))))) {
                            correct = false;
                            missingMerges.computeIfAbsent(distance, k -> new ArrayList<>()).add(merge);
                        }
                    }
                }
            }

            if (correct) {
                awardedPoints = awardedPoints.add(solutionMergeEvents.get(distance).pointsForDistance);
            }
        }


        if (feedbackLevel > 0) {
            // compute missing distances
            List<Double> missingDistances = new ArrayList<>();

            for (double distance : solutionMergeEvents.keySet()) {
                if (!foundDistances.contains(distance)) {
                    missingDistances.add(distance);
                }
            }

            // feedback
            feedbackBuilder
                .withWrongOrSuperfluousDistances(wrongOrSuperfluousDistances)
                .withMissingDistances(missingDistances, solutionMergeEvents)
                .withSuperfluousMerges(superfluousMerges)
                .withRedundantMerges(redundantMerges)
                .withMissingMerges(missingMerges)
                .withDuplicateDataPoints(duplicateDataPoints)
                .withSuperfluousDataPoints(superfluousDataPoints)
                .withMissingDataPoints(missingDataPoints)
                .feedbackGroupedByDistance();
        }

        return awardedPoints;
    }

    public static SortedMap<Double, MergeEventAtDistance> buildEvaluationMergeHistoryForTask(HierarchicalClusteringTask task) {
        SortedMap<Double, MergeEventAtDistance> mergeEvents = new TreeMap<>();
        List<HierarchicalClusteringMerge> mergeHistory = task.getSolutionMergeHistory();
        SortedSet<Double> distances = mergeHistory.stream()
            .map(HierarchicalClusteringMerge::getDistance)
            .sorted()
            .collect(Collectors.toCollection(TreeSet::new));

        for (Double distance : distances) {
            BigDecimal pointsForDistance = BigDecimal.ZERO;

            // add old merges to be inherited from previous distances
            List<HierarchicalClusteringMerge> inheritedMerges = new ArrayList<>();
            for (HierarchicalClusteringMerge merge : mergeHistory) {
                if (merge.getDistance() < distance) {
                    // build new merge to set new distance for evaluation
                    HierarchicalClusteringMerge newMerge = new HierarchicalClusteringMerge();
                    newMerge.setDistance(distance);
                    newMerge.setTask(merge.getTask());
                    newMerge.setId(merge.getId());
                    newMerge.setResult(merge.getResult());
                    newMerge.setSourceCluster1(merge.getSourceCluster1());
                    newMerge.setSourceCluster2(merge.getSourceCluster2());
                    inheritedMerges.add(newMerge);
                }
            }

            // add new merges at this distance
            List<HierarchicalClusteringMerge> newMerges = new ArrayList<>();
            for (HierarchicalClusteringMerge merge : mergeHistory) {
                if (merge.getDistance() == distance) {
                    newMerges.add(merge);
                    pointsForDistance = pointsForDistance.add(task.getPointsPerCorrectCluster());
                }
            }

            // remove old merges that are merged into a new cluster at this distance
            inheritedMerges.removeIf(inheritedMerge -> newMerges.stream().anyMatch(m ->
                new HashSet<>(m.getResult().getDataPoints()).containsAll(inheritedMerge.getResult().getDataPoints())));
            // remove merges that are contained in another inherited merge
            inheritedMerges.removeIf(inheritedMerge -> inheritedMerges.stream().anyMatch(m ->
                m.getResult().getDataPoints() != inheritedMerge.getResult().getDataPoints() &&
                    new HashSet<>(m.getResult().getDataPoints()).containsAll(inheritedMerge.getResult().getDataPoints())));

            mergeEvents.put(distance, new MergeEventAtDistance(newMerges, inheritedMerges, pointsForDistance));
        }

        return mergeEvents;
    }

    private void buildClusterFeedbackLists(HierarchicalClusteringMerge inputMerge,
                                           List<HierarchicalClusteringMerge> solutionMerges,
                                           SortedMap<Double, List<HierarchicalClusteringMerge>> foundSolutionMerges,
                                           SortedMap<Double, List<HierarchicalClusteringMerge>> partiallyFoundSolutionMerges,
                                           SortedMap<Double, List<HierarchicalClusteringMerge>> superfluousMerges,
                                           SortedMap<Double, Map<HierarchicalClusteringMerge, List<String>>> missingDataPoints,
                                           SortedMap<Double, Map<HierarchicalClusteringMerge, List<String>>> superfluousDataPoints,
                                           SortedMap<Double, Map<HierarchicalClusteringMerge, List<String>>> duplicateDataPoints) {

        solutionMerges.removeAll(foundSolutionMerges.values().stream().flatMap(Collection::stream).toList());
        solutionMerges.removeAll(partiallyFoundSolutionMerges.values().stream().flatMap(Collection::stream).toList());

        // find best matching solution merge (i.e. the merge/cluster with the most matching data points compared to the input)
        HierarchicalClusteringMerge solutionMerge = solutionMerges.stream()
            .filter(m -> m.getResult().getDataPoints().stream().anyMatch(d -> inputMerge.getResult().getDataPoints().contains(d)))
            .max(Comparator.comparingInt(m -> (int) m.getResult().getDataPoints().stream()
                .filter(d -> inputMerge.getResult().getDataPoints().contains(d))
                .count()))
            .orElse(null);

        // if no data point of the input cluster matched with any point of the solution merges at this distance -> superfluous
        if (solutionMerge == null) {
            superfluousMerges.computeIfAbsent(inputMerge.getDistance(), k -> new ArrayList<>()).add(inputMerge);
            return;
        }

        Set<String> foundPoints = new HashSet<>();
        List<String> missingPoints = new ArrayList<>();
        Set<String> superfluousPoints = new HashSet<>();
        List<String> duplicates = new ArrayList<>();

        List<String> solutionDataPoints = solutionMerge.getResult().getDataPoints();
        List<String> inputDataPoints = inputMerge.getResult().getDataPoints();

        for (String point : inputDataPoints) {
            if (solutionDataPoints.contains(point)) {
                if (!foundPoints.add(point)) {
                    duplicates.add(point);
                }
            } else if (!superfluousPoints.add(point)) {
                duplicates.add(point);
            }
        }

        for (String point : solutionDataPoints) {
            if (!foundPoints.contains(point)) {
                missingPoints.add(point);
            }
        }

        // add missing/superfluous/duplicate points and mark merge as partially found solution
        partiallyFoundSolutionMerges.computeIfAbsent(inputMerge.getDistance(), k -> new ArrayList<>()).add(solutionMerge);
        if (!missingPoints.isEmpty()) {
            missingDataPoints.computeIfAbsent(inputMerge.getDistance(), k -> new HashMap<>()).put(inputMerge, missingPoints);
        }
        if (!superfluousPoints.isEmpty()) {
            superfluousDataPoints.computeIfAbsent(inputMerge.getDistance(), k -> new HashMap<>()).put(inputMerge, new ArrayList<>(superfluousPoints));
        }
        if (!duplicates.isEmpty()) {
            duplicateDataPoints.computeIfAbsent(inputMerge.getDistance(), k -> new HashMap<>()).put(inputMerge, duplicates);
        }
    }

    public record MergeEventAtDistance(
        List<HierarchicalClusteringMerge> newMerges,
        List<HierarchicalClusteringMerge> inheritedMerges,
        BigDecimal pointsForDistance) {}
}

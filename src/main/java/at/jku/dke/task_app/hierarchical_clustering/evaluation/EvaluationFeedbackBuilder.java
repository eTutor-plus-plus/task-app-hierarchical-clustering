package at.jku.dke.task_app.hierarchical_clustering.evaluation;

import at.jku.dke.etutor.task_app.dto.CriterionDto;
import at.jku.dke.task_app.hierarchical_clustering.data.entities.HierarchicalClusteringCluster;
import at.jku.dke.task_app.hierarchical_clustering.data.entities.HierarchicalClusteringMerge;
import at.jku.dke.task_app.hierarchical_clustering.data.entities.HierarchicalClusteringTask;
import at.jku.dke.task_app.hierarchical_clustering.generators.dendrogram.DendrogramImageExporter;
import at.jku.dke.task_app.hierarchical_clustering.generators.dendrogram.DendrogramSvgRenderer;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

public class EvaluationFeedbackBuilder {

    private final HierarchicalClusteringTask task;
    private final MessageSource messageSource;
    private final Locale locale;
    private final int feedbackLevel;
    private final List<CriterionDto> criteria;

    boolean isWrongOrder;

    SortedMap<Double, EvaluationService.MergeEventAtDistance> solutionMergeEvents;

    List<Double> wrongOrSuperfluousDistances;
    List<Double> missingDistances;

    SortedMap<Double, List<HierarchicalClusteringMerge>> superfluousMerges;
    SortedMap<Double, List<HierarchicalClusteringMerge>> redundantMerges;
    SortedMap<Double, List<HierarchicalClusteringMerge>> missingMerges;

    SortedMap<Double, Map<HierarchicalClusteringMerge, List<String>>> missingDataPoints;
    SortedMap<Double, Map<HierarchicalClusteringMerge, List<String>>> superfluousDataPoints;
    SortedMap<Double, Map<HierarchicalClusteringMerge, List<String>>> duplicateDataPoints;

    public EvaluationFeedbackBuilder(HierarchicalClusteringTask task, MessageSource messageSource, Locale locale, int feedbackLevel, List<CriterionDto> criteria) {
        this.task = task;
        this.messageSource = messageSource;
        this.locale = locale;
        this.feedbackLevel = feedbackLevel;
        this.criteria = criteria;
    }

    public EvaluationFeedbackBuilder withWrongOrderGeneral() {
        isWrongOrder = true;
        return this;
    }

    public EvaluationFeedbackBuilder withWrongOrSuperfluousDistances(List<Double> wrongOrSuperfluousDistances) {
        this.wrongOrSuperfluousDistances = wrongOrSuperfluousDistances;
        return this;
    }

    public EvaluationFeedbackBuilder withMissingDistances(List<Double> missingDistances, SortedMap<Double, EvaluationService.MergeEventAtDistance> solutionMergeEvents) {
        this.solutionMergeEvents = solutionMergeEvents;
        this.missingDistances = missingDistances;
        return this;
    }

    public EvaluationFeedbackBuilder withSuperfluousMerges(SortedMap<Double, List<HierarchicalClusteringMerge>> superfluousMerges) {
        this.superfluousMerges = superfluousMerges;
        return this;
    }

    public EvaluationFeedbackBuilder withRedundantMerges(SortedMap<Double, List<HierarchicalClusteringMerge>> redundantMerges) {
        this.redundantMerges = redundantMerges;
        return this;
    }

    public EvaluationFeedbackBuilder withMissingMerges(SortedMap<Double, List<HierarchicalClusteringMerge>> missingMerges) {
        this.missingMerges = missingMerges;
        return this;
    }

    public EvaluationFeedbackBuilder withMissingDataPoints(SortedMap<Double, Map<HierarchicalClusteringMerge, List<String>>> missingDataPoints) {
        this.missingDataPoints = missingDataPoints;
        return this;
    }

    public EvaluationFeedbackBuilder withSuperfluousDataPoints(SortedMap<Double, Map<HierarchicalClusteringMerge, List<String>>> superfluousDataPoints) {
        this.superfluousDataPoints = superfluousDataPoints;
        return this;
    }

    public EvaluationFeedbackBuilder withDuplicateDataPoints(SortedMap<Double, Map<HierarchicalClusteringMerge, List<String>>> duplicateDataPoints) {
        this.duplicateDataPoints = duplicateDataPoints;
        return this;
    }

    public void feedbackGroupedByDistance() {
        if (feedbackLevel <= 0) {
            return;
        }

        if (isWrongOrder) {
            wrongOrderFeedbackGeneral();
        }

        if (!wrongOrSuperfluousDistances.isEmpty()) {
            wrongOrSuperfluousDistanceFeedback(wrongOrSuperfluousDistances);
        }

        if (!missingDistances.isEmpty()) {
            missingDistancesFeedback(missingDistances);
        }

        SortedSet<Double> distances = task.getSolutionMergeHistory().stream()
            .map(HierarchicalClusteringMerge::getDistance)
            .collect(Collectors.toCollection(TreeSet::new));

        // alternative feedback for redundant merges if they are not to be counted as a mistake
//        for (double distance : redundantMerges.keySet()) {
//            String criterium = "criterium.redundantCluster";
//            switch (feedbackLevel) {
//                case 1:
//                    addCriterion(criterium, BigDecimal.ZERO, criterium + ".feedback", redundantMerges.get(distance).size());
//                    break;
//                case 2:
//                    addCriterion(criterium, BigDecimal.ZERO, criterium + ".feedback.distance", distance, redundantMerges.get(distance).size());
//                    break;
//                case 3:
//                    List<String> resultLabels = new ArrayList<>();
//                    for (HierarchicalClusteringMerge merge : redundantMerges.get(distance)) {
//                        resultLabels.add(merge.getResult().getFullLabel());
//                    }
//
//                    addCriterion(criterium, BigDecimal.ZERO, criterium + ".feedback.solution", distance, String.join(", ", resultLabels));
//                    break;
//            }
//        }

        for (double distance : distances) {
            if (!hasFeedback(distance)) {
                continue;
            }

            addDistanceToEvaluation(distance);

            if (superfluousMerges.containsKey(distance)) {
                mergeFeedback("criterium.superfluousCluster", distance, superfluousMerges.get(distance));
            }

            if (redundantMerges.containsKey(distance)) {
                mergeFeedback("criterium.redundantCluster", distance, redundantMerges.get(distance));
            }

            if (missingMerges.containsKey(distance)) {
                mergeFeedback("criterium.missingCluster", distance, missingMerges.get(distance));
            }

            if (superfluousDataPoints.containsKey(distance)) {
                pointInClusterFeedback("criterium.superfluousPointInCluster", distance, superfluousDataPoints.get(distance));
            }

            if (duplicateDataPoints.containsKey(distance)) {
                pointInClusterFeedback("criterium.duplicatePointInCluster", distance, duplicateDataPoints.get(distance));
            }

            if (missingDataPoints.containsKey(distance)) {
                pointInClusterFeedback("criterium.missingPointInCluster", distance, missingDataPoints.get(distance));
            }
        }

        attachSolutionAndDendrogram();
    }

    public void feedbackGroupedByType() {
        if (feedbackLevel <= 0) {
            return;
        }

        if (isWrongOrder) {
            wrongOrderFeedbackGeneral();
        }

        if (!wrongOrSuperfluousDistances.isEmpty()) {
            wrongOrSuperfluousDistanceFeedback(wrongOrSuperfluousDistances);
        }

        missingDistancesFeedback(missingDistances);

        for (double distance : duplicateDataPoints.keySet()) {
            pointInClusterFeedback("criterium.duplicatePointInCluster", distance, duplicateDataPoints.get(distance));
        }

        for (double distance : missingDataPoints.keySet()) {
            pointInClusterFeedback("criterium.missingPointInCluster", distance, missingDataPoints.get(distance));
        }

        for (double distance : superfluousDataPoints.keySet()) {
            pointInClusterFeedback("criterium.superfluousPointInCluster", distance, superfluousDataPoints.get(distance));
        }

        for (double distance : missingMerges.keySet()) {
            mergeFeedback("criterium.missingCluster", distance, missingMerges.get(distance));
        }

        for (double distance : redundantMerges.keySet()) {
            mergeFeedback("criterium.redundantCluster", distance, redundantMerges.get(distance));
        }

        for (double distance : superfluousMerges.keySet()) {
            mergeFeedback("criterium.superfluousCluster", distance, superfluousMerges.get(distance));
        }

        attachSolutionAndDendrogram();
    }

    private void wrongOrderFeedbackGeneral() {
        addCriterion("criterium.order", task.getWrongOrderPenalty().negate(), "criterium.order.feedback");
    }

    private void wrongOrderFeedbackSpecific(HierarchicalClusteringMerge merge, int newStep) {
        String criterium = "criterium.order";

        String solution = feedbackLevel == 3 ?
            " " + this.messageSource.getMessage(criterium + ".feedback.solution", new Object[]{newStep}, locale) : "";

        addCriterion(criterium, criterium + ".feedback.step", merge.getDistance(), solution);
    }

    private boolean hasFeedback(double distance) {
        return superfluousMerges.containsKey(distance) ||
            redundantMerges.containsKey(distance) ||
            missingMerges.containsKey(distance) ||
            superfluousDataPoints.containsKey(distance) ||
            duplicateDataPoints.containsKey(distance) ||
            missingDataPoints.containsKey(distance);
    }

    private void addDistanceToEvaluation(double distance) {
        String criterium = "criterium.distance";
        BigDecimal pointsForDistance = solutionMergeEvents.get(distance).pointsForDistance().negate();

        switch (this.feedbackLevel) {
            case 1:
                addCriterion(criterium, pointsForDistance, criterium + ".wrong");
                break;
            case 2, 3:
                addCriterion(criterium, pointsForDistance, criterium + ".wrong.distance", distance);
                break;
        }
    }

    private void wrongOrSuperfluousDistanceFeedback(List<Double> wrongOrRedundantDistances) {
        String criterium = "criterium.distance";

        switch (this.feedbackLevel) {
            case 1:
                // only give the general "wrong distance" feedback once if feedback level is 1
                addCriterion(criterium, criterium + ".feedback", wrongOrRedundantDistances.size());
                break;
            case 2, 3:
                for (double distance : wrongOrRedundantDistances) {
                    addCriterion(criterium, criterium + ".feedback.distance", distance);
                }
                break;
        }
    }

    private void missingDistancesFeedback(List<Double> missingDistances) {
        String criterium = "criterium.missingDistance";

        if (!missingDistances.isEmpty()) {
            switch (feedbackLevel) {
                case 1:
                    BigDecimal totalDeduction = BigDecimal.ZERO;
                    for (double distance : new HashSet<>(missingDistances)) {
                        totalDeduction = totalDeduction.subtract(solutionMergeEvents.get(distance).pointsForDistance());
                    }

                    addCriterion("criterium.distance", totalDeduction, criterium + ".feedback", missingDistances.size());
                    break;
                case 2:
                    for (double distance : missingDistances) {
                        BigDecimal deduction = solutionMergeEvents.get(distance).pointsForDistance().negate();
                        HierarchicalClusteringMerge merge = solutionMergeEvents.get(distance).newMerges().getFirst();
                        addCriterion("criterium.distance", deduction, criterium + ".feedback.step", merge.getStep());
                    }
                    break;
                case 3:
                    for (double distance : missingDistances) {
                        BigDecimal deduction = solutionMergeEvents.get(distance).pointsForDistance().negate();
                        HierarchicalClusteringMerge merge = solutionMergeEvents.get(distance).newMerges().getFirst();
                        addCriterion("criterium.distance", deduction, criterium + ".feedback.distance", merge.getDistance(), merge.getResult().getFullLabel(), merge.getStep());
                    }
                    break;
            }
        }
    }

    private void mergeFeedback(String criterium, double distance, List<HierarchicalClusteringMerge> mergeList) {
        switch (feedbackLevel) {
            case 1:
                addCriterion(criterium, criterium + ".feedback", mergeList.size());
                break;
            case 2:
                addCriterion(criterium, criterium + ".feedback.distance", distance, mergeList.size());
                break;
            case 3:
                List<String> resultLabels = new ArrayList<>();
                for (HierarchicalClusteringMerge merge : mergeList) {
                    resultLabels.add(merge.getResult().getFullLabel());
                }

                addCriterion(criterium, criterium + ".feedback.solution", distance, String.join(", ", resultLabels));
                break;
        }
    }

    private void pointInClusterFeedback(String criterium, double distance, Map<HierarchicalClusteringMerge, List<String>> map) {
        switch (feedbackLevel) {
            case 1:
                addCriterion(criterium, criterium + ".feedback", map.size());
                break;
            case 2:
                addCriterion(criterium, criterium + ".feedback.distance", map.size(), distance);
                break;
            case 3:
                for (HierarchicalClusteringMerge merge : map.keySet()) {
                    HierarchicalClusteringCluster cluster = new HierarchicalClusteringCluster();
                    cluster.setDataPoints(map.get(merge));
                    addCriterion(criterium, criterium + ".feedback.solution", merge.getResult().getFullLabel(), distance, cluster.getFullLabel());
                }
                break;
        }
    }

    private void attachSolutionAndDendrogram() {
        if (feedbackLevel == 3) {
            String solution = buildSolutionString(task);
            addCriterion("solution", true, "solution.string", solution);

            try {
                String dendrogramSvg = new DendrogramSvgRenderer().render(task.getDendrogramModel());
                byte[] dendrogramPng = new DendrogramImageExporter().export(DendrogramImageExporter.ImageFormat.PNG, dendrogramSvg);
                String dendrogramBase64 = Base64.getEncoder().encodeToString(dendrogramPng);

                String inlineImage = "<img src='data:image/png;base64," + dendrogramBase64 + "' />";
                addCriterion("solution", true, "solution.dendrogram", inlineImage);
            } catch (Exception e) {
                LoggerFactory.getLogger(EvaluationService.class).warn("Error while trying to export Dendrogram SVG to image.", e);
            }
        }
    }

    public static String buildSolutionString(HierarchicalClusteringTask task) {
        SortedMap<Double, EvaluationService.MergeEventAtDistance> mergeEventHistory = new TreeMap<>(EvaluationService.buildEvaluationMergeHistoryForTask(task));
        StringBuilder solutionBuilder = new StringBuilder();

        for (double distance : mergeEventHistory.keySet().stream().sorted().toList()) {
            solutionBuilder.append("Distance ").append(distance).append(": ");

            List<HierarchicalClusteringMerge> newMerges = mergeEventHistory.get(distance).newMerges();
            for (HierarchicalClusteringMerge newMerge : newMerges) {
                solutionBuilder.append(newMerge.getResult().getFullLabel()).append(newMerges.indexOf(newMerge) != newMerges.size() - 1 ? ", " : "");
            }

            List<HierarchicalClusteringMerge> inheritedMerges = mergeEventHistory.get(distance).inheritedMerges();
            for (HierarchicalClusteringMerge inheritedMerge : inheritedMerges) {
                solutionBuilder.append(", ").append(inheritedMerge.getResult().getFullLabel());
            }

            solutionBuilder.append("\n");
        }

        return solutionBuilder.toString();
    }

    private void addCriterion(String criterionName, String feedback, Object... args) {
        addCriterion(criterionName, null, false, feedback, args);
    }

    private void addCriterion(String criterionName, boolean passed, String feedback, Object... args) {
        addCriterion(criterionName, null, passed, feedback, args);
    }

    private void addCriterion(String criterionName, BigDecimal points, String feedback, Object... args) {
        addCriterion(criterionName, points, false, feedback, args);
    }

    private void addCriterion(String criterionName, BigDecimal points, boolean passed, String feedback, Object... args) {
        criteria.add(new CriterionDto(
            this.messageSource.getMessage(criterionName, null, locale),
            points,
            passed,
            this.messageSource.getMessage(feedback, args, locale)
        ));
    }
}

package at.jku.dke.task_app.hierarchical_clustering.evaluation;

import at.jku.dke.etutor.task_app.dto.CriterionDto;
import at.jku.dke.task_app.hierarchical_clustering.data.entities.HierarchicalClusteringCluster;
import at.jku.dke.task_app.hierarchical_clustering.data.entities.HierarchicalClusteringMerge;
import at.jku.dke.task_app.hierarchical_clustering.data.entities.HierarchicalClusteringTask;
import at.jku.dke.task_app.hierarchical_clustering.dendrogram.DendrogramImageExporter;
import at.jku.dke.task_app.hierarchical_clustering.dendrogram.DendrogramSvgRenderer;
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

    SortedMap<BigDecimal, EvaluationService.MergeEventAtDistance> solutionMergeEvents;

    List<BigDecimal> wrongOrSuperfluousDistances;
    List<BigDecimal> missingDistances;

    SortedMap<BigDecimal, List<HierarchicalClusteringMerge>> superfluousMerges;
    SortedMap<BigDecimal, List<HierarchicalClusteringMerge>> redundantMerges;
    SortedMap<BigDecimal, List<HierarchicalClusteringMerge>> missingMerges;

    SortedMap<BigDecimal, Map<HierarchicalClusteringMerge, List<String>>> missingDataPoints;
    SortedMap<BigDecimal, Map<HierarchicalClusteringMerge, List<String>>> superfluousDataPoints;
    SortedMap<BigDecimal, Map<HierarchicalClusteringMerge, List<String>>> duplicateDataPoints;

    public EvaluationFeedbackBuilder(HierarchicalClusteringTask task, SortedMap<BigDecimal, EvaluationService.MergeEventAtDistance> solutionMergeEvents,
                                     MessageSource messageSource, Locale locale, int feedbackLevel, List<CriterionDto> criteria) {
        this.task = task;
        this.solutionMergeEvents = solutionMergeEvents;
        this.messageSource = messageSource;
        this.locale = locale;
        this.feedbackLevel = feedbackLevel;
        this.criteria = criteria;
    }

    public EvaluationFeedbackBuilder withWrongOrderGeneral() {
        isWrongOrder = true;
        return this;
    }

    public EvaluationFeedbackBuilder withWrongOrSuperfluousDistances(List<BigDecimal> wrongOrSuperfluousDistances) {
        this.wrongOrSuperfluousDistances = wrongOrSuperfluousDistances;
        return this;
    }

    public EvaluationFeedbackBuilder withMissingDistances(List<BigDecimal> missingDistances) {
        this.missingDistances = missingDistances;
        return this;
    }

    public EvaluationFeedbackBuilder withSuperfluousMerges(SortedMap<BigDecimal, List<HierarchicalClusteringMerge>> superfluousMerges) {
        this.superfluousMerges = superfluousMerges;
        return this;
    }

    public EvaluationFeedbackBuilder withRedundantMerges(SortedMap<BigDecimal, List<HierarchicalClusteringMerge>> redundantMerges) {
        this.redundantMerges = redundantMerges;
        return this;
    }

    public EvaluationFeedbackBuilder withMissingMerges(SortedMap<BigDecimal, List<HierarchicalClusteringMerge>> missingMerges) {
        this.missingMerges = missingMerges;
        return this;
    }

    public EvaluationFeedbackBuilder withMissingDataPoints(SortedMap<BigDecimal, Map<HierarchicalClusteringMerge, List<String>>> missingDataPoints) {
        this.missingDataPoints = missingDataPoints;
        return this;
    }

    public EvaluationFeedbackBuilder withSuperfluousDataPoints(SortedMap<BigDecimal, Map<HierarchicalClusteringMerge, List<String>>> superfluousDataPoints) {
        this.superfluousDataPoints = superfluousDataPoints;
        return this;
    }

    public EvaluationFeedbackBuilder withDuplicateDataPoints(SortedMap<BigDecimal, Map<HierarchicalClusteringMerge, List<String>>> duplicateDataPoints) {
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

        SortedSet<BigDecimal> distances = task.getSolutionMergeHistory().stream()
            .map(HierarchicalClusteringMerge::getDistance)
            .collect(Collectors.toCollection(TreeSet::new));

        for (BigDecimal distance : distances) {
            if (!hasFeedback(distance)) {
                continue;
            }

            addDistanceHeaderToFeedback(distance);

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

        for (BigDecimal distance : duplicateDataPoints.keySet()) {
            pointInClusterFeedback("criterium.duplicatePointInCluster", distance, duplicateDataPoints.get(distance));
        }

        for (BigDecimal distance : missingDataPoints.keySet()) {
            pointInClusterFeedback("criterium.missingPointInCluster", distance, missingDataPoints.get(distance));
        }

        for (BigDecimal distance : superfluousDataPoints.keySet()) {
            pointInClusterFeedback("criterium.superfluousPointInCluster", distance, superfluousDataPoints.get(distance));
        }

        for (BigDecimal distance : missingMerges.keySet()) {
            mergeFeedback("criterium.missingCluster", distance, missingMerges.get(distance));
        }

        for (BigDecimal distance : redundantMerges.keySet()) {
            mergeFeedback("criterium.redundantCluster", distance, redundantMerges.get(distance));
        }

        for (BigDecimal distance : superfluousMerges.keySet()) {
            mergeFeedback("criterium.superfluousCluster", distance, superfluousMerges.get(distance));
        }

        attachSolutionAndDendrogram();
    }

    private boolean hasFeedback(BigDecimal distance) {
        return superfluousMerges.containsKey(distance) ||
            redundantMerges.containsKey(distance) ||
            missingMerges.containsKey(distance) ||
            superfluousDataPoints.containsKey(distance) ||
            duplicateDataPoints.containsKey(distance) ||
            missingDataPoints.containsKey(distance);
    }

    private void addDistanceHeaderToFeedback(BigDecimal distance) {
        String criterium = "criterium.distance";
        BigDecimal pointsForDistance = solutionMergeEvents.get(distance).pointsForDistance().negate();
        BigDecimal formatted = distance.stripTrailingZeros().scale() == 0 ? distance.stripTrailingZeros().setScale(1) : distance.stripTrailingZeros();

        switch (this.feedbackLevel) {
            case 1:
                addCriterion(criterium, criterium + ".wrong");
                break;
            case 2:
                addCriterion(criterium, criterium + ".wrong.distance", formatted.toPlainString());
                break;
            case 3:
                addCriterion(criterium, pointsForDistance, criterium + ".wrong.distance", formatted.toPlainString());
                break;
        }
    }

    private void wrongOrderFeedbackGeneral() {
        BigDecimal penalty = feedbackLevel == 3 ? task.getWrongOrderPenalty().negate() : null;
        addCriterion("criterium.order", penalty, "criterium.order.feedback");
    }

    private void wrongOrSuperfluousDistanceFeedback(List<BigDecimal> wrongOrRedundantDistances) {
        String criterium = "criterium.distance";

        switch (this.feedbackLevel) {
            case 1:
                // only give the general "wrong distance" feedback once if feedback level is 1
                addCriterion(criterium, criterium + ".feedback", wrongOrRedundantDistances.size());
                break;
            case 2, 3:
                for (BigDecimal distance : wrongOrRedundantDistances) {
                    BigDecimal formatted = distance.stripTrailingZeros().scale() == 0 ? distance.stripTrailingZeros().setScale(1) : distance.stripTrailingZeros();
                    addCriterion(criterium, criterium + ".feedback.distance", formatted.toPlainString());
                }
                break;
        }
    }

    private void missingDistancesFeedback(List<BigDecimal> missingDistances) {
        String criterium = "criterium.missingDistance";

        if (!missingDistances.isEmpty()) {
            switch (feedbackLevel) {
                case 1:
                    addCriterion("criterium.distance", criterium + ".feedback", missingDistances.size());
                    break;
                case 2:
                    for (BigDecimal distance : missingDistances) {
                        HierarchicalClusteringMerge merge = solutionMergeEvents.get(distance).newMerges().getFirst();
                        addCriterion("criterium.distance", criterium + ".feedback.step", merge.getStep());
                    }
                    break;
                case 3:
                    for (BigDecimal distance : missingDistances) {
                        BigDecimal deduction = solutionMergeEvents.get(distance).pointsForDistance().negate();
                        BigDecimal formatted = distance.stripTrailingZeros().scale() == 0 ? distance.stripTrailingZeros().setScale(1) : distance.stripTrailingZeros();

                        List<String> resultLabels = new ArrayList<>();
                        List<HierarchicalClusteringMerge> merges = solutionMergeEvents.get(distance).newMerges();
                        merges.addAll(solutionMergeEvents.get(distance).inheritedMerges());
                        merges.stream().map(m -> m.getResult().getFullLabel()).forEach(resultLabels::add);

                        addCriterion("criterium.distance", deduction, criterium + ".feedback.distance", formatted.toPlainString(), String.join(", ", resultLabels));
                    }
                    break;
            }
        }
    }

    private void mergeFeedback(String criterium, BigDecimal distance, List<HierarchicalClusteringMerge> mergeList) {
        BigDecimal formatted = distance.stripTrailingZeros().scale() == 0 ? distance.stripTrailingZeros().setScale(1) : distance.stripTrailingZeros();

        switch (feedbackLevel) {
            case 1:
                addCriterion(criterium, criterium + ".feedback", mergeList.size());
                break;
            case 2:
                addCriterion(criterium, criterium + ".feedback.distance", formatted.toPlainString(), mergeList.size());
                break;
            case 3:
                List<String> resultLabels = new ArrayList<>();
                mergeList.stream().map(m -> m.getResult().getFullLabel()).forEach(resultLabels::add);

                addCriterion(criterium, criterium + ".feedback.solution", formatted.toPlainString(), String.join(", ", resultLabels));
                break;
        }
    }

    private void pointInClusterFeedback(String criterium, BigDecimal distance, Map<HierarchicalClusteringMerge, List<String>> map) {
        BigDecimal formatted = distance.stripTrailingZeros().scale() == 0 ? distance.stripTrailingZeros().setScale(1) : distance.stripTrailingZeros();

        switch (feedbackLevel) {
            case 1:
                addCriterion(criterium, criterium + ".feedback", map.size());
                break;
            case 2:
                addCriterion(criterium, criterium + ".feedback.distance", formatted.toPlainString(), map.size());
                break;
            case 3:
                for (HierarchicalClusteringMerge merge : map.keySet()) {
                    HierarchicalClusteringCluster cluster = new HierarchicalClusteringCluster();
                    cluster.setDataPoints(map.get(merge));
                    addCriterion(criterium, criterium + ".feedback.solution", merge.getResult().getFullLabel(), formatted.toPlainString(), cluster.getFullLabel());
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
        SortedMap<BigDecimal, EvaluationService.MergeEventAtDistance> mergeEventHistory = new TreeMap<>(EvaluationService.buildEvaluationMergeHistoryForTask(task));
        StringBuilder solutionBuilder = new StringBuilder();

        for (BigDecimal distance : mergeEventHistory.keySet().stream().sorted().toList()) {
            BigDecimal stripped = distance.stripTrailingZeros();
            solutionBuilder.append("Distance ").append(stripped.scale() == 0 ? stripped.setScale(1).toPlainString() : stripped.toPlainString()).append(": ");

            List<HierarchicalClusteringMerge> newMerges = mergeEventHistory.get(distance).newMerges();
            for (int i = 0; i < newMerges.size(); i++) {
                HierarchicalClusteringMerge newMerge = newMerges.get(i);
                solutionBuilder.append(newMerge.getResult().getFullLabel()).append(i < newMerges.size() - 1 ? ", " : "");
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

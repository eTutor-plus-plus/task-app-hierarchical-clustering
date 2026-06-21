package at.jku.dke.task_app.hierarchical_clustering.evaluation;

import at.jku.dke.etutor.task_app.dto.CriterionDto;
import at.jku.dke.task_app.hierarchical_clustering.data.entities.HierarchicalClusteringCluster;
import at.jku.dke.task_app.hierarchical_clustering.data.entities.HierarchicalClusteringMerge;
import at.jku.dke.task_app.hierarchical_clustering.data.entities.HierarchicalClusteringTask;
import at.jku.dke.task_app.hierarchical_clustering.dendrogram.ImageExporter;
import at.jku.dke.task_app.hierarchical_clustering.dendrogram.DendrogramSvgRenderer;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Builder to construct feedback for students from the evaluation.
 */
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

    /**
     * Creates a new instance of class {@linkplain EvaluationFeedbackBuilder}.
     *
     * @param task          The task.
     * @param messageSource The message source for building messages.
     * @param locale        The locale/language for messages.
     * @param feedbackLevel The feedback level for this submission.
     * @param criteria      The list of feedback criteria.
     */
    public EvaluationFeedbackBuilder(HierarchicalClusteringTask task, SortedMap<BigDecimal, EvaluationService.MergeEventAtDistance> solutionMergeEvents,
                                     MessageSource messageSource, Locale locale, int feedbackLevel, List<CriterionDto> criteria) {
        this.task = task;
        this.solutionMergeEvents = solutionMergeEvents;
        this.messageSource = messageSource;
        this.locale = locale;
        this.feedbackLevel = feedbackLevel;
        this.criteria = criteria;
    }

    /**
     * Sets wrong order to true to include the associated message in the feedback.
     *
     * @return This builder for chaining.
     */
    public EvaluationFeedbackBuilder withWrongOrderGeneral() {
        isWrongOrder = true;
        return this;
    }

    /**
     * Sets the register of wrong or superfluous distances to include associated messages in the feedback.
     *
     * @param wrongOrSuperfluousDistances The register of wrong or superfluous distances.
     * @return This builder for chaining.
     */
    public EvaluationFeedbackBuilder withWrongOrSuperfluousDistances(List<BigDecimal> wrongOrSuperfluousDistances) {
        this.wrongOrSuperfluousDistances = wrongOrSuperfluousDistances;
        return this;
    }

    /**
     * Sets the register of missing distances to include associated messages in the feedback.
     *
     * @param missingDistances    The register of missing distances.
     * @return This builder for chaining.
     */
    public EvaluationFeedbackBuilder withMissingDistances(List<BigDecimal> missingDistances) {
        this.missingDistances = missingDistances;
        return this;
    }

    /**
     * Sets the register of superfluous merges/clusters to include associated messages in the feedback.
     *
     * @param superfluousMerges The register of superfluous merges/clusters.
     * @return This builder for chaining.
     */
    public EvaluationFeedbackBuilder withSuperfluousClusters(SortedMap<BigDecimal, List<HierarchicalClusteringMerge>> superfluousMerges) {
        this.superfluousMerges = superfluousMerges;
        return this;
    }

    /**
     * Sets the register for redundant merges/clusters to include associated messages in the feedback.
     *
     * @param redundantMerges The register of redundant merges/clusters.
     * @return This builder for chaining.
     */
    public EvaluationFeedbackBuilder withRedundantClusters(SortedMap<BigDecimal, List<HierarchicalClusteringMerge>> redundantMerges) {
        this.redundantMerges = redundantMerges;
        return this;
    }

    /**
     * Sets the register for missing merges/clusters to include associated messages in the feedback.
     *
     * @param missingMerges The register of missing merges/clusters.
     * @return This builder for chaining.
     */
    public EvaluationFeedbackBuilder withMissingClusters(SortedMap<BigDecimal, List<HierarchicalClusteringMerge>> missingMerges) {
        this.missingMerges = missingMerges;
        return this;
    }

    /**
     * Sets the register for missing data points in clusters to include associated messages in the feedback.
     *
     * @param missingDataPoints The register of missing data points in clusters.
     * @return This builder for chaining.
     */
    public EvaluationFeedbackBuilder withMissingDataPoints(SortedMap<BigDecimal, Map<HierarchicalClusteringMerge, List<String>>> missingDataPoints) {
        this.missingDataPoints = missingDataPoints;
        return this;
    }

    /**
     * Sets the register for superfluous data points in clusters to include associated messages in the feedback.
     *
     * @param superfluousDataPoints The register of superfluous data points in clusters.
     * @return This builder for chaining.
     */
    public EvaluationFeedbackBuilder withSuperfluousDataPoints(SortedMap<BigDecimal, Map<HierarchicalClusteringMerge, List<String>>> superfluousDataPoints) {
        this.superfluousDataPoints = superfluousDataPoints;
        return this;
    }

    /**
     * Sets the register for duplicate/redundant data points in clusters to include associated messages in the feedback.
     *
     * @param duplicateDataPoints The register of duplicate/redundant data points in clusters.
     * @return This builder for chaining.
     */
    public EvaluationFeedbackBuilder withDuplicateDataPoints(SortedMap<BigDecimal, Map<HierarchicalClusteringMerge, List<String>>> duplicateDataPoints) {
        this.duplicateDataPoints = duplicateDataPoints;
        return this;
    }

    /**
     * Builds feedback messages and adds them to the list of feedback criteria grouped
     * by distance, with the option to include a header for feedback levels 1 and 2 (is
     * always included in level 3 to display point deductions correctly).
     */
    public void feedbackGroupedByDistance() {
        if (feedbackLevel <= 0) {
            return;
        }

        if (isWrongOrder) {
            wrongOrderFeedback();
        }

        if (!wrongOrSuperfluousDistances.isEmpty()) {
            wrongOrSuperfluousDistanceFeedback(wrongOrSuperfluousDistances);
        }

        if (!missingDistances.isEmpty()) {
            missingDistanceFeedback(missingDistances);
        }

        SortedSet<BigDecimal> distances = task.getSolutionMergeHistory().stream()
            .map(HierarchicalClusteringMerge::getDistance)
            .collect(Collectors.toCollection(TreeSet::new));

        for (BigDecimal distance : distances) {
            if (!hasFeedback(distance)) {
                continue;
            }

            addDistanceHeader(distance);

            if (superfluousMerges.containsKey(distance)) {
                clusterFeedback("criterium.superfluousCluster", distance, superfluousMerges.get(distance));
            }

            if (redundantMerges.containsKey(distance)) {
                clusterFeedback("criterium.redundantCluster", distance, redundantMerges.get(distance));
            }

            if (missingMerges.containsKey(distance)) {
                clusterFeedback("criterium.missingCluster", distance, missingMerges.get(distance));
            }

            if (superfluousDataPoints.containsKey(distance)) {
                dataPointFeedback("criterium.superfluousPointInCluster", distance, superfluousDataPoints.get(distance));
            }

            if (duplicateDataPoints.containsKey(distance)) {
                dataPointFeedback("criterium.duplicatePointInCluster", distance, duplicateDataPoints.get(distance));
            }

            if (missingDataPoints.containsKey(distance)) {
                dataPointFeedback("criterium.missingPointInCluster", distance, missingDataPoints.get(distance));
            }
        }

        attachSolutionAndDendrogram();
    }

    /**
     * Helper method to determine whether any mistakes have been found
     * in the submission at the given distance by checking all registers
     * on whether they contain an entry for said distance.
     *
     * @param distance The distance to be checked for.
     * @return {@code true}, if any register has at least one entry for the
     *         given distance.
     */
    private boolean hasFeedback(BigDecimal distance) {
        return superfluousMerges.containsKey(distance) ||
            redundantMerges.containsKey(distance) ||
            missingMerges.containsKey(distance) ||
            superfluousDataPoints.containsKey(distance) ||
            duplicateDataPoints.containsKey(distance) ||
            missingDataPoints.containsKey(distance);
    }

    /**
     * Helper method for adding a header for a distance to group feedback messages.
     * <p>
     * Should always be added for feedback level 3 to allow for displaying point
     * deductions in the feedback (as multiple errors at a distance still lead to
     * the same deduction it needs to be grouped).
     *
     * @param distance The distance for which the header should be added to the feedback.
     */
    private void addDistanceHeader(BigDecimal distance) {
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

    /**
     * Helper method for adding the feedback message for wrong ordering of a submission.
     */
    private void wrongOrderFeedback() {
        BigDecimal penalty = feedbackLevel == 3 ? task.getWrongOrderPenalty().negate() : null;
        addCriterion("criterium.order", penalty, "criterium.order.feedback");
    }

    /**
     * Helper method for adding feedback messages for wrong or superfluous distances
     * from the register depending on the feedback level.
     *
     * @param wrongOrRedundantDistances The register of wrong or superfluous distances.
     */
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

    /**
     * Helper method for adding feedback messages for missing distances
     * from the register depending on the feedback level.
     *
     * @param missingDistances The register of missing distances.
     */
    private void missingDistanceFeedback(List<BigDecimal> missingDistances) {
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

    /**
     * Helper method for adding different kinds of feedback messages concerning whole merges/clusters
     * from a register depending on the feedback level.
     * <p>
     * Used as a helper for superfluous, redundant and missing cluster feedback.
     *
     * @param criterium The type of feedback.
     * @param distance  The distance where the errors occur.
     * @param mergeList The register of merges/clusters that were wrong.
     */
    private void clusterFeedback(String criterium, BigDecimal distance, List<HierarchicalClusteringMerge> mergeList) {
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

    /**
     * Helper method for adding different kinds of feedback messages concerning data points
     * in merges/clusters from a register depending on the feedback level.
     * <p>
     * Used as a helper for superfluous, duplicate/redundant and missing data points feedback.
     *
     * @param criterium The type of feedback.
     * @param distance  The distance where the errors occur.
     * @param map       The registers of data points that were wrong.
     */
    private void dataPointFeedback(String criterium, BigDecimal distance, Map<HierarchicalClusteringMerge, List<String>> map) {
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

    /**
     * Helper method to attach the sample solution in string form and the dendrogram
     * as an image in PNG format to the feedback for feedback level 3.
     */
    private void attachSolutionAndDendrogram() {
        if (feedbackLevel == 3) {
            String solution = buildSolutionString(task);
            addCriterion("solution", true, "solution.string", solution);

            try {
                String dendrogramSvg = new DendrogramSvgRenderer().render(task.getDendrogramModel());
                byte[] dendrogramPng = new ImageExporter().export(ImageExporter.ImageFormat.PNG, dendrogramSvg);
                String dendrogramBase64 = Base64.getEncoder().encodeToString(dendrogramPng);

                String inlineImage = "<img src='data:image/png;base64," + dendrogramBase64 + "' />";
                addCriterion("solution", true, "solution.dendrogram", inlineImage);
            } catch (Exception e) {
                LoggerFactory.getLogger(EvaluationService.class).warn("Error while trying to export Dendrogram SVG to image.", e);
            }
        }
    }

    /**
     * Builds a formatted string from a tasks solution for displaying in feedback or the UI.
     *
     * @param task The task for which to build the formatted solution.
     * @return The formatted solution of a task.
     */
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

    /**
     * Helper method for adding a feedback message with parameters to the list of criteria.
     *
     * @param criterionName The key/code of the criterion name message.
     * @param feedback      The key/code of the criterion feedback message.
     * @param args          Optional message parameters.
     */
    private void addCriterion(String criterionName, String feedback, Object... args) {
        addCriterion(criterionName, null, false, feedback, args);
    }

    /**
     * Helper method for adding a feedback message with parameters to the list of criteria.
     * Also allows for indicating whether the criterion has been passed or not.
     *
     * @param criterionName The key/code of the criterion name message.
     * @param passed        Indicator whether criterion has been passed.
     * @param feedback      The key/code of the criterion feedback message.
     * @param args          Optional message parameters.
     */
    private void addCriterion(String criterionName, boolean passed, String feedback, Object... args) {
        addCriterion(criterionName, null, passed, feedback, args);
    }

    /**
     * Helper method for adding a feedback message with parameters and awarded/deducted points
     * to the list of criteria.
     *
     * @param criterionName The key/code of the criterion name message.
     * @param points        The awarded or deducted points for this criterion.
     * @param feedback      The key/code of the criterion feedback message.
     * @param args          Optional message parameters.
     */
    private void addCriterion(String criterionName, BigDecimal points, String feedback, Object... args) {
        addCriterion(criterionName, points, false, feedback, args);
    }

    /**
     * Helper method for adding a feedback message with parameters and awarded/deducted points
     * to the list of criteria. Also allows for indicating whether the criterion has been passed or not.
     *
     * @param criterionName The key/code of the criterion name message.
     * @param points        The awarded or deducted points for this criterion.
     * @param passed        Indicator whether criterion has been passed.
     * @param feedback      The key/code of the criterion feedback message.
     * @param args          Optional message parameters.
     */
    private void addCriterion(String criterionName, BigDecimal points, boolean passed, String feedback, Object... args) {
        criteria.add(new CriterionDto(
            this.messageSource.getMessage(criterionName, null, locale),
            points,
            passed,
            this.messageSource.getMessage(feedback, args, locale)
        ));
    }
}

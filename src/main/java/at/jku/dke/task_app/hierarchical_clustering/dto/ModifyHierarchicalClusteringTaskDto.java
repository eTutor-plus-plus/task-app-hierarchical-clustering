package at.jku.dke.task_app.hierarchical_clustering.dto;

import at.jku.dke.task_app.hierarchical_clustering.data.entities.HierarchicalClusteringTask;
import at.jku.dke.task_app.hierarchical_clustering.validation.DistinctDistances;
import at.jku.dke.task_app.hierarchical_clustering.validation.ValidCoordinates;
import at.jku.dke.task_app.hierarchical_clustering.validation.ValidDistances;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * This class represents a data transfer object for modifying a hierarchical clustering task.
 *
 * @param nDataPoints The number of data points to be generated for the task.
 */
public record ModifyHierarchicalClusteringTaskDto(
    @NotNull AssignmentTypeDto assignmentType,
    DistanceMetricDto distanceMetric,
    @NotNull @PositiveOrZero Integer nDataPoints,
    @NotNull LinkageMethodDto linkageMethod,
    @NotNull @PositiveOrZero BigDecimal pointsPerCorrectCluster,
    @PositiveOrZero BigDecimal wrongOrderPenalty,
    @ValidCoordinates List<HierarchicalClusteringTask.CoordinatePoint> coordinatePoints,
    @ValidDistances @DistinctDistances HierarchicalClusteringTask.DistanceMatrix distanceMatrix) implements Serializable {
}

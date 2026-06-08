package at.jku.dke.task_app.hierarchical_clustering.dto;

import at.jku.dke.task_app.hierarchical_clustering.clustering.LinkageMethod;
import at.jku.dke.task_app.hierarchical_clustering.data.entities.HierarchicalClusteringTask;
import at.jku.dke.task_app.hierarchical_clustering.generators.matrix.DistanceMetric;
import at.jku.dke.task_app.hierarchical_clustering.validation.ValidMatrix;
import at.jku.dke.task_app.hierarchical_clustering.validation.ValidCoordinateSystem;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * This class represents a data transfer object for modifying a hierarchical clustering task.
 *
 * @param nDataPoints The number of data points to be generated for the task.
 */
public record ModifyHierarchicalClusteringTaskDto(
    @NotNull AssignmentTypeDto assignmentType,
    DistanceMetric distanceMetric,
    @NotNull @Positive Integer nDataPoints,
    @NotNull LinkageMethod linkageMethod,
    @NotNull @Positive BigDecimal pointsPerCorrectCluster,
    @PositiveOrZero BigDecimal wrongOrderPenalty,
    @ValidCoordinateSystem HierarchicalClusteringTask.CoordinateSystem coordinateSystem,
    @ValidMatrix HierarchicalClusteringTask.DistanceMatrix distanceMatrix) implements Serializable {
}

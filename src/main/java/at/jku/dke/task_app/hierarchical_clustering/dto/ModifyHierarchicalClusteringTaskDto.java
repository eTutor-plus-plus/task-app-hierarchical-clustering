package at.jku.dke.task_app.hierarchical_clustering.dto;

import at.jku.dke.task_app.hierarchical_clustering.clustering.LinkageMethod;
import at.jku.dke.task_app.hierarchical_clustering.data.entities.HierarchicalClusteringTask;
import at.jku.dke.task_app.hierarchical_clustering.generators.matrix.DistanceMetric;
import at.jku.dke.task_app.hierarchical_clustering.validation.ValidMatrix;
import at.jku.dke.task_app.hierarchical_clustering.validation.ValidCoordinateSystem;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Data transfer object for creating and updating specific task data.
 *
 * @param assignmentType          The type of assignment.
 * @param distanceMetric          The metric to calculate distances (only for coordinate list task type).
 * @param nDataPoints             The number of data points.
 * @param linkageMethod           The linkage method.
 * @param pointsPerCorrectCluster The points to be awarded per correctly formed new cluster at a distance.
 * @param wrongOrderPenalty       The penalty for wrong ordering of distances in a submission.
 * @param coordinateSystem        The coordinate system (only for coordinate list task type).
 * @param distanceMatrix          The distance matrix.
 */
@Schema(description = "Task type specific data for creating and updating tasks")
public record ModifyHierarchicalClusteringTaskDto(
    @Schema(description = "The type of assignment") @NotNull AssignmentTypeDto assignmentType,
    @Schema(description = "The metric to calculate distances with. Only required if assignment type is coordinate list") DistanceMetric distanceMetric,
    @Schema(description = "The number of data points of a task", example = "5") @NotNull @Positive Integer nDataPoints,
    @Schema(description = "The linkage method for computing a clustering") @NotNull LinkageMethod linkageMethod,
    @Schema(description = "The points to be awarded per correctly formed new cluster at a distance", example = "1.5") @NotNull @Positive BigDecimal pointsPerCorrectCluster,
    @Schema(description = "The penalty for wrong ordering of distances in a submission", example = "2") @PositiveOrZero BigDecimal wrongOrderPenalty,
    @Schema(description = "The coordinate system. Only required if assignment type is coordinate list") @ValidCoordinateSystem HierarchicalClusteringTask.CoordinateSystem coordinateSystem,
    @Schema(description = "The distance matrix") @ValidMatrix HierarchicalClusteringTask.DistanceMatrix distanceMatrix) implements Serializable {
}

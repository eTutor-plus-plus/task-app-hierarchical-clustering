package at.jku.dke.task_app.hierarchical_clustering.dto;

import at.jku.dke.task_app.hierarchical_clustering.data.entities.HierarchicalClusteringTask;
import at.jku.dke.task_app.hierarchical_clustering.validation.ValidCoordinateSystem;
import at.jku.dke.task_app.hierarchical_clustering.validation.ValidMatrix;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * DTO for {@link HierarchicalClusteringTask}
 *
 * @param nDataPoints The number of data points in the distance matrix.
 */
public record HierarchicalClusteringTaskDto(
    @NotNull AssignmentTypeDto assignmentType,
    DistanceMetric distanceMetric,
    @NotNull @PositiveOrZero Integer nDataPoints,
    @NotNull LinkageMethodDto linkageMethod,
    @NotNull @PositiveOrZero BigDecimal pointsPerCorrectCluster,
    @PositiveOrZero BigDecimal wrongOrderPenalty,
    @ValidCoordinateSystem HierarchicalClusteringTask.CoordinateSystem coordinateSystem,
    @ValidMatrix HierarchicalClusteringTask.DistanceMatrix distanceMatrix,
    String solution,
    byte[] dendrogram) implements Serializable {
}

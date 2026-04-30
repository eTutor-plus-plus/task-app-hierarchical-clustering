package at.jku.dke.task_app.hierarchical_clustering.dto;

import at.jku.dke.task_app.hierarchical_clustering.data.entities.HierarchicalClusteringTask;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

/**
 * DTO for {@link HierarchicalClusteringTask}
 *
 * @param nDataPoints The number of data points in the distance matrix.
 */
public record HierarchicalClusteringTaskDto(
    @NotNull GenerationStrategyDto generationStrategy,
    @NotNull DistanceMetricDto distanceMetric,
    @NotNull Integer nDataPoints,
    @NotNull LinkageMethodDto linkageMethod,
    HierarchicalClusteringTask.DistanceMatrix distanceMatrix) implements Serializable {
}

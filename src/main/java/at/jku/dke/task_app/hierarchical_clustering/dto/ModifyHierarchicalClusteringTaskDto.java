package at.jku.dke.task_app.hierarchical_clustering.dto;

import at.jku.dke.task_app.hierarchical_clustering.data.entities.HierarchicalClusteringTask;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

/**
 * This class represents a data transfer object for modifying a hierarchical clustering task.
 *
 * @param nDataPoints The number of data points to be generated for the task.
 */
public record ModifyHierarchicalClusteringTaskDto(
    @NotNull GenerationStrategyDto generationStrategy,
    DistanceMetricDto distanceMetric,
    @NotNull Integer nDataPoints,
    @NotNull LinkageMethodDto linkageMethod,
    HierarchicalClusteringTask.DistanceMatrix distanceMatrix) implements Serializable {
}

package at.jku.dke.task_app.hierarchical_clustering.dto;

import at.jku.dke.task_app.hierarchical_clustering.data.entities.HierarchicalClusteringTask;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * DTO for {@link HierarchicalClusteringTask}
 *
 * @param nDataPoints The number of data points in the distance matrix.
 */
public record HierarchicalClusteringTaskDto(
    @NotNull Integer nDataPoints,
    @NotNull LinkageMethodDto linkageMethod,
    HierarchicalClusteringTask.DistanceMatrix distanceMatrix) implements Serializable {
}

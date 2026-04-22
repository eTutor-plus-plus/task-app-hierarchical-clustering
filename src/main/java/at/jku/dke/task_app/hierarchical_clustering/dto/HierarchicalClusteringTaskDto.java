package at.jku.dke.task_app.hierarchical_clustering.dto;

import at.jku.dke.task_app.hierarchical_clustering.data.entities.HierarchicalClusteringTask;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

/**
 * DTO for {@link HierarchicalClusteringTask}
 *
 * @param solution The solution.
 */
public record HierarchicalClusteringTaskDto(@NotNull Integer solution) implements Serializable {
}

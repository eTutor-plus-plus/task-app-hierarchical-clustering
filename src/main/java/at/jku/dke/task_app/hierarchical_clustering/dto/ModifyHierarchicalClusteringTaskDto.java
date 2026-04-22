package at.jku.dke.task_app.hierarchical_clustering.dto;

import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

/**
 * This class represents a data transfer object for modifying a hierarchical clustering task.
 *
 * @param solution The solution.
 */
public record ModifyHierarchicalClusteringTaskDto(@NotNull Integer solution) implements Serializable {
}

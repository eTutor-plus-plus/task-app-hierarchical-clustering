package at.jku.dke.task_app.hierarchical_clustering.dto;

import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

/**
 * DTO for {@link at.jku.dke.task_app.hierarchical_clustering.data.entities.BinarySearchTask}
 *
 * @param solution The solution.
 */
public record BinarySearchTaskDto(@NotNull Integer solution) implements Serializable {
}

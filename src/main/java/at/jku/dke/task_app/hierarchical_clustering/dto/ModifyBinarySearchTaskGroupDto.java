package at.jku.dke.task_app.hierarchical_clustering.dto;

import at.jku.dke.task_app.hierarchical_clustering.validation.ValidTaskGroupNumber;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

/**
 * This class represents a data transfer object for modifying a hierarchical clustering task group.
 *
 * @param minNumber The minimum number.
 * @param maxNumber The maximum number.
 */
@ValidTaskGroupNumber
public record ModifyBinarySearchTaskGroupDto(@NotNull Integer minNumber, @NotNull Integer maxNumber) implements Serializable {
}

package at.jku.dke.task_app.hierarchical_clustering.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * This class represents a data transfer object for submitting a solution.
 *
 * @param input The user input.
 */
@Schema(description = "Data of a submission")
public record HierarchicalClusteringSubmissionDto(
    @Schema(description = "The input of a submission", example = "Distance 1.0: (3,4), (5,6)") @NotNull String input) {
}

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
    @NotNull AssignmentTypeDto assignmentType,
    DistanceMetricDto distanceMetric,
    @NotNull Integer nDataPoints,
    @NotNull LinkageMethodDto linkageMethod,
    @NotNull BigDecimal pointsPerCorrectCluster,
    BigDecimal wrongOrderPenalty,
    Integer lengthX,
    Integer lengthY,
    List<HierarchicalClusteringTask.CoordinatePoint> coordinatePoints,
    HierarchicalClusteringTask.DistanceMatrix distanceMatrix,
    String solution,
    String dendrogramSvg) implements Serializable {
}

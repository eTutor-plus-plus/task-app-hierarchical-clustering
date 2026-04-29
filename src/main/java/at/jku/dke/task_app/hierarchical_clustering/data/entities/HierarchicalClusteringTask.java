package at.jku.dke.task_app.hierarchical_clustering.data.entities;

import at.jku.dke.etutor.task_app.data.entities.BaseTask;
import at.jku.dke.etutor.task_app.dto.TaskStatus;
import at.jku.dke.task_app.hierarchical_clustering.data.converters.LinkageMethodConverter;
import at.jku.dke.task_app.hierarchical_clustering.dto.LinkageMethodDto;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.List;

/**
 * Represents a hierarchical clustering task.
 */
@Entity
@Table(name = "task")
public class HierarchicalClusteringTask extends BaseTask {

    @Convert(converter = LinkageMethodConverter.class)
    @Column(name = "linkage", columnDefinition = "linkage_method not null default 'single'", nullable = false)
    private LinkageMethodDto linkageMethod;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "distance_matrix", nullable = false)
    private DistanceMatrix distanceMatrix;

    @Column(name = "points_per_correct_cluster", nullable = false, precision = 7, scale = 2)
    private BigDecimal pointsPerCorrectCluster;

    /**
     * Creates a new instance of class {@link HierarchicalClusteringTask}.
     */
    public HierarchicalClusteringTask() {
    }

    /**
     * Creates a new instance of class {@link HierarchicalClusteringTask}.
     *
     * @param maxPoints The maximum points.
     * @param status    The status.
     */
    public HierarchicalClusteringTask(BigDecimal maxPoints, TaskStatus status) {
        super(maxPoints, status);
    }

    /**
     * Creates a new instance of class {@link HierarchicalClusteringTask}.
     *
     * @param maxPoints      The maximum points.
     * @param status         The status.
     * @param distanceMatrix The distance matrix for clustering.
     */
    public HierarchicalClusteringTask(BigDecimal maxPoints, TaskStatus status, DistanceMatrix distanceMatrix, BigDecimal pointsPerCorrectCluster) {
        super(maxPoints, status);
        this.distanceMatrix = distanceMatrix;
        this.pointsPerCorrectCluster = pointsPerCorrectCluster;
    }

    /**
     * Creates a new instance of class {@link HierarchicalClusteringTask}.
     *
     * @param id             The identifier.
     * @param maxPoints      The maximum points.
     * @param status         The status.
     * @param distanceMatrix The distance matrix for clustering.
     */
    public HierarchicalClusteringTask(Long id, BigDecimal maxPoints, TaskStatus status, DistanceMatrix distanceMatrix, BigDecimal pointsPerCorrectCluster) {
        super(id, maxPoints, status);
        this.distanceMatrix = distanceMatrix;
        this.pointsPerCorrectCluster = pointsPerCorrectCluster;
    }

    public LinkageMethodDto getLinkageMethod() {
        return linkageMethod;
    }

    public void setLinkageMethod(LinkageMethodDto linkageMethod) {
        this.linkageMethod = linkageMethod;
    }

    public DistanceMatrix getDistanceMatrix() {
        return distanceMatrix;
    }

    public void setDistanceMatrix(DistanceMatrix distanceMatrix) {
        this.distanceMatrix = distanceMatrix;
    }

    public BigDecimal getPointsPerCorrectCluster() {
        return pointsPerCorrectCluster;
    }

    public void setPointsPerCorrectCluster(BigDecimal pointsPerCorrectCluster) {
        this.pointsPerCorrectCluster = pointsPerCorrectCluster;
    }

    public static class DistanceMatrix {
        private List<String> labels;
        private double[][] distances;

        public DistanceMatrix(List<String> labels, double[][] distances) {
            this.labels = labels;
            this.distances = distances;
        }

        public List<String> getLabels() {
            return labels;
        }

        public void setLabels(List<String> labels) {
            this.labels = labels;
        }

        public double[][] getDistances() {
            return distances;
        }

        public void setDistances(double[][] distances) {
            this.distances = distances;
        }
    }
}

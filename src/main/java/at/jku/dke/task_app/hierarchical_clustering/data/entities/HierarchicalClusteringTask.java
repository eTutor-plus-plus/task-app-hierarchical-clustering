package at.jku.dke.task_app.hierarchical_clustering.data.entities;

import at.jku.dke.etutor.task_app.data.entities.BaseTask;
import at.jku.dke.etutor.task_app.dto.TaskStatus;
import at.jku.dke.task_app.hierarchical_clustering.data.converters.DendrogramModelConverter;
import at.jku.dke.task_app.hierarchical_clustering.data.converters.DistanceMetricConverter;
import at.jku.dke.task_app.hierarchical_clustering.data.converters.LinkageMethodConverter;
import at.jku.dke.task_app.hierarchical_clustering.dto.DistanceMetricDto;
import at.jku.dke.task_app.hierarchical_clustering.dto.LinkageMethodDto;
import at.jku.dke.task_app.hierarchical_clustering.generators.dendrogram.DendrogramModel;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a hierarchical clustering task.
 */
@Entity
@Table(name = "task")
public class HierarchicalClusteringTask extends BaseTask {

    @Convert(converter = DistanceMetricConverter.class)
    @Column(name = "metric", columnDefinition = "distance_metric")
    private DistanceMetricDto distanceMetric;

    @Convert(converter = LinkageMethodConverter.class)
    @Column(name = "linkage", columnDefinition = "linkage_method not null default 'single'", nullable = false)
    private LinkageMethodDto linkageMethod;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "coordinate_list")
    private List<CoordinatePoint> coordinateList;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "distance_matrix")
    private DistanceMatrix distanceMatrix;

    @Column(name = "points_per_correct_cluster", nullable = false, precision = 7, scale = 2)
    private BigDecimal pointsPerCorrectCluster;

    @Column(name = "wrong_order_penalty", precision = 7, scale = 2)
    private BigDecimal wrongOrderPenalty;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HierarchicalClusteringMerge> solutionMergeHistory = new ArrayList<>();

    @Convert(converter = DendrogramModelConverter.class)
    @Column(name = "dendrogram_model", columnDefinition = "jsonb", nullable = false)
    @JdbcTypeCode(SqlTypes.JSON)
    private DendrogramModel dendrogramModel;

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

    public DistanceMetricDto getDistanceMetric() {
        return distanceMetric;
    }

    public void setDistanceMetric(DistanceMetricDto distanceMetric) {
        this.distanceMetric = distanceMetric;
    }

    public LinkageMethodDto getLinkageMethod() {
        return linkageMethod;
    }

    public void setLinkageMethod(LinkageMethodDto linkageMethod) {
        this.linkageMethod = linkageMethod;
    }

    public List<CoordinatePoint> getCoordinateList() {
        return coordinateList;
    }

    public void setCoordinateList(List<CoordinatePoint> coordinateList) {
        this.coordinateList = coordinateList;
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

    public BigDecimal getWrongOrderPenalty() {
        return wrongOrderPenalty;
    }

    public void setWrongOrderPenalty(BigDecimal wrongOrderPenalty) {
        this.wrongOrderPenalty = wrongOrderPenalty;
    }

    public List<HierarchicalClusteringMerge> getSolutionMergeHistory() {
        return solutionMergeHistory;
    }

    public void setSolutionMergeHistory(List<HierarchicalClusteringMerge> solutionMergeHistory) {
        this.solutionMergeHistory = solutionMergeHistory;
    }

    public DendrogramModel getDendrogramModel() {
        return dendrogramModel;
    }

    public void setDendrogramModel(DendrogramModel dendrogramModel) {
        this.dendrogramModel = dendrogramModel;
    }

    public static class CoordinatePoint {
        private String label;
        private double x;
        private double y;

        public CoordinatePoint(String label, double x, double y) {
            this.label = label;
            this.x = x;
            this.y = y;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public double getX() {
            return x;
        }

        public void setX(double x) {
            this.x = x;
        }

        public double getY() {
            return y;
        }

        public void setY(double y) {
            this.y = y;
        }
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

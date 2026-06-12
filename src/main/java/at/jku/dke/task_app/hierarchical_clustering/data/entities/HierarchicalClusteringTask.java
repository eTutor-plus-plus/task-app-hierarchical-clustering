package at.jku.dke.task_app.hierarchical_clustering.data.entities;

import at.jku.dke.etutor.task_app.data.entities.BaseTask;
import at.jku.dke.etutor.task_app.dto.TaskStatus;
import at.jku.dke.task_app.hierarchical_clustering.data.converters.DistanceMetricConverter;
import at.jku.dke.task_app.hierarchical_clustering.data.converters.LinkageMethodConverter;
import at.jku.dke.task_app.hierarchical_clustering.generators.matrix.DistanceMetric;
import at.jku.dke.task_app.hierarchical_clustering.clustering.LinkageMethod;
import at.jku.dke.task_app.hierarchical_clustering.dendrogram.DendrogramModel;
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
    private DistanceMetric distanceMetric;

    @Convert(converter = LinkageMethodConverter.class)
    @Column(name = "linkage", columnDefinition = "linkage_method not null default 'single'", nullable = false)
    private LinkageMethod linkageMethod;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "coordinate_system")
    private CoordinateSystem coordinateSystem;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "distance_matrix")
    private DistanceMatrix distanceMatrix;

    @Column(name = "points_per_correct_cluster", nullable = false, precision = 7, scale = 2)
    private BigDecimal pointsPerCorrectCluster;

    @Column(name = "wrong_order_penalty", precision = 7, scale = 2)
    private BigDecimal wrongOrderPenalty;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HierarchicalClusteringMerge> solutionMergeHistory = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "dendrogram_model", columnDefinition = "jsonb", nullable = false)
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
     * @param id                      The identifier.
     * @param maxPoints               The maximum points.
     * @param status                  The status.
     * @param distanceMatrix          The distance matrix for clustering.
     * @param pointsPerCorrectCluster The points per correct new cluster.
     */
    public HierarchicalClusteringTask(Long id, BigDecimal maxPoints, TaskStatus status, DistanceMatrix distanceMatrix, BigDecimal pointsPerCorrectCluster) {
        super(id, maxPoints, status);
        this.distanceMatrix = distanceMatrix;
        this.pointsPerCorrectCluster = pointsPerCorrectCluster;
    }

    /**
     * Gets the distance metric.
     *
     * @return The distance metric.
     */
    public DistanceMetric getDistanceMetric() {
        return distanceMetric;
    }

    /**
     * Sets the distance metric.
     *
     * @param distanceMetric The distance metric.
     */
    public void setDistanceMetric(DistanceMetric distanceMetric) {
        this.distanceMetric = distanceMetric;
    }

    /**
     * Gets the linkage method.
     *
     * @return The linkage method.
     */
    public LinkageMethod getLinkageMethod() {
        return linkageMethod;
    }

    /**
     * Sets the linkage method.
     *
     * @param linkageMethod The linkage method.
     */
    public void setLinkageMethod(LinkageMethod linkageMethod) {
        this.linkageMethod = linkageMethod;
    }

    /**
     * Gets the coordinate system.
     *
     * @return The coordinate system.
     */
    public CoordinateSystem getCoordinateSystem() {
        return coordinateSystem;
    }

    /**
     * Sets the coordinate system.
     *
     * @param coordinateSystem The coordinate system.
     */
    public void setCoordinateSystem(CoordinateSystem coordinateSystem) {
        this.coordinateSystem = coordinateSystem;
    }

    /**
     * Gets the distance matrix.
     *
     * @return The distance matrix.
     */
    public DistanceMatrix getDistanceMatrix() {
        return distanceMatrix;
    }

    /**
     * Sets the distance matrix.
     *
     * @param distanceMatrix The distance matrix.
     */
    public void setDistanceMatrix(DistanceMatrix distanceMatrix) {
        this.distanceMatrix = distanceMatrix;
    }

    /**
     * Gets the points per correct cluster.
     *
     * @return The points per correct cluster.
     */
    public BigDecimal getPointsPerCorrectCluster() {
        return pointsPerCorrectCluster;
    }

    /**
     * Sets the points per correct cluster.
     *
     * @param pointsPerCorrectCluster The points per correct cluster.
     */
    public void setPointsPerCorrectCluster(BigDecimal pointsPerCorrectCluster) {
        this.pointsPerCorrectCluster = pointsPerCorrectCluster;
    }

    /**
     * Gets the wrong order penalty.
     *
     * @return The wrong order penalty.
     */
    public BigDecimal getWrongOrderPenalty() {
        return wrongOrderPenalty;
    }

    /**
     * Sets the wrong order penalty.
     *
     * @param wrongOrderPenalty The wrong order penalty.
     */
    public void setWrongOrderPenalty(BigDecimal wrongOrderPenalty) {
        this.wrongOrderPenalty = wrongOrderPenalty;
    }

    /**
     * Gets the solution merge history.
     *
     * @return The solution merge history.
     */
    public List<HierarchicalClusteringMerge> getSolutionMergeHistory() {
        return solutionMergeHistory;
    }

    /**
     * Sets the solution merge history.
     *
     * @param solutionMergeHistory The solution merge history.
     */
    public void setSolutionMergeHistory(List<HierarchicalClusteringMerge> solutionMergeHistory) {
        this.solutionMergeHistory = solutionMergeHistory;
    }

    /**
     * Gets the dendrogram model.
     *
     * @return The dendrogram model.
     */
    public DendrogramModel getDendrogramModel() {
        return dendrogramModel;
    }

    /**
     * Sets the dendrogram model.
     *
     * @param dendrogramModel The dendrogram model.
     */
    public void setDendrogramModel(DendrogramModel dendrogramModel) {
        this.dendrogramModel = dendrogramModel;
    }

    /**
     * Represents the coordinate system for a hierarchical clustering task.
     * <p>
     * Stores the minimum and maximum values for the x and y axes, as well as
     * a list of points with their coordinates.
     */
    public static class CoordinateSystem {
        private Integer minX;
        private Integer maxX;
        private Integer minY;
        private Integer maxY;
        private List<CoordinatePoint> coordinateList;

        /**
         * Creates a new instance of {@link CoordinateSystem}.
         *
         * @param minX           The minimum x value.
         * @param maxX           The maximum x value.
         * @param minY           The minimum y value.
         * @param maxY           The maximum y value.
         * @param coordinateList The list of coordinate points.
         */
        public CoordinateSystem(int minX, int maxX, int minY, int maxY, List<CoordinatePoint> coordinateList) {
            this.minX = minX;
            this.maxX = maxX;
            this.minY = minY;
            this.maxY = maxY;
            this.coordinateList = coordinateList;
        }

        /**
         * Gets the minimum x value.
         *
         * @return The minimum x value.
         */
        public Integer getMinX() {
            return minX;
        }

        /**
         * Sets the minimum x value.
         *
         * @param minX The minimum x value.
         */
        public void setMinX(Integer minX) {
            this.minX = minX;
        }

        /**
         * Gets the maximum x value.
         *
         * @return The maximum x value.
         */
        public Integer getMaxX() {
            return maxX;
        }

        /**
         * Sets the maximum x value.
         *
         * @param maxX The maximum x value.
         */
        public void setMaxX(Integer maxX) {
            this.maxX = maxX;
        }

        /**
         * Gets the minimum y value.
         *
         * @return The minimum y value.
         */
        public Integer getMinY() {
            return minY;
        }

        /**
         * Sets the minimum y value.
         *
         * @param minY The minimum y value.
         */
        public void setMinY(Integer minY) {
            this.minY = minY;
        }

        /**
         * Gets the maximum y value.
         *
         * @return The maximum y value.
         */
        public Integer getMaxY() {
            return maxY;
        }

        /**
         * Sets the maximum y value.
         *
         * @param maxY The maximum y value.
         */
        public void setMaxY(Integer maxY) {
            this.maxY = maxY;
        }

        /**
         * Gets the list of coordinate points.
         *
         * @return The list of coordinate points.
         */
        public List<CoordinatePoint> getCoordinateList() {
            return coordinateList;
        }

        /**
         * Sets the list of coordinate points.
         *
         * @param coordinateList The list of coordinate points.
         */
        public void setCoordinateList(List<CoordinatePoint> coordinateList) {
            this.coordinateList = coordinateList;
        }
    }

    /**
     * Represents a single point in the coordinate system.
     * <p>
     * Stores a label and the x and y coordinates of the point.
     */
    public static class CoordinatePoint {
        private String label;
        private BigDecimal x;
        private BigDecimal y;

        /**
         * Creates a new instance of {@link CoordinatePoint}.
         *
         * @param label The label of the point.
         * @param x     The x coordinate of the point.
         * @param y     The y coordinate of the point.
         */
        public CoordinatePoint(String label, BigDecimal x, BigDecimal y) {
            this.label = label;
            this.x = x;
            this.y = y;
        }

        /**
         * Gets the label of the point.
         *
         * @return The label of the point.
         */
        public String getLabel() {
            return label;
        }

        /**
         * Sets the label of the point.
         *
         * @param label The label of the point.
         */
        public void setLabel(String label) {
            this.label = label;
        }

        /**
         * Gets the x coordinate of the point.
         *
         * @return The x coordinate.
         */
        public BigDecimal getX() {
            return x;
        }

        /**
         * Sets the x coordinate of the point.
         *
         * @param x The x coordinate.
         */
        public void setX(BigDecimal x) {
            this.x = x;
        }

        /**
         * Gets the y coordinate of the point.
         *
         * @return The y coordinate.
         */
        public BigDecimal getY() {
            return y;
        }

        /**
         * Sets the y coordinate of the point.
         *
         * @param y The y coordinate.
         */
        public void setY(BigDecimal y) {
            this.y = y;
        }
    }


    /**
     * Represents a distance matrix for clustering.
     * <p>
     * Stores the labels of points and the pairwise distances between them.
     */
    public static class DistanceMatrix {
        private List<String> labels;
        private BigDecimal[][] distances;

        /**
         * Creates a new instance of {@link DistanceMatrix}.
         *
         * @param labels    The labels of the points.
         * @param distances The pairwise distances between points.
         */
        public DistanceMatrix(List<String> labels, BigDecimal[][] distances) {
            this.labels = labels;
            this.distances = distances;
        }

        /**
         * Gets the labels of the points.
         *
         * @return The labels of the points.
         */
        public List<String> getLabels() {
            return labels;
        }

        /**
         * Sets the labels of the points.
         *
         * @param labels The labels of the points.
         */
        public void setLabels(List<String> labels) {
            this.labels = labels;
        }

        /**
         * Gets the pairwise distances between points.
         *
         * @return The distances.
         */
        public BigDecimal[][] getDistances() {
            return distances;
        }

        /**
         * Sets the pairwise distances between points.
         *
         * @param distances The distances.
         */
        public void setDistances(BigDecimal[][] distances) {
            this.distances = distances;
        }
    }
}

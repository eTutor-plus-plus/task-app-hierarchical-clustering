package at.jku.dke.task_app.hierarchical_clustering.data.entities;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Represents a single merge in the clustering/merge history of a task.
 */
@Entity
@Table(name = "merge")
public class HierarchicalClusteringMerge {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_cluster_1", nullable = false)
    private HierarchicalClusteringCluster sourceCluster1;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_cluster_2", nullable = false)
    private HierarchicalClusteringCluster sourceCluster2;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "result", nullable = false)
    private HierarchicalClusteringCluster result;

    @Column(name = "distance", nullable = false)
    private BigDecimal distance;

    @Column(name = "step", nullable = false)
    private Integer step;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id")
    private HierarchicalClusteringTask task;

    /**
     * Creates a new instance of class {@linkplain HierarchicalClusteringMerge}.
     */
    public HierarchicalClusteringMerge() {}

    /**
     * Creates a new instance of class {@linkplain HierarchicalClusteringMerge}.
     *
     * @param source1  The first source cluster.
     * @param source2  The second source cluster.
     * @param merged   The resulting cluster.
     * @param distance The distance/height of the merge.
     * @param step     The step of the merge (ordering).
     */
    public HierarchicalClusteringMerge(HierarchicalClusteringCluster source1, HierarchicalClusteringCluster source2, HierarchicalClusteringCluster merged, BigDecimal distance, int step) {
        this.sourceCluster1 = source1;
        this.sourceCluster2 = source2;
        this.result = merged;
        this.distance = distance;
        this.step = step;
    }

    /**
     * Sets the identifier.
     *
     * @param id The identifier.
     */
    public void setId(UUID id) {
        this.id = id;
    }

    /**
     * Gets the identifier.
     *
     * @return The identifier.
     */
    public UUID getId() {
        return id;
    }

    /**
     * Gets the first source cluster.
     *
     * @return The first source cluster.
     */
    public HierarchicalClusteringCluster getSourceCluster1() {
        return sourceCluster1;
    }

    /**
     * Sets the first source cluster.
     *
     * @param clusterLeft The first source cluster.
     */
    public void setSourceCluster1(HierarchicalClusteringCluster clusterLeft) {
        this.sourceCluster1 = clusterLeft;
    }

    /**
     * Gets the second source cluster.
     *
     * @return The second source cluster.
     */
    public HierarchicalClusteringCluster getSourceCluster2() {
        return sourceCluster2;
    }

    /**
     * Sets the second source cluster.
     *
     * @param clusterRight The second source cluster.
     */
    public void setSourceCluster2(HierarchicalClusteringCluster clusterRight) {
        this.sourceCluster2 = clusterRight;
    }

    /**
     * Gets the resulting cluster.
     *
     * @return The resulting cluster.
     */
    public HierarchicalClusteringCluster getResult() {
        return result;
    }

    /**
     * Sets the resulting cluster.
     *
     * @param result The resulting cluster.
     */
    public void setResult(HierarchicalClusteringCluster result) {
        this.result = result;
    }

    /**
     * Gets the distance of the merge.
     *
     * @return The distance of the merge.
     */
    public BigDecimal getDistance() {
        return distance;
    }

    /**
     * Sets the distance of the merge.
     *
     * @param distance The distance of the merge.
     */
    public void setDistance(BigDecimal distance) {
        this.distance = distance;
    }

    /**
     * Gets the clustering step.
     *
     * @return The clustering step.
     */
    public Integer getStep() {
        return step;
    }

    /**
     * Sets the clustering step.
     *
     * @param step The clustering step.
     */
    public void setStep(Integer step) {
        this.step = step;
    }

    /**
     * Gets the associated task.
     *
     * @return The associated task.
     */
    public HierarchicalClusteringTask getTask() {
        return task;
    }

    /**
     * Sets the associated task.
     *
     * @param task The associated task.
     */
    public void setTask(HierarchicalClusteringTask task) {
        this.task = task;
    }

    @Override
    public String toString() {
        // for displaying purposes
        return "Distance " + distance + ": " + result.getFullLabel();
    }
}

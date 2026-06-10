package at.jku.dke.task_app.hierarchical_clustering.data.entities;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Represents a single merge in the clustering/dendrogram of a task.
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

    public HierarchicalClusteringMerge() {}

    public HierarchicalClusteringMerge(HierarchicalClusteringCluster source1, HierarchicalClusteringCluster source2, HierarchicalClusteringCluster merged, BigDecimal distance, int step) {
        this.sourceCluster1 = source1;
        this.sourceCluster2 = source2;
        this.result = merged;
        this.distance = distance;
        this.step = step;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getId() {
        return id;
    }

    public HierarchicalClusteringCluster getSourceCluster1() {
        return sourceCluster1;
    }

    public void setSourceCluster1(HierarchicalClusteringCluster clusterLeft) {
        this.sourceCluster1 = clusterLeft;
    }

    public HierarchicalClusteringCluster getSourceCluster2() {
        return sourceCluster2;
    }

    public void setSourceCluster2(HierarchicalClusteringCluster clusterRight) {
        this.sourceCluster2 = clusterRight;
    }

    public HierarchicalClusteringCluster getResult() {
        return result;
    }

    public void setResult(HierarchicalClusteringCluster result) {
        this.result = result;
    }

    public BigDecimal getDistance() {
        return distance;
    }

    public void setDistance(BigDecimal distance) {
        this.distance = distance;
    }

    public Integer getStep() {
        return step;
    }

    public void setStep(Integer step) {
        this.step = step;
    }

    public HierarchicalClusteringTask getTask() {
        return task;
    }

    public void setTask(HierarchicalClusteringTask task) {
        this.task = task;
    }

    @Override
    public String toString() {
        return "Distance " + distance + ": " + result.getFullLabel();
    }
}

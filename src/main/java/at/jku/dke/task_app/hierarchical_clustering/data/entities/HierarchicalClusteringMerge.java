package at.jku.dke.task_app.hierarchical_clustering.data.entities;

import jakarta.persistence.*;

import java.util.Objects;
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
    private double distance;

    @Column(name = "step", nullable = false)
    private int step;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id")
    private HierarchicalClusteringTask task;

    public HierarchicalClusteringMerge() {}

    public HierarchicalClusteringMerge(HierarchicalClusteringCluster source1, HierarchicalClusteringCluster source2, HierarchicalClusteringCluster merged, double distance, int step) {
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

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
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

    // can not be used to compare an input merge and a solution merge because source clusters will not be equal
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof HierarchicalClusteringMerge merge)) return false;
        return Double.compare(distance, merge.distance) == 0 &&
            sourceCluster1.equals(merge.sourceCluster1) &&
            sourceCluster2.equals(merge.sourceCluster2) &&
            result.equals(merge.result);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourceCluster1, sourceCluster2, result, distance, step);
    }
}

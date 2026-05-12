package at.jku.dke.task_app.hierarchical_clustering.data.entities;

import jakarta.persistence.*;

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
    @JoinColumn(name = "cluster_left", nullable = false)
    private HierarchicalClusteringCluster clusterLeft;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cluster_right", nullable = false)
    private HierarchicalClusteringCluster clusterRight;

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

    public HierarchicalClusteringMerge(HierarchicalClusteringCluster left, HierarchicalClusteringCluster right, HierarchicalClusteringCluster merged, double distance, int step) {
        this.clusterLeft = left;
        this.clusterRight = right;
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

    public HierarchicalClusteringCluster getClusterLeft() {
        return clusterLeft;
    }

    public void setClusterLeft(HierarchicalClusteringCluster clusterLeft) {
        this.clusterLeft = clusterLeft;
    }

    public HierarchicalClusteringCluster getClusterRight() {
        return clusterRight;
    }

    public void setClusterRight(HierarchicalClusteringCluster clusterRight) {
        this.clusterRight = clusterRight;
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
}

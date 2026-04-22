package at.jku.dke.task_app.hierarchical_clustering.data.entities;

import jakarta.persistence.*;

import java.util.List;
import java.util.UUID;

/**
 * Represents a cluster (including single data points that act as clusters in the initial step of agglomerative clustering).
 */
@Entity
@Table(name = "cluster")
public class HierarchicalClusteringCluster {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "label")
    private String label;

    @Column(name = "data_points")
    private List<String> dataPoints;

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getId() {
        return id;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public void setDataPoints(List<String> dataPoints) {
        this.dataPoints = dataPoints;
    }

    public List<String> getDataPoints() {
        return dataPoints;
    }
}

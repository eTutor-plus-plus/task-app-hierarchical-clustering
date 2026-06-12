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

    /**
     * Creates a new instance of class {@linkplain HierarchicalClusteringCluster}.
     */
    public HierarchicalClusteringCluster() {}

    /**
     * Creates a new instance of class {@linkplain HierarchicalClusteringCluster}.
     *
     * @param uuid       The identifier.
     * @param dataPoints The data points in the cluster.
     */
    public HierarchicalClusteringCluster(UUID uuid, List<String> dataPoints) {
        this.id = uuid;
        setDataPoints(dataPoints);
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
     * Sets the label based on the provided data points.
     *
     * @param dataPoints The data points used to generate the label.
     */
    private void setLabel(List<String> dataPoints) {
        this.label = String.join(",", dataPoints);
    }

    /**
     * Gets the label.
     *
     * @return The label.
     */
    public String getLabel() {
        return label;
    }

    /**
     * Gets the full label enclosed in parentheses.
     *
     * @return The full label.
     */
    public String getFullLabel() {
        return "(" + label + ")";
    }

    /**
     * Sets the data points and updates the label accordingly.
     *
     * @param dataPoints The data points.
     */
    public void setDataPoints(List<String> dataPoints) {
        this.dataPoints = dataPoints;
        setLabel(dataPoints);
    }

    /**
     * Gets the data points.
     *
     * @return The data points.
     */
    public List<String> getDataPoints() {
        return dataPoints;
    }
}

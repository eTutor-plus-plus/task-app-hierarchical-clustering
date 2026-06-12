package at.jku.dke.task_app.hierarchical_clustering.data.repositories;

import at.jku.dke.task_app.hierarchical_clustering.data.entities.HierarchicalClusteringCluster;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface HierarchicalClusteringClusterRepository extends JpaRepository<HierarchicalClusteringCluster, UUID> {
}

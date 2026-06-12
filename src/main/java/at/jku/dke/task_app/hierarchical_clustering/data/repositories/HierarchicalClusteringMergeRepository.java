package at.jku.dke.task_app.hierarchical_clustering.data.repositories;

import at.jku.dke.task_app.hierarchical_clustering.data.entities.HierarchicalClusteringMerge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface HierarchicalClusteringMergeRepository extends JpaRepository<HierarchicalClusteringMerge, UUID> {
}

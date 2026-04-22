package at.jku.dke.task_app.hierarchical_clustering.data.repositories;

import at.jku.dke.task_app.hierarchical_clustering.data.entities.HierarchicalClusteringMerge;
import at.jku.dke.task_app.hierarchical_clustering.data.entities.HierarchicalClusteringTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface HierarchicalClusteringMergeRepository extends JpaRepository<HierarchicalClusteringMerge, UUID> {

    @Query("SELECT m FROM HierarchicalClusteringMerge m WHERE m.task = :task")
    List<HierarchicalClusteringMerge> findByTask(@Param("task") HierarchicalClusteringTask task);

    @Query("SELECT m FROM HierarchicalClusteringMerge m WHERE m.task = :task ORDER BY m.step ASC")
    List<HierarchicalClusteringMerge> findByTaskSorted(@Param("task") HierarchicalClusteringTask task);

}

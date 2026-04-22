package at.jku.dke.task_app.hierarchical_clustering.data.repositories;

import at.jku.dke.task_app.hierarchical_clustering.data.entities.HierarchicalClusteringCluster;
import at.jku.dke.task_app.hierarchical_clustering.data.entities.HierarchicalClusteringMerge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface HierarchicalClusteringClusterRepository extends JpaRepository<HierarchicalClusteringCluster, UUID> {

    @Query("""
        SELECT c
        FROM HierarchicalClusteringCluster c
        JOIN HierarchicalClusteringMerge m
        WHERE m = :merge AND (c.id = m.clusterLeft.id OR c.id = m.clusterRight.id)
        """)
    List<HierarchicalClusteringCluster> findAsChildFromMerge(@Param("merge") HierarchicalClusteringMerge merge);

    @Query("""
        SELECT c
        FROM HierarchicalClusteringCluster c
        JOIN HierarchicalClusteringMerge m
        WHERE m = :merge AND c.id = m.result.id
        """)
    List<HierarchicalClusteringCluster> findAsResultFromMerge(@Param("merge") HierarchicalClusteringMerge merge);

}

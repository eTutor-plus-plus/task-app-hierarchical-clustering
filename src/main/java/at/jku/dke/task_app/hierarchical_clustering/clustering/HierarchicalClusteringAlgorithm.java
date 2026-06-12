package at.jku.dke.task_app.hierarchical_clustering.clustering;

import at.jku.dke.task_app.hierarchical_clustering.data.entities.HierarchicalClusteringMerge;
import at.jku.dke.task_app.hierarchical_clustering.data.entities.HierarchicalClusteringTask;

import java.util.List;

/**
 * Represents a clustering algorithm for hierarchical clustering.
 *
 * @see NaiveAgglomerativeClusteringAlgorithm
 */
public interface HierarchicalClusteringAlgorithm {

    /**
     * Computes the clustering from the given distance matrix.
     *
     * @param distanceMatrix The distance matrix used for clustering.
     * @return The list of merges computed by the algorithm.
     */
    List<HierarchicalClusteringMerge> cluster(HierarchicalClusteringTask.DistanceMatrix distanceMatrix);

}

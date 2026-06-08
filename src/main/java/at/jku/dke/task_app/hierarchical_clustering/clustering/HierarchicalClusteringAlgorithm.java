package at.jku.dke.task_app.hierarchical_clustering.clustering;

import at.jku.dke.task_app.hierarchical_clustering.data.entities.HierarchicalClusteringMerge;
import at.jku.dke.task_app.hierarchical_clustering.data.entities.HierarchicalClusteringTask;

import java.util.List;

public interface HierarchicalClusteringAlgorithm {

    List<HierarchicalClusteringMerge> cluster(HierarchicalClusteringTask.DistanceMatrix distanceMatrix);

}

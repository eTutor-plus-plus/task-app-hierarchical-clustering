package at.jku.dke.task_app.hierarchical_clustering.generators;

import at.jku.dke.task_app.hierarchical_clustering.data.entities.HierarchicalClusteringTask;

public interface DistanceMetric {
	double distance(HierarchicalClusteringTask.CoordinatePoint p1, HierarchicalClusteringTask.CoordinatePoint p2);
}

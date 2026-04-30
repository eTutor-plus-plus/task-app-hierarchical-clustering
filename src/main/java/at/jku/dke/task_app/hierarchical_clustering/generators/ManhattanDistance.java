package at.jku.dke.task_app.hierarchical_clustering.generators;

import at.jku.dke.task_app.hierarchical_clustering.data.entities.HierarchicalClusteringTask;

public class ManhattanDistance implements DistanceMetric {
	@Override
	public double distance(HierarchicalClusteringTask.CoordinatePoint p1, HierarchicalClusteringTask.CoordinatePoint p2) {
		double sum = 0.0;

		sum += Math.abs(p1.getX() - p2.getX());
		sum += Math.abs(p1.getY() - p2.getY());

		return sum;
	}
}

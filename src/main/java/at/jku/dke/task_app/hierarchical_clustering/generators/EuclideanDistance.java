package at.jku.dke.task_app.hierarchical_clustering.generators;

import at.jku.dke.task_app.hierarchical_clustering.data.entities.HierarchicalClusteringTask;

public class EuclideanDistance implements DistanceMetric {
	@Override
	public double distance(HierarchicalClusteringTask.CoordinatePoint p1, HierarchicalClusteringTask.CoordinatePoint p2) {
		double sum = 0.0;

		double diffX = p1.getX() - p2.getX();
		sum += diffX * diffX;
		double diffY = p1.getY() - p2.getY();
		sum += diffY * diffY;

		return Math.sqrt(sum);
	}
}

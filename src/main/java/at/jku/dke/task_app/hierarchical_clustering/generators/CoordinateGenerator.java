package at.jku.dke.task_app.hierarchical_clustering.generators;

import at.jku.dke.task_app.hierarchical_clustering.data.entities.HierarchicalClusteringTask;

import java.util.List;

public interface CoordinateGenerator {
	default List<HierarchicalClusteringTask.CoordinatePoint> generate(int n, double length) {
		return this.generate(n, length, length);
	}

	List<HierarchicalClusteringTask.CoordinatePoint> generate(int n, double lengthX, double lengthY);
}

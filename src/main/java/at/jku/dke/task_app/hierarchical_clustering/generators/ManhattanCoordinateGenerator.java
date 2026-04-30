package at.jku.dke.task_app.hierarchical_clustering.generators;

import at.jku.dke.task_app.hierarchical_clustering.data.entities.HierarchicalClusteringTask;

import java.util.*;

public class ManhattanCoordinateGenerator implements CoordinateGenerator {
	@Override
	public List<HierarchicalClusteringTask.CoordinatePoint> generate(int n, double lengthX, double lengthY) {
        List<double[]> candidates = new ArrayList<>();
        for (double x = 0; x <= lengthX + 1e-9; x += 0.1) {
            for (double y = 0; y <= lengthY + 1e-9; y += 0.1) {
                candidates.add(new double[]{Math.round(x * 10) / 10.0, Math.round(y * 10) / 10.0});
            }
        }
        Collections.shuffle(candidates);

        List<HierarchicalClusteringTask.CoordinatePoint> points = new ArrayList<>();
        Set<Double> usedDistances = new HashSet<>();

        for (double[] c : candidates) {
            Set<Double> newDists = new HashSet<>();
            boolean valid = true;

            for (var p : points) {
                double dist = Math.round((Math.abs(c[0] - p.getX()) + Math.abs(c[1] - p.getY())) * 10) / 10.0;
                if (usedDistances.contains(dist) || newDists.contains(dist)) {
                    valid = false;
                    break;
                }
                newDists.add(dist);
            }

            if (valid) {
                points.add(new HierarchicalClusteringTask.CoordinatePoint(String.valueOf(points.size() + 1), c[0], c[1]));
                usedDistances.addAll(newDists);
                if (points.size() == n) break;
            }
        }

        if (points.size() < n) {
            throw new RuntimeException("Cannot generate enough points with unique Manhattan distances in the given space");
        }
        return points;
	}
}

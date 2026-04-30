package at.jku.dke.task_app.hierarchical_clustering.generators;

import at.jku.dke.task_app.hierarchical_clustering.data.entities.HierarchicalClusteringTask;

import java.util.*;

public class EuclideanCoordinateGenerator implements CoordinateGenerator {

    @Override
	public List<HierarchicalClusteringTask.CoordinatePoint> generate(int n, double lengthX, double lengthY) {
		// Build candidate grid: multiples of 0.5 within bounds
		List<double[]> grid = buildGrid(lengthX, lengthY);

		if (grid.size() < n) {
			throw new IllegalArgumentException(
					"Not enough grid positions (" + grid.size() + ") for " + n + " points in a "
							+ lengthX + "x" + lengthY + " space. Increase space size or reduce n.");
		}

		Random rng = new Random();
		Collections.shuffle(grid, rng);

		List<HierarchicalClusteringTask.CoordinatePoint> result = new ArrayList<>();
		// Store squared distances (×4 to work in integers) to avoid floating-point ambiguity
		Set<Long> usedDistancesSq = new HashSet<>();
		List<HierarchicalClusteringTask.CoordinatePoint> placed = new ArrayList<>();

		for (double[] candidate : grid) {
			if (placed.size() == n) break;

			double cx = candidate[0];
			double cy = candidate[1];

			// Compute squared distances (×4) to all already-placed points
			List<Long> newDistsSq = new ArrayList<>();
			boolean collision = false;

			for (HierarchicalClusteringTask.CoordinatePoint existing : placed) {
				double dx = cx - existing.getX();
				double dy = cy - existing.getY();
				// Multiply by 4 and round to integer to avoid floating-point issues
				long dSq4 = Math.round(4.0 * (dx * dx + dy * dy));
				if (usedDistancesSq.contains(dSq4)) {
					collision = true;
					break;
				}
				// Also check among the new distances themselves (no duplicates within batch)
				if (newDistsSq.contains(dSq4)) {
					collision = true;
					break;
				}
				newDistsSq.add(dSq4);
			}

			if (!collision) {
				String id = String.valueOf(placed.size() + 1);
				HierarchicalClusteringTask.CoordinatePoint dp = new HierarchicalClusteringTask.CoordinatePoint(id, cx, cy);
				placed.add(dp);
				result.add(dp);
				usedDistancesSq.addAll(newDistsSq);
			}
		}

		if (placed.size() < n) {
			throw new IllegalStateException(
					"Could only place " + placed.size() + " of " + n
							+ " points without distance duplicates. "
							+ "Try a larger space or fewer points.");
		}

		return result;
	}

	private List<double[]> buildGrid(double lengthX, double lengthY) {
		List<double[]> grid = new ArrayList<>();
		// Step of 0.5 ensures (a-b)^2 is always a multiple of 0.25,
		// and sqrt of a sum of two such values rounds cleanly to 1 decimal.
		double step = 0.5;
		for (double x = 0.0; x <= lengthX + 1e-9; x += step) {
			double rx = Math.round(x * 10.0) / 10.0;
			for (double y = 0.0; y <= lengthY + 1e-9; y += step) {
				double ry = Math.round(y * 10.0) / 10.0;
				grid.add(new double[]{rx, ry});
			}
		}
		return grid;
	}
}

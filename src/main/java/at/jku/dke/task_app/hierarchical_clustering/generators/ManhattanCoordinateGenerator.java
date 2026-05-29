package at.jku.dke.task_app.hierarchical_clustering.generators;

import at.jku.dke.task_app.hierarchical_clustering.data.entities.HierarchicalClusteringTask;

import java.util.*;

public class ManhattanCoordinateGenerator extends CoordinateGenerator {

    private static final int maxRestarts = 100;

    @Override
	public List<HierarchicalClusteringTask.CoordinatePoint> generate(int n, double minX, double maxX, double minY, double maxY, Random random) {
        int minStepX = (int) (minX * 10);
        int maxStepX = (int) (maxX * 10);
        int minStepY = (int) (minY * 10);
        int maxStepY = (int) (maxY * 10);
        List<int[]> candidates = buildGenericCandidatePool(minStepX, maxStepX, minStepY, maxStepY);

        Collections.shuffle(candidates, random);

        List<HierarchicalClusteringTask.CoordinatePoint> points = new ArrayList<>();
        Set<Integer> usedDistances = new HashSet<>();

        for (int restart = 0; restart <= maxRestarts; restart += 1) {
            for (int[] c : candidates) {
                Set<Integer> newDists = new HashSet<>();
                boolean valid = true;

                for (var p : points) {
                    int dist = Math.abs(c[0] - (int) (p.getX() * 10)) + Math.abs(c[1] - (int) (p.getY() * 10));

                    if (usedDistances.contains(dist) || newDists.contains(dist)) {
                        valid = false;
                        break;
                    }

                    newDists.add(dist);
                }

                if (valid) {
                    points.add(new HierarchicalClusteringTask.CoordinatePoint(String.valueOf(points.size() + 1), c[0] / 10.0, c[1] / 10.0));
                    usedDistances.addAll(newDists);
                    if (points.size() == n) {
                        return points;
                    }
                }
            }
        }

        if (points.size() < n) {
            throw new RuntimeException("Could not generate " + n + " points satisfying all constraints after " +
                maxRestarts + " restarts. Retry generation by saving again, or try a larger grid or fewer points.");
        }

        return points;
	}
}

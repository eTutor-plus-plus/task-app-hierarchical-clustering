package at.jku.dke.task_app.hierarchical_clustering.generators.coordinates;

import at.jku.dke.task_app.hierarchical_clustering.data.entities.HierarchicalClusteringTask;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;

public class ManhattanCoordinateGenerator extends CoordinateGenerator {


    @Override
	public List<HierarchicalClusteringTask.CoordinatePoint> generate(int n, double minX, double maxX, double minY, double maxY, Random random) {
        int minStepX = (int) (minX * 10);
        int maxStepX = (int) (maxX * 10);
        int minStepY = (int) (minY * 10);
        int maxStepY = (int) (maxY * 10);
        List<int[]> candidates = buildGenericCandidatePool(minStepX, maxStepX, minStepY, maxStepY);

        if (candidates.size() < n) {
            throw new IllegalArgumentException(
                "The grid (" + candidates.size() + " points) is too small to hold " + n + " points."
            );
        }

        Collections.shuffle(candidates, random);

        for (int restart = 0; restart <= Config.maxRestarts; restart += 1) {
            List<HierarchicalClusteringTask.CoordinatePoint> points = new ArrayList<>();
            Set<Integer> usedDistances = new HashSet<>();

            List<int[]> remaining = new ArrayList<>(candidates);
            boolean runFailed = false;

            for (int i = 0; i < n; i++) {
                boolean placedFlag = false;
                int attempts = 0;

                Collections.shuffle(remaining, random);

                Iterator<int[]> it = remaining.iterator();

                while (it.hasNext() && attempts < Config.maxAttemptsPerPoint) {
                    int[] cand = it.next();
                    attempts++;

                    Set<Integer> newDists = new HashSet<>();
                    boolean valid = true;
                    for (var p : points) {
                        int dist = Math.abs(cand[0] - (int) (p.getX() * 10)) + Math.abs(cand[1] - (int) (p.getY() * 10));

                        if (usedDistances.contains(dist) || newDists.contains(dist)) {
                            valid = false;
                            break;
                        }

                        newDists.add(dist);
                    }

                    if (!valid) continue;

                    // Place the point
                    points.add(new HierarchicalClusteringTask.CoordinatePoint(String.valueOf(points.size() + 1), cand[0] / 10.0, cand[1] / 10.0));

                    usedDistances.addAll(newDists);

                    it.remove();
                    placedFlag = true;
                    break;
                }

                if (!placedFlag) {
                    runFailed = true;
                    break;
                }
            }

            if (!runFailed) {
                return points;
            }
        }

        throw new RuntimeException(
            "Could not generate " + n + " points satisfying all constraints after " +
                Config.maxRestarts + " restarts. Try a larger grid or fewer points."
        );
    }

    @Component
    static class Config {
        private static int maxRestarts;
        private static int maxAttemptsPerPoint;

        @Value("${app.generation.coordinates.manhattan.restarts}")
        public void setMaxRestarts(int maxRestarts) {
            Config.maxRestarts = maxRestarts;
        }

        @Value("${app.generation.coordinates.manhattan.max-attempts-per-point}")
        public void setMaxAttemptsPerPoint(int maxAttemptsPerPoint) {
            Config.maxAttemptsPerPoint = maxAttemptsPerPoint;
        }
    }
}

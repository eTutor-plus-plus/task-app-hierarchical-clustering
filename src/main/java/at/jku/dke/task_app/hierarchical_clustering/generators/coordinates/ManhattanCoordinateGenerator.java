package at.jku.dke.task_app.hierarchical_clustering.generators.coordinates;

import at.jku.dke.task_app.hierarchical_clustering.data.entities.HierarchicalClusteringTask;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * A generator that can generate a list of data points with their coordinates.
 * Coordinates are generated in a way that they fit all constraints after
 * calculating the distances with manhattan distance.
 */
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
                        int dist = Math.abs(cand[0] - (p.getX().multiply(BigDecimal.TEN).intValue())) + Math.abs(cand[1] - (p.getY().multiply(BigDecimal.TEN).intValue()));

                        if (usedDistances.contains(dist) || newDists.contains(dist)) {
                            valid = false;
                            break;
                        }

                        newDists.add(dist);
                    }

                    if (!valid) continue;

                    // Place the point
                    points.add(new HierarchicalClusteringTask.CoordinatePoint(
                        String.valueOf(points.size() + 1),
                        BigDecimal.valueOf(cand[0]).divide(BigDecimal.TEN, 1, RoundingMode.HALF_UP).stripTrailingZeros(),
                        BigDecimal.valueOf(cand[1]).divide(BigDecimal.TEN, 1, RoundingMode.HALF_UP).stripTrailingZeros()
                    ));

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

    /**
     * Spring component responsible for injecting configuration values
     * into static fields for access.
     * <p>
     * Separate config for the coordinate generators as different
     * configuration of values may be favourable (e.g. enabling
     * more restarts for Manhattan coordinate generation because
     * it takes generally less time and therefore more runs are
     * feasible compared to Euclidean coordinate generation)
     */
    @Component
    public static class Config {
        private static int maxRestarts;
        private static int maxAttemptsPerPoint;

        /**
         * Sets the {@linkplain #maxRestarts} value from the Spring property
         * {@code app.generation.coordinates.manhattan.restarts}.
         * <p>
         * This method is called by Spring automatically; do not call it manually.
         * </p>
         *
         * @param maxRestarts The maximum amount of restarts for trying coordinate generation.
         */
        @Value("${app.generation.coordinates.manhattan.restarts}")
        public void setMaxRestarts(int maxRestarts) {
            Config.maxRestarts = maxRestarts;
        }

        /**
         * Sets the {@linkplain #maxAttemptsPerPoint} value from the Spring property
         * {@code app.generation.coordinates.manhattan.max-attempts-per-point}.
         * <p>
         * This method is called by Spring automatically; do not call it manually.
         * </p>
         *
         * @param maxAttemptsPerPoint The maximum amount of retries for single points during generation.
         */
        @Value("${app.generation.coordinates.manhattan.max-attempts-per-point}")
        public void setMaxAttemptsPerPoint(int maxAttemptsPerPoint) {
            Config.maxAttemptsPerPoint = maxAttemptsPerPoint;
        }
    }
}

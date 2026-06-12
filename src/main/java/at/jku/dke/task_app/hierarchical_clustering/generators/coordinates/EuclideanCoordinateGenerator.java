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
 * calculating the distances with euclidean distance.
 */
public class EuclideanCoordinateGenerator extends CoordinateGenerator {

    @Override
    public List<HierarchicalClusteringTask.CoordinatePoint> generate(int n, double minX, double maxX, double minY, double maxY, Random random) {
        // arithmetic is done in integer tenths to avoid floating-point error
        int xMinSteps = (int) Math.round(minX * 10);
        int xMaxSteps = (int) Math.round(maxX * 10);
        int yMinSteps = (int) Math.round(minY * 10);
        int yMaxSteps = (int) Math.round(maxY * 10);
        // to allow for only a number of coordinates to be the same (as coordinate generation tends to fill one axis with the same coordinates otherwise)
        int maxSharedCoordinates = (int) Math.ceil((double) n / Math.min(xMaxSteps + 1, yMaxSteps + 1) * 1.5);
        maxSharedCoordinates = Math.max(1, maxSharedCoordinates); // always allow at least 1

        List<int[]> allCandidates = buildGenericCandidatePool(xMinSteps, xMaxSteps, yMinSteps, yMaxSteps);

        if (allCandidates.size() < n) {
            throw new IllegalArgumentException(
                "The grid (" + allCandidates.size() + " points) is too small to hold " + n + " points."
            );
        }

        for (int restart = 0; restart <= Config.maxRestarts; restart++) {
            List<HierarchicalClusteringTask.CoordinatePoint> result    = new ArrayList<>();
            List<int[]> placed = new ArrayList<>();
            Set<Double> usedDists = new HashSet<>();

            // Track how many placed points share each x or y value (in tenths)
            Map<Integer, Integer> xCount = new HashMap<>();
            Map<Integer, Integer> yCount = new HashMap<>();

            List<int[]> remaining = new ArrayList<>(allCandidates);
            boolean runFailed = false;

            for (int i = 0; i < n; i++) {
                boolean placedFlag = false;
                int attempts = 0;

                Collections.shuffle(remaining, random);

                Iterator<int[]> it = remaining.iterator();

                while (it.hasNext() && attempts < Config.maxAttemptsPerPoint) {
                    int[] cand = it.next();
                    attempts++;

                    // Reject if this candidate would push any axis count over the limit
                    if (xCount.getOrDefault(cand[0], 0) >= maxSharedCoordinates) continue;
                    if (yCount.getOrDefault(cand[1], 0) >= maxSharedCoordinates) continue;

                    if (isValidCandidate(cand, placed, usedDists)) {
                        Set<Double> newDists = newDistances(cand, placed);

                        result.add(new HierarchicalClusteringTask.CoordinatePoint(
                            String.valueOf(i + 1),
                            BigDecimal.valueOf(cand[0]).divide(BigDecimal.TEN, 1, RoundingMode.HALF_UP).stripTrailingZeros(),
                            BigDecimal.valueOf(cand[1]).divide(BigDecimal.TEN, 1, RoundingMode.HALF_UP).stripTrailingZeros()
                        ));

                        placed.add(cand);
                        usedDists.addAll(newDists);
                        xCount.merge(cand[0], 1, Integer::sum);
                        yCount.merge(cand[1], 1, Integer::sum);

                        it.remove();
                        placedFlag = true;
                        break;
                    }
                }

                if (!placedFlag) {
                    runFailed = true;
                    break;
                }
            }

            if (!runFailed) {
                return result;
            }
        }

        throw new RuntimeException(
            "Could not generate " + n + " points satisfying all constraints after " +
                Config.maxRestarts + " restarts. Try a larger grid or fewer points."
        );
    }

    /**
     * Checks whether the Euclidean distance defined by the given integer
     * coordinate differences can be represented exactly with one decimal place.
     * <p>
     * The method operates on integer tenths (i.e., scaled by factor 10) and
     * verifies whether the squared distance is a perfect square. This ensures
     * that the resulting distance has an exact decimal representation when
     * divided by 10.
     * </p>
     *
     * @param dxi The difference in x-coordinates (scaled by 10).
     * @param dyi The difference in y-coordinates (scaled by 10).
     * @return {@code true} if the distance can be represented exactly with one decimal place,
     *         {@code false} otherwise.
     */
    private static boolean hasExactOneDecimalDistance(int dxi, int dyi) {
        if (dxi == 0 && dyi == 0) {
            return true;
        }

        long sumSq = (long) dxi * dxi + (long) dyi * dyi;
        long root  = Math.round(Math.sqrt(sumSq));

        for (long r = Math.max(0, root - 1); r <= root + 1; r++) {
            if (r * r == sumSq) {
                return true;
            }
        }

        return false;
    }

    /**
     * Computes the exact Euclidean distance (with one decimal precision)
     * between two points represented by their coordinate differences.
     * <p>
     * This method assumes that {@link #hasExactOneDecimalDistance(int, int)}
     * has already been checked and will throw an exception if the distance
     * is not an exact square root.
     * </p>
     *
     * @param dxi The difference in x-coordinates (scaled by 10).
     * @param dyi The difference in y-coordinates (scaled by 10).
     * @return The exact Euclidean distance as a double.
     * @throws ArithmeticException If the square root is not exact.
     */
    private static double exactDistance(int dxi, int dyi) {
        if (dxi == 0 && dyi == 0) {
            return 0.0;
        }

        long sumSq = (long) dxi * dxi + (long) dyi * dyi;
        long root  = Math.round(Math.sqrt(sumSq));

        for (long r = Math.max(0, root - 1); r <= root + 1; r++) {
            if (r * r == sumSq) {
                return r / 10.0;
            }
        }

        throw new ArithmeticException("No exact root — call hasExactOneDecimalDistance first.");
    }

    /**
     * Determines whether a candidate point can be added to the current set
     * of placed points while satisfying all constraints.
     * <p>
     * A candidate is considered valid if:
     * <ul>
     *   <li>All pairwise distances to already placed points can be represented
     *       exactly with one decimal place.</li>
     *   <li>None of these distances have been used before.</li>
     * </ul>
     * </p>
     *
     * @param cand      The candidate point (integer coordinates in tenths).
     * @param placed    The list of already placed points.
     * @param usedDists The set of distances that have already been used.
     * @return {@code true} if the candidate satisfies all constraints,
     *         {@code false} otherwise.
     */
    private static boolean isValidCandidate(int[] cand, List<int[]> placed, Set<Double> usedDists) {
        Set<Double> tentative = new HashSet<>();
        for (int[] p : placed) {
            int dxi = cand[0] - p[0];
            int dyi = cand[1] - p[1];

            if (!hasExactOneDecimalDistance(dxi, dyi)) {
                return false;
            }

            double dist = exactDistance(dxi, dyi);

            if (usedDists.contains(dist) || tentative.contains(dist)) {
                return false;
            }

            tentative.add(dist);
        }

        return true;
    }

    /**
     * Computes all pairwise distances between a candidate point and the
     * already placed points.
     *
     * @param cand   The candidate point (integer coordinates in tenths).
     * @param placed The list of already placed points.
     * @return A set containing all newly introduced distances.
     */
    private static Set<Double> newDistances(int[] cand, List<int[]> placed) {
        Set<Double> dists = new HashSet<>();

        for (int[] p : placed) {
            dists.add(exactDistance(cand[0] - p[0], cand[1] - p[1]));
        }

        return dists;
    }

    /**
     * Spring component responsible for injecting configuration values
     * into static fields for access.
     * <p>
     * Separate config for the coordinate generators as different
     * configuration of values may be favourable (e.g. enabling
     * fewer restarts for Euclidean coordinate generation because
     * it takes generally more time and therefore more runs are
     * not feasible compared to Manhattan coordinate generation)
     */
    @Component
    public static class Config {
        private static int maxRestarts;
        private static int maxAttemptsPerPoint;

        /**
         * Sets the {@linkplain #maxRestarts} value from the Spring property
         * {@code app.generation.coordinates.euclidean.restarts}.
         * <p>
         * This method is called by Spring automatically; do not call it manually.
         * </p>
         *
         * @param maxRestarts The maximum amount of restarts for trying coordinate generation.
         */
        @Value("${app.generation.coordinates.euclidean.restarts}")
        public void setMaxRestarts(int maxRestarts) {
            Config.maxRestarts = maxRestarts;
        }

        /**
         * Sets the {@linkplain #maxAttemptsPerPoint} value from the Spring property
         * {@code app.generation.coordinates.euclidean.max-attempts-per-point}.
         * <p>
         * This method is called by Spring automatically; do not call it manually.
         * </p>
         *
         * @param maxAttemptsPerPoint The maximum amount of retries for single points during generation.
         */
        @Value("${app.generation.coordinates.euclidean.max-attempts-per-point}")
        public void setMaxAttemptsPerPoint(int maxAttemptsPerPoint) {
            Config.maxAttemptsPerPoint = maxAttemptsPerPoint;
        }
    }
}

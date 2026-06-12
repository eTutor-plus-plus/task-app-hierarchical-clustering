package at.jku.dke.task_app.hierarchical_clustering.generators.matrix;

import at.jku.dke.task_app.hierarchical_clustering.data.entities.HierarchicalClusteringTask;
import at.jku.dke.task_app.hierarchical_clustering.generators.DataGenerator;
import at.jku.dke.task_app.hierarchical_clustering.validation.ValidMatrix;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * A generator that can generate a matrix of data points in form of a distance matrix.
 * Also handles calculation of distance matrices from a list of coordinates.
 */
public class DistanceMatrixGenerator implements DataGenerator<HierarchicalClusteringTask.DistanceMatrix> {

    @Override
	public HierarchicalClusteringTask.DistanceMatrix generate(int nDataPoints, Random random) {
		if (nDataPoints <= 0) {
            throw new IllegalArgumentException("n must be > 0");
        }

        BigDecimal step = Config.step;

        if (step.compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalArgumentException("Generation step must be set to a value > 0 in configuration");
        }

		// number of point pairs that need distances
		int nPairs = nDataPoints * (nDataPoints - 1) / 2;

		// build value pool with random expansion factor (to not have strictly ascending distances in the matrix, but more spread out distance values)
		double expansionFactor = 1 + random.nextDouble();
		int poolSize = (int) Math.ceil(nPairs * expansionFactor);
		List<Double> pool = new ArrayList<>(poolSize);
		for (int i = 1; i <= poolSize; i++) {
			pool.add(step.multiply(new BigDecimal(i)).doubleValue());
		}

		// shuffle pool (to randomize positions of values in the matrix)
		Collections.shuffle(pool, random);

		// fill matrix
        List<String> labels = new ArrayList<>(nDataPoints);
		BigDecimal[][] matrix = new BigDecimal[nDataPoints][nDataPoints];
		int idx = 0;

		for (int i = 0; i < nDataPoints; i++) {
            labels.add(String.valueOf(i + 1));
			matrix[i][i] = BigDecimal.ZERO;

			for (int j = i + 1; j < nDataPoints; j++) {
				matrix[i][j] = BigDecimal.valueOf(pool.get(idx));
				matrix[j][i] = BigDecimal.valueOf(pool.get(idx));
				idx++;
			}
		}

		return new HierarchicalClusteringTask.DistanceMatrix(labels, matrix);
	}

    /**
     * Creates a distance matrix from a given list of coordinates by calculating the distances
     * between the points in the coordinate list using the specified distance metric and filling
     * them into the matrix symmetrically.
     *
     * @param points The list of coordinates.
     * @param metric The metric to calculate distances with.
     * @return The resulting distance matrix.
     */
    @ValidMatrix
    public HierarchicalClusteringTask.DistanceMatrix calculateMatrixFromCoordinates(List<HierarchicalClusteringTask.CoordinatePoint> points, DistanceMetric metric) {
        int n = points.size();
        List<String> labels = new ArrayList<>();
        BigDecimal[][] matrix = new BigDecimal[n][n];

        for (int i = 0; i < n; i++) {
            labels.add(points.get(i).getLabel());
            matrix[i][i] = BigDecimal.ZERO;

            for (int j = i + 1; j < n; j++) {
                BigDecimal distance = metric.distance(points.get(i), points.get(j)).setScale(2, RoundingMode.HALF_UP).stripTrailingZeros();

                matrix[i][j] = distance;
                matrix[j][i] = distance;
            }
        }

        return new HierarchicalClusteringTask.DistanceMatrix(labels, matrix);
    }

    /**
     * Spring component responsible for injecting configuration values
     * into static fields for access.
     */
    @Component
    public static class Config {
        static BigDecimal step;

        /**
         * Sets the {@linkplain #step} value from the Spring property
         * {@code app.generation.matrix.step}.
         * <p>
         * This method is called by Spring automatically; do not call it manually.
         * </p>
         *
         * @param step the step size for matrix generation
         */
        @Value("${app.generation.matrix.step}")
        public void setStep(BigDecimal step) {
            Config.step = step;
        }
    }
}

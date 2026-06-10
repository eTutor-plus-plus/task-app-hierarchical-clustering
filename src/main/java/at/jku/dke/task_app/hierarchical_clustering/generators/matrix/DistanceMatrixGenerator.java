package at.jku.dke.task_app.hierarchical_clustering.generators.matrix;

import at.jku.dke.task_app.hierarchical_clustering.data.entities.HierarchicalClusteringTask;
import at.jku.dke.task_app.hierarchical_clustering.generators.Generator;
import at.jku.dke.task_app.hierarchical_clustering.validation.ValidMatrix;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class DistanceMatrixGenerator implements Generator<HierarchicalClusteringTask.DistanceMatrix> {

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
		double[][] matrix = new double[nDataPoints][nDataPoints];
		int idx = 0;

		for (int i = 0; i < nDataPoints; i++) {
            labels.add(String.valueOf(i + 1));
			matrix[i][i] = 0.0;

			for (int j = i + 1; j < nDataPoints; j++) {
				matrix[i][j] = pool.get(idx);
				matrix[j][i] = pool.get(idx);
				idx++;
			}
		}

		return new HierarchicalClusteringTask.DistanceMatrix(labels, matrix);
	}

    @ValidMatrix
    public HierarchicalClusteringTask.DistanceMatrix calculateMatrixFromCoordinates(List<HierarchicalClusteringTask.CoordinatePoint> points, DistanceMetric metric) {
        int n = points.size();
        List<String> labels = new ArrayList<>();
        double[][] matrix = new double[n][n];

        for (int i = 0; i < n; i++) {
            labels.add(points.get(i).getLabel());
            matrix[i][i] = 0.0;

            for (int j = i + 1; j < n; j++) {
                BigDecimal bd = BigDecimal.valueOf(metric.distance(points.get(i), points.get(j)));
                bd = bd.setScale(2, RoundingMode.HALF_UP);
                double d = bd.doubleValue();

                matrix[i][j] = d;
                matrix[j][i] = d;
            }
        }

        return new HierarchicalClusteringTask.DistanceMatrix(labels, matrix);
    }

    @Component
    public static class Config {
        static BigDecimal step;

        @Value("${app.generation.matrix.step}")
        public void setStep(BigDecimal step) {
            Config.step = step;
        }
    }
}

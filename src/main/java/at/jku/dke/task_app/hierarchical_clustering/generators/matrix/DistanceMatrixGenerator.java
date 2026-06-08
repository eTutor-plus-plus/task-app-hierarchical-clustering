package at.jku.dke.task_app.hierarchical_clustering.generators.matrix;

import at.jku.dke.task_app.hierarchical_clustering.data.entities.HierarchicalClusteringTask;
import at.jku.dke.task_app.hierarchical_clustering.generators.Generator;
import at.jku.dke.task_app.hierarchical_clustering.validation.ValidMatrix;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;

public class DistanceMatrixGenerator implements Generator<HierarchicalClusteringTask.DistanceMatrix> {

	public HierarchicalClusteringTask.DistanceMatrix generate(int nDataPoints, Random random) {
		if (nDataPoints <= 0) throw new IllegalArgumentException("n must be > 0");
        // following line only if step should be manually assignable (by teachers)
		// if (STEP <= 0) throw new IllegalArgumentException("step must be > 0");

		// number of point pairs that need distances
		int nPairs = nDataPoints * (nDataPoints - 1) / 2;

		// build value pool with random expansion factor (to not have strictly ascending distances in the matrix, but more spread out distance values)
		double expansionFactor = 1 + random.nextDouble();
		int poolSize = (int) Math.ceil(nPairs * expansionFactor);
		List<Double> pool = new ArrayList<>(poolSize);
		for (int i = 1; i <= poolSize; i++) {
			pool.add(i * Config.step);
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
                double d = Math.round(metric.distance(points.get(i), points.get(j)) * 10) / 10.0;

                matrix[i][j] = d;
                matrix[j][i] = d;
            }
        }

        return new HierarchicalClusteringTask.DistanceMatrix(labels, matrix);
    }

    @Component
    static class Config {
        static double step;

        @Value("${app.generation.matrix.step}")
        public void setStep(double step) {
            Config.step = step;
        }
    }
}

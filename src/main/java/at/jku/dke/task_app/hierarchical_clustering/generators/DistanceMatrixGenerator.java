package at.jku.dke.task_app.hierarchical_clustering.generators;

import at.jku.dke.task_app.hierarchical_clustering.data.entities.HierarchicalClusteringTask;

import java.nio.charset.StandardCharsets;
import java.util.*;

public class DistanceMatrixGenerator {

    private static final double STEP = 0.5;

	public static HierarchicalClusteringTask.DistanceMatrix getRandomMatrix(int nDataPoints) {
		if (nDataPoints <= 0) throw new IllegalArgumentException("n must be > 0");
        // following line only if step should be manually assignable (by teachers)
		// if (STEP <= 0) throw new IllegalArgumentException("step must be > 0");

		// number of point pairs that need distances
		int nPairs = nDataPoints * (nDataPoints - 1) / 2;

		// build value pool with random expansion factor (to not have strictly ascending distances in the matrix, but more spread out distance values)
		double expansionFactor = 1 + new Random().nextDouble();
		int poolSize = (int) Math.ceil(nPairs * expansionFactor);
		List<Double> pool = new ArrayList<>(poolSize);
		for (int i = 1; i <= poolSize; i++) {
			pool.add(i * STEP);
		}

		// shuffle pool (to randomize positions of values in the matrix)
		Collections.shuffle(pool, new Random());

		// fill matrix
        List<String> labels = new ArrayList<>(nDataPoints);
		double[][] matrix = new double[nDataPoints][nDataPoints];
		int idx = 0;

		for (int i = 0; i < nDataPoints; i++) {
			matrix[i][i] = 0.0;
            labels.add(String.valueOf(i + 1));

			for (int j = i + 1; j < nDataPoints; j++) {
				matrix[i][j] = pool.get(idx);
				matrix[j][i] = pool.get(idx);
				idx++;
			}
		}

		return new HierarchicalClusteringTask.DistanceMatrix(labels, matrix);
	}

    public static String getAsSvg(HierarchicalClusteringTask.DistanceMatrix matrix) {
        List<String> labels = matrix.getLabels();
        double[][] distances = matrix.getDistances();

        int n = labels.size();
        int cellSize = 50;

        // Minimal margins for labels
        int topMargin = 20;   // space for column labels
        int leftMargin = 60;  // space for row labels

        int svgWidth = n * cellSize + leftMargin;
        int svgHeight = n * cellSize + topMargin;

        StringBuilder svg = new StringBuilder();
        svg.append(String.format("<svg xmlns='http://www.w3.org/2000/svg' width='%d' height='%d'>", svgWidth, svgHeight));

        // Draw cells and numbers
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                int x = leftMargin + j * cellSize;
                int y = topMargin + i * cellSize;

                // Cell border
                svg.append(String.format(
                    "<rect x='%d' y='%d' width='%d' height='%d' fill='white' stroke='black' />",
                    x, y, cellSize, cellSize
                ));

                // Distance number centered
                String value = String.format("%.2f", distances[i][j]);
                svg.append(String.format(
                    "<text x='%d' y='%d' font-size='12' text-anchor='middle' alignment-baseline='middle'>%s</text>",
                    x + cellSize / 2, y + cellSize / 2, value
                ));
            }
        }

        // Column labels (top)
        for (int j = 0; j < n; j++) {
            int x = leftMargin + j * cellSize + cellSize / 2;
            int y = topMargin - 5; // slightly above first row
            svg.append(String.format(
                "<text x='%d' y='%d' font-size='12' text-anchor='middle'>%s</text>",
                x, y, labels.get(j)
            ));
        }

        // Row labels (left)
        for (int i = 0; i < n; i++) {
            int x = leftMargin - 5; // slightly left of first column
            int y = topMargin + i * cellSize + cellSize / 2;
            svg.append(String.format(
                "<text x='%d' y='%d' font-size='12' text-anchor='end' alignment-baseline='middle'>%s</text>",
                x, y, labels.get(i)
            ));
        }

        svg.append("</svg>");

        // Encode SVG as base64 for <img>
        String svgBase64 = Base64.getEncoder().encodeToString(svg.toString().getBytes(StandardCharsets.UTF_8));

        // Return <img> tag with minimal spacing
        return "<img src='data:image/svg+xml;base64," + svgBase64 + "' />";
    }
}

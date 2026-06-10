package at.jku.dke.task_app.hierarchical_clustering.clustering;

import at.jku.dke.task_app.hierarchical_clustering.data.entities.HierarchicalClusteringMerge;
import at.jku.dke.task_app.hierarchical_clustering.data.entities.HierarchicalClusteringTask;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class NaiveAgglomerativeClusteringAlgorithm implements HierarchicalClusteringAlgorithm {

    private final LinkageMethod linkage;

    public NaiveAgglomerativeClusteringAlgorithm(LinkageMethod linkage) {
        this.linkage = linkage;
    }

    @Override
    public List<HierarchicalClusteringMerge> cluster(HierarchicalClusteringTask.DistanceMatrix input) {
        int n = input.getLabels().size();
        BigDecimal[][] originalMatrix = input.getDistances();

        // Working distance matrix
        BigDecimal[][] workingMatrix = deepCopy(originalMatrix, n);

        // Track which original label indices each active cluster contains
        List<List<Integer>> clusters = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            List<Integer> singleton = new ArrayList<>();
            singleton.add(i);
            clusters.add(singleton);
        }

        List<SolutionFormatter.RawMerge> rawMerges = new ArrayList<>(n - 1);

        for (int step = 1; step < n; step++) {
            int size = clusters.size();

            checkForTiebreaker(workingMatrix);

            // 1. Find the closest pair of clusters
            BigDecimal minDist = BigDecimal.valueOf(Double.MAX_VALUE);
            int clusterA = -1, clusterB = -1;

            for (int i = 0; i < size; i++) {
                for (int j = i + 1; j < size; j++) {
                    if (workingMatrix[i][j].compareTo(minDist) < 0) {
                        minDist = workingMatrix[i][j];
                        clusterA  = i;
                        clusterB  = j;
                    }
                }
            }

            // 2. Record the raw merge
            List<String> leftLabels  = indicesToLabels(clusters.get(clusterA), input.getLabels());
            List<String> rightLabels = indicesToLabels(clusters.get(clusterB), input.getLabels());
            rawMerges.add(new SolutionFormatter.RawMerge(
                leftLabels, rightLabels, minDist, step));

            // 3. Merge cluster B into A (member indices)
            clusters.get(clusterA).addAll(clusters.get(clusterB));
            clusters.remove(clusterB);

            // 4. Rebuild the distance matrix for the reduced cluster set
            int newSize = clusters.size();
            BigDecimal[][] newDist = new BigDecimal[newSize][newSize];

            for (int i = 0; i < newSize; i++) {
                for (int j = i + 1; j < newSize; j++) {
                    BigDecimal d = linkage.distance(clusters.get(i), clusters.get(j), originalMatrix);
                    newDist[i][j] = d;
                    newDist[j][i] = d;
                }
            }

            workingMatrix = newDist;
        }

        return SolutionFormatter.format(rawMerges);
    }

    private BigDecimal[][] deepCopy(BigDecimal[][] sourceMatrix, int n) {
        BigDecimal[][] copy = new BigDecimal[n][n];

        for (int i = 0; i < n; i++) {
            copy[i] = sourceMatrix[i].clone();
        }

        return copy;
    }

    private List<String> indicesToLabels(List<Integer> indices, List<String> labels) {
        List<String> result = new ArrayList<>(indices.size());

        for (int idx : indices) {
            result.add(labels.get(idx));
        }

        return result;
    }

    private void checkForTiebreaker(BigDecimal[][] workingMatrix) {
        int size = workingMatrix.length;

        // Find minimum distance
        BigDecimal minDist = BigDecimal.valueOf(Double.MAX_VALUE);
        for (int i = 0; i < size; i++) {
            for (int j = i + 1; j < size; j++) {
                minDist = new BigDecimal(Double.toString(Math.min(minDist.doubleValue(), workingMatrix[i][j].doubleValue())));
            }
        }

        // Check whether any cluster appears twice among minimum-distance pairs
        boolean[] seen = new boolean[size];

        for (int i = 0; i < size; i++) {
            for (int j = i + 1; j < size; j++) {
                if (workingMatrix[i][j].compareTo(minDist) == 0) {
                    if (seen[i] || seen[j]) {
                        throw new IllegalStateException(
                            "Solution for current Matrix leads to Tiebreaker! Consider changing or re-arranging duplicate distances."
                        );
                    }
                    seen[i] = true;
                    seen[j] = true;
                }
            }
        }
    }
}

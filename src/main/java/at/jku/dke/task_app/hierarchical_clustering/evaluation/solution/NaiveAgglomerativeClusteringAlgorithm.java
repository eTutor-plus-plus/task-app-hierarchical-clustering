package at.jku.dke.task_app.hierarchical_clustering.evaluation.solution;

import at.jku.dke.task_app.hierarchical_clustering.data.entities.HierarchicalClusteringMerge;
import at.jku.dke.task_app.hierarchical_clustering.data.entities.HierarchicalClusteringTask;

import java.util.ArrayList;
import java.util.List;

/**
 * Generic O(n³) naive agglomerative hierarchical clustering.
 *
 * <p>The algorithm itself is linkage-agnostic: it maintains a live set of
 * clusters and, in each of the n−1 steps, finds the pair of clusters with the
 * smallest inter-cluster distance (as defined by the injected
 * {@link LinkageMethod}), merges them, and records the event.
 *
 * <p>All output formatting is deferred to {@link HierarchicalClusteringSolutionFormatter},
 * keeping the algorithm free of any presentation concerns.
 *
 * <h2>Extending with new linkage criteria</h2>
 * Implement {@link LinkageMethod} and pass it to the constructor — no other
 * changes required:
 * <pre>
 *   // Ward's method (illustrative; Ward needs squared distances + |A||B|/(|A|+|B|) weight)
 *   LinkageMethod ward = (a, b, dist) -> { ... };
 *   new NaiveAgglomerativeClusteringAlgorithm(ward).cluster(matrix);
 * </pre>
 *
 * <h2>Pre-built strategies</h2>
 * See {@link LinkageMethods} for SINGLE, COMPLETE, AVERAGE, and others.
 */
public class NaiveAgglomerativeClusteringAlgorithm implements HierarchicalClusteringAlgorithm {

    private final LinkageMethod linkage;

    public NaiveAgglomerativeClusteringAlgorithm(LinkageMethod linkage) {
        this.linkage = linkage;
    }

    public List<HierarchicalClusteringMerge> cluster(HierarchicalClusteringTask.DistanceMatrix input) {
        int n = input.getLabels().size();
        double[][] dist = input.getDistances();   // original matrix — strategies read from this

        // Each active cluster is a list of original label indices
        List<List<Integer>> clusters = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            List<Integer> singleton = new ArrayList<>();
            singleton.add(i);
            clusters.add(singleton);
        }

        List<HierarchicalClusteringSolutionFormatter.RawMerge> rawMerges = new ArrayList<>(n - 1);

        for (int step = 1; step < n; step++) {
            int size = clusters.size();

            // --- 1. Find the closest pair of clusters under the chosen linkage ---
            double minDist = Double.MAX_VALUE;
            int mergeA = -1, mergeB = -1;

            for (int a = 0; a < size; a++) {
                for (int b = a + 1; b < size; b++) {
                    double d = linkage.distance(clusters.get(a), clusters.get(b), dist);
                    if (d < minDist) {
                        minDist = d;
                        mergeA  = a;
                        mergeB  = b;
                    }
                }
            }

            // --- 2. Record the raw merge event (indices → labels deferred to formatter) ---
            List<String> leftLabels  = indicesToLabels(clusters.get(mergeA), input.getLabels());
            List<String> rightLabels = indicesToLabels(clusters.get(mergeB), input.getLabels());
            rawMerges.add(new HierarchicalClusteringSolutionFormatter.RawMerge(
                leftLabels, rightLabels, minDist, step));

            // --- 3. Merge: absorb B into A, then remove B ---
            clusters.get(mergeA).addAll(clusters.get(mergeB));
            clusters.remove(mergeB);
        }

        return HierarchicalClusteringSolutionFormatter.format(rawMerges);
    }

    private List<String> indicesToLabels(List<Integer> indices, List<String> labels) {
        List<String> result = new ArrayList<>(indices.size());
        for (int idx : indices) result.add(labels.get(idx));
        return result;
    }
}

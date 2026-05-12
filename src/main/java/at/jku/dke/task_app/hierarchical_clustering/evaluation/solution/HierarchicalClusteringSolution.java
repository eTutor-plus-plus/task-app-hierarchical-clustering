package at.jku.dke.task_app.hierarchical_clustering.evaluation.solution;

import at.jku.dke.task_app.hierarchical_clustering.data.entities.HierarchicalClusteringCluster;
import at.jku.dke.task_app.hierarchical_clustering.data.entities.HierarchicalClusteringMerge;

import java.util.List;

public class HierarchicalClusteringSolution {

    public static List<HierarchicalClusteringMerge> getSolution() {
        return testSolution();
    }

    public static List<HierarchicalClusteringMerge> testSolution() {
        HierarchicalClusteringMerge merge = new HierarchicalClusteringMerge();
        merge.setDistance(1.0);
        HierarchicalClusteringCluster cluster = new HierarchicalClusteringCluster();
        cluster.setDataPoints(List.of("3", "4"));
        merge.setResult(cluster);
        merge.setStep(1);
        return List.of(merge);
    }
}

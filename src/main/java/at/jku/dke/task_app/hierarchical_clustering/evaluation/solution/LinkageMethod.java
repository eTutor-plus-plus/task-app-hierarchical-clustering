package at.jku.dke.task_app.hierarchical_clustering.evaluation.solution;

import java.util.List;

@FunctionalInterface
public interface LinkageMethod {

    double distance(List<Integer> a, List<Integer> b, double[][] dist);

}

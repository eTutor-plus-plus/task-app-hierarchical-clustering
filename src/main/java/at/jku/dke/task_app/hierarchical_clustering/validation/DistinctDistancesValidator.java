package at.jku.dke.task_app.hierarchical_clustering.validation;

import at.jku.dke.task_app.hierarchical_clustering.data.entities.HierarchicalClusteringTask;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.HashSet;
import java.util.Set;

public class DistinctDistancesValidator implements ConstraintValidator<DistinctDistances, HierarchicalClusteringTask.DistanceMatrix> {

    public DistinctDistancesValidator() {}

    @Override
    public boolean isValid(HierarchicalClusteringTask.DistanceMatrix value, ConstraintValidatorContext context) {
        if (value != null) { // before task creation and persistence, matrix is always null
            double[][] distances = value.getDistances();
            Set<Double> usedDistances = new HashSet<>();

            for (int i = 0; i < distances.length; i++) {
                for (int j = 0; j < distances[0].length && i > j; j++) {
                    if (!usedDistances.add(distances[i][j])) {
                        return false;
                    }
                }
            }
        }

        return true;
    }
}

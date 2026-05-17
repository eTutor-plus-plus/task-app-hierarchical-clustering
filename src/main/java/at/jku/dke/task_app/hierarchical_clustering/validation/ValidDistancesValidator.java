package at.jku.dke.task_app.hierarchical_clustering.validation;

import at.jku.dke.task_app.hierarchical_clustering.data.entities.HierarchicalClusteringTask;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidDistancesValidator implements ConstraintValidator<ValidDistances, HierarchicalClusteringTask.DistanceMatrix> {

    public ValidDistancesValidator() {}

    @Override
    public boolean isValid(HierarchicalClusteringTask.DistanceMatrix value, ConstraintValidatorContext context) {
        if (value != null) { // before task creation and persistence, matrix is always null
            double[][] distances = value.getDistances();

            for (int i = 0; i < distances.length; i++) {
                for (int j = 0; j < distances[0].length && i > j; j++) {
                    if (distances[i][j] < 0.0) {
                        return false;
                    }
                }
            }
        }

        return true;
    }
}

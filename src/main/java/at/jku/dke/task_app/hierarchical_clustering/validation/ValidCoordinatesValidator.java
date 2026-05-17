package at.jku.dke.task_app.hierarchical_clustering.validation;

import at.jku.dke.task_app.hierarchical_clustering.data.entities.HierarchicalClusteringTask;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.List;

public class ValidCoordinatesValidator implements ConstraintValidator<ValidCoordinates, List<HierarchicalClusteringTask.CoordinatePoint>> {

    public ValidCoordinatesValidator() {}

    @Override
    public boolean isValid(List<HierarchicalClusteringTask.CoordinatePoint> value, ConstraintValidatorContext context) {
        if (value != null) { // before task creation and persistence, coordinate list is always null
            for (HierarchicalClusteringTask.CoordinatePoint point : value) {
                if (point.getX() < 0 || point.getY() < 0) {
                    return false;
                }
            }
        }

        return true;
    }
}

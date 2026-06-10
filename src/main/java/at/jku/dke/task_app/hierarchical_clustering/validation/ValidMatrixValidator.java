package at.jku.dke.task_app.hierarchical_clustering.validation;

import at.jku.dke.task_app.hierarchical_clustering.data.entities.HierarchicalClusteringTask;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;

import java.math.BigDecimal;
import java.util.*;

public class ValidMatrixValidator implements ConstraintValidator<ValidMatrix, HierarchicalClusteringTask.DistanceMatrix> {

    @Autowired
    private MessageSource messageSource;

    public ValidMatrixValidator() {}

    @Override
    public boolean isValid(HierarchicalClusteringTask.DistanceMatrix value, ConstraintValidatorContext context) {
        if (value == null || value.getDistances() == null || value.getLabels() == null) {
            return true;
        }

        double[][] distances = value.getDistances();

        // distance matrix needs to be of size n x n and contain enough labels (pure safety checks, this should not be possible through normal usage)
        for (int i = 0; i < distances.length; i++) {
            if (distances[i].length != distances.length) {
                putMessage(context, "dimension", distances.length, i);
                return false;
            }
        }

        if (distances.length != value.getLabels().size()) {
            putMessage(context, "labels", distances.length, value.getLabels().size());
            return false;
        }

        for (int i = 0; i < distances.length; i++) {
            // diagonal check
            if (distances[i][i] != 0) {
                putMessage(context, "diagonal", i);
                return false;
            }

            for (int j = 0; j < i; j++) { // only go through lower triangle as matrix is symmetric
                // symmetry check
                if (distances[i][j] != distances[j][i]) {
                    putMessage(context, "symmetry", List.of(value.getLabels().get(i), value.getLabels().get(j)));
                    return false;
                }

                // valid distances check (need only check lower triangle because symmetry has been established by the previous check)
                if (distances[i][j] <= 0.0) {
                    putMessage(context, "negatives", List.of(value.getLabels().get(i), value.getLabels().get(j)));
                    return false;
                }
            }
        }

        return true;
    }

    private void putMessage(ConstraintValidatorContext context, String messageKey, Object... messageParameters) {
        if (context != null) {
            HibernateConstraintValidatorContext hibernateContext = context.unwrap(HibernateConstraintValidatorContext.class);

            hibernateContext.disableDefaultConstraintViolation();

            String message = this.messageSource.getMessage("validation.distanceMatrix." + messageKey, messageParameters, Locale.getDefault());

            hibernateContext
                .addMessageParameter("0", message)
                .buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                .addConstraintViolation();
        }
    }
}

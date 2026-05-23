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
        if (value != null) { // before task creation and persistence, matrix is always null
            double[][] distances = value.getDistances();
            Set<Double> distinctDistances = new HashSet<>();

            for (int i = 0; i < distances.length; i++) {
                for (int j = 0; j < distances[0].length && i > j; j++) { // only go through lower triangle as matrix is symmetric

                    // symmetry check
                    if (distances[i][j] != distances[j][i] || distances[i][i] != 0.0) {
                        putMessage(context, "symmetry", new Object[]{List.of(value.getLabels().get(i), value.getLabels().get(j))});
                        return false;
                    }

                    // valid distances check (need only check lower triangle because symmetry has been confirmed in the previous check)
                    if (distances[i][j] <= 0.0) {
                        putMessage(context, "negatives", new Object[]{List.of(value.getLabels().get(i), value.getLabels().get(j))});
                        return false;
                    }

                    // distinct distances check (checks only lower triangle if distance is not 0 or negative)
                    if (!distinctDistances.add(distances[i][j])) {
                        putMessage(context, "duplicates", new Object[]{List.of(value.getLabels().get(i), value.getLabels().get(j))});
                        return false;
                    }
                }
            }
        }

        return true;
    }

    private void putMessage(ConstraintValidatorContext context, String messageKey, Object[] messageParameters) {
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

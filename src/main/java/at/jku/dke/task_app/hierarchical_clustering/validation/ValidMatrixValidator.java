package at.jku.dke.task_app.hierarchical_clustering.validation;

import at.jku.dke.task_app.hierarchical_clustering.data.entities.HierarchicalClusteringTask;
import at.jku.dke.task_app.hierarchical_clustering.dto.ModifyHierarchicalClusteringTaskDto;
import at.jku.dke.task_app.hierarchical_clustering.generators.matrix.DistanceMatrixGenerator;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;

import java.math.BigDecimal;
import java.util.*;

/**
 * Custom validator for distance matrices in {@link ModifyHierarchicalClusteringTaskDto}
 * or calculated by {@link DistanceMatrixGenerator}.
 * Validates whether a distance matrix is of size n x n, has 0-diagonal and symmetry
 * and does not contain negative distance values.
 */
public class ValidMatrixValidator implements ConstraintValidator<ValidMatrix, HierarchicalClusteringTask.DistanceMatrix> {

    @Autowired
    private MessageSource messageSource;

    /**
     * Creates a new instance of class {@linkplain ValidMatrixValidator}.
     */
    public ValidMatrixValidator() {}

    @Override
    public boolean isValid(HierarchicalClusteringTask.DistanceMatrix value, ConstraintValidatorContext context) {
        if (value == null || value.getDistances() == null || value.getLabels() == null) {
            return true;
        }

        BigDecimal[][] distances = value.getDistances();

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
            if (distances[i][i].compareTo(BigDecimal.ZERO) != 0) {
                putMessage(context, "diagonal", i);
                return false;
            }

            for (int j = 0; j < i; j++) { // only go through lower triangle as matrix is symmetric
                // symmetry check
                if (distances[i][j].compareTo(distances[j][i]) != 0) {
                    putMessage(context, "symmetry", List.of(value.getLabels().get(i), value.getLabels().get(j)));
                    return false;
                }

                // valid distances check (need only check lower triangle because symmetry has been established by the previous check)
                if (distances[i][j].compareTo(BigDecimal.ZERO) <= 0) {
                    putMessage(context, "negatives", List.of(value.getLabels().get(i), value.getLabels().get(j)));
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Helper method to insert a specified message into the default message for more exact error feedback.
     *
     * @param context           context in which the constraint is evaluated.
     * @param messageKey        the key/code of the message.
     * @param messageParameters optional parameters for the message.
     */
    private void putMessage(ConstraintValidatorContext context, String messageKey, Object... messageParameters) {
        if (context != null) {
            HibernateConstraintValidatorContext hibernateContext = context.unwrap(HibernateConstraintValidatorContext.class);

            hibernateContext.disableDefaultConstraintViolation();

            // can be switched to desired locale
            String message = this.messageSource.getMessage("validation.distanceMatrix." + messageKey, messageParameters, Locale.getDefault());

            hibernateContext
                .addMessageParameter("0", message)
                .buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                .addConstraintViolation();
        }
    }
}

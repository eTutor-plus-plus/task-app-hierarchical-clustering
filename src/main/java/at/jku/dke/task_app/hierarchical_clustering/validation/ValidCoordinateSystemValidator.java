package at.jku.dke.task_app.hierarchical_clustering.validation;

import at.jku.dke.task_app.hierarchical_clustering.dto.ModifyHierarchicalClusteringTaskDto;
import at.jku.dke.task_app.hierarchical_clustering.data.entities.HierarchicalClusteringTask;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;

import java.util.Locale;

/**
 * Custom validator for coordinate systems in {@link ModifyHierarchicalClusteringTaskDto}.
 * Validates whether a coordinate system's axes' minimum limits do not exceed the maximums.
 */
public class ValidCoordinateSystemValidator implements ConstraintValidator<ValidCoordinateSystem, HierarchicalClusteringTask.CoordinateSystem> {

    @Autowired
    private MessageSource messageSource;

    /**
     * Creates a new instance of class {@linkplain ValidCoordinateSystemValidator}.
     */
    public ValidCoordinateSystemValidator() {}

    @Override
    public boolean isValid(HierarchicalClusteringTask.CoordinateSystem value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        // check for axis minimum exceeding maximum
        if (value.getMaxX() <= value.getMinX() || value.getMaxY() <= value.getMinY()) {
            putMessage(context, "overflow");
            return false;
        }

        return true;
    }

    /**
     * Helper method to insert a specified message into the default message for more exact error feedback.
     *
     * @param context           context in which the constraint is evaluated.
     * @param messageKey        the key/code of the message for Spring's message source.
     * @param messageParameters optional parameters for the message.
     */
    private void putMessage(ConstraintValidatorContext context, String messageKey, Object... messageParameters) {
        if (context != null) {
            HibernateConstraintValidatorContext hibernateContext = context.unwrap(HibernateConstraintValidatorContext.class);

            hibernateContext.disableDefaultConstraintViolation();

            String message = this.messageSource.getMessage("validation.coordinateSystem." + messageKey, messageParameters, Locale.getDefault());

            hibernateContext
                .addMessageParameter("0", message)
                .buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                .addConstraintViolation();
        }
    }
}

package at.jku.dke.task_app.hierarchical_clustering.validation;

import at.jku.dke.task_app.hierarchical_clustering.data.entities.HierarchicalClusteringTask;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;

import java.util.List;
import java.util.Locale;

public class ValidCoordinateSystemValidator implements ConstraintValidator<ValidCoordinateSystem, HierarchicalClusteringTask.CoordinateSystem> {

    @Autowired
    private MessageSource messageSource;

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

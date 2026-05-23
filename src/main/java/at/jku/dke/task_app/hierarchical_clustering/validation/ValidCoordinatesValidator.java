package at.jku.dke.task_app.hierarchical_clustering.validation;

import at.jku.dke.task_app.hierarchical_clustering.data.entities.HierarchicalClusteringTask;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

public class ValidCoordinatesValidator implements ConstraintValidator<ValidCoordinates, List<HierarchicalClusteringTask.CoordinatePoint>> {

    @Autowired
    private MessageSource messageSource;

    public ValidCoordinatesValidator() {}

    @Override
    public boolean isValid(List<HierarchicalClusteringTask.CoordinatePoint> value, ConstraintValidatorContext context) {
        if (value != null) { // before task creation and persistence, coordinate list is always null
            for (HierarchicalClusteringTask.CoordinatePoint point : value) {
                // check for negative points
                if (point.getX() < 0 || point.getY() < 0) {
                    putMessage(context, "negatives", new Object[]{point.getX(), point.getY()});
                    return false;
                }
            }
        }

        return true;
    }

    private void putMessage(ConstraintValidatorContext context, String messageKey, Object[] messageParameters) {
        if (context != null) {
            HibernateConstraintValidatorContext hibernateContext = context.unwrap(HibernateConstraintValidatorContext.class);

            hibernateContext.disableDefaultConstraintViolation();

            String message = this.messageSource.getMessage("validation.coordinateList." + messageKey, messageParameters, Locale.getDefault());

            hibernateContext
                .addMessageParameter("0", message)
                .buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                .addConstraintViolation();
        }
    }
}

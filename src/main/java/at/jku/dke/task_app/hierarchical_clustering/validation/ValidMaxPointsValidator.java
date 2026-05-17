package at.jku.dke.task_app.hierarchical_clustering.validation;

import at.jku.dke.task_app.hierarchical_clustering.dto.ModifyHierarchicalClusteringTaskDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Valid;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;

public class ValidMaxPointsValidator implements ConstraintValidator<ValidMaxPoints, ValidMaxPointsValidator.MaxPointsValidationDto> {

    public ValidMaxPointsValidator() {}

    @Override
    public boolean isValid(MaxPointsValidationDto value, ConstraintValidatorContext context) {
        int nSolutionSteps = value.additionalData().nDataPoints() - 1;
        BigDecimal expectedMaxPoints = value.additionalData().pointsPerCorrectCluster().multiply(BigDecimal.valueOf(nSolutionSteps));
        BigDecimal actualMaxPoints = value.maxPoints();

        if (expectedMaxPoints.compareTo(actualMaxPoints) != 0) {
            if (context != null) {
                HibernateConstraintValidatorContext hibernateContext =
                    context.unwrap(HibernateConstraintValidatorContext.class);

                hibernateContext.disableDefaultConstraintViolation();

                hibernateContext
                    .addMessageParameter("expected", expectedMaxPoints)
                    .addMessageParameter("actual", actualMaxPoints)
                    .buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                    .addConstraintViolation();
            }

            return false;
        }

        return true;
    }

    @ValidMaxPoints
    public record MaxPointsValidationDto(BigDecimal maxPoints, ModifyHierarchicalClusteringTaskDto additionalData) { }

    @Component
    @Validated
    public static class Invoker {
        // proxy method for validating max points of task modification
        public void validate(@Valid ValidMaxPointsValidator.MaxPointsValidationDto dto) {}
    }
}

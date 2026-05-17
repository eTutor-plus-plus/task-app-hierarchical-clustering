package at.jku.dke.task_app.hierarchical_clustering.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {DistinctDistancesValidator.class})
public @interface DistinctDistances {
    String message() default "{validation.distanceMatrix.duplicates}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

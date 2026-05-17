package at.jku.dke.task_app.hierarchical_clustering.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {ValidDistancesValidator.class})
public @interface ValidDistances {
    String message() default "{validation.distanceMatrix.values}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

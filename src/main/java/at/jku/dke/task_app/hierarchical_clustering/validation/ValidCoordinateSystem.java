package at.jku.dke.task_app.hierarchical_clustering.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * The annotated elements must be a valid coordinate system
 * so that the minimum axis limit does not exceed the maximum limit.
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {ValidCoordinateSystemValidator.class})
public @interface ValidCoordinateSystem {
    String message() default "{validation.coordinateSystem}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

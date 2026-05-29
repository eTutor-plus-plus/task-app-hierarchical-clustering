package at.jku.dke.task_app.hierarchical_clustering.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {ValidCoordinateSystemValidator.class})
public @interface ValidCoordinateSystem {
    String message() default "{validation.coordinateSystem}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

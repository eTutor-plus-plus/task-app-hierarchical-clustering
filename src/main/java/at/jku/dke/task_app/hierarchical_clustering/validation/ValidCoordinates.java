package at.jku.dke.task_app.hierarchical_clustering.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {ValidCoordinatesValidator.class})
public @interface ValidCoordinates {
    String message() default "{validation.coordinateList}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

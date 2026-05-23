package at.jku.dke.task_app.hierarchical_clustering.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {ValidMatrixValidator.class})
public @interface ValidMatrix {
    String message() default "{validation.distanceMatrix}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

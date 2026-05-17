package at.jku.dke.task_app.hierarchical_clustering.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {ValidMaxPointsValidator.class})
public @interface ValidMaxPoints {
    String message() default "{validation.maxPoints}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

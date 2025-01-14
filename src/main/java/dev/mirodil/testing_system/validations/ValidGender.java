package dev.mirodil.testing_system.validations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = GenderValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidGender {
    String message() default "Invalid gender. Must be 'male' or 'female'.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

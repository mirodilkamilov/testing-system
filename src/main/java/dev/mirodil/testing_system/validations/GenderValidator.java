package dev.mirodil.testing_system.validations;

import dev.mirodil.testing_system.models.UserGender;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class GenderValidator implements ConstraintValidator<ValidGender, UserGender> {

    @Override
    public void initialize(ValidGender constraintAnnotation) {
    }

    @Override
    public boolean isValid(UserGender gender, ConstraintValidatorContext context) {
        if (gender == null) {
            return false;
        }
        return gender == UserGender.MALE || gender == UserGender.FEMALE;
    }
}

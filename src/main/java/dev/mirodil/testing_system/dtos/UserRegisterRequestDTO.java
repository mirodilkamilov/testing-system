package dev.mirodil.testing_system.dtos;

import dev.mirodil.testing_system.models.UserGender;
import dev.mirodil.testing_system.validations.UniqueEmail;
import dev.mirodil.testing_system.validations.ValidGender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UserRegisterRequestDTO(
        @Email @Size(min = 5, max = 50) @UniqueEmail
        String email,
        @NotBlank @Size(min = 5, max = 50)
        String password,
        @NotBlank @Size(max = 50)
        String fname,
        @NotBlank @Size(max = 50)
        String lname,
        @NotNull @ValidGender
        UserGender gender
) {
}

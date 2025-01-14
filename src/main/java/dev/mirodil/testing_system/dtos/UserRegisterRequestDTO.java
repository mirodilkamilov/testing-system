package dev.mirodil.testing_system.dtos;

import dev.mirodil.testing_system.models.UserGender;
import dev.mirodil.testing_system.validations.UniqueEmail;
import dev.mirodil.testing_system.validations.ValidGender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class UserRegisterRequestDTO {
    @Email
    @Size(min = 5, max = 50)
    @UniqueEmail
    private final String email;
    @NotBlank
    @Size(min = 5, max = 50)
    private final String password;
    @NotBlank
    @Size(max = 50)
    private final String fname;
    @NotBlank
    @Size(max = 50)
    private final String lname;
    @NotNull
    @ValidGender
    private final String gender;

    public UserRegisterRequestDTO(String email, String password, String fname, String lname, String gender) {
        this.email = email;
        this.password = password;
        this.fname = fname;
        this.lname = lname;
        this.gender = gender;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getFirstName() {
        return fname;
    }

    public String getLastName() {
        return lname;
    }

    public UserGender getGender() {
        return UserGender.valueOf(gender.toUpperCase());
    }
}

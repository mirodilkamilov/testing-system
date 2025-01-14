package dev.mirodil.testing_system.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UserLoginRequestDTO {
    @Email
    @Size(min = 5, max = 50)
    private final String email;
    @NotBlank
    @Size(min = 5, max = 50)
    private final String password;


    public UserLoginRequestDTO(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
}

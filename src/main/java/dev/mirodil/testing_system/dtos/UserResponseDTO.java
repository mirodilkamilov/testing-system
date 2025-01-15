package dev.mirodil.testing_system.dtos;

import dev.mirodil.testing_system.models.User;
import dev.mirodil.testing_system.models.UserGender;
import dev.mirodil.testing_system.models.UserRole;
import dev.mirodil.testing_system.models.UserStatus;

import java.net.URI;
import java.util.Date;


public record UserResponseDTO(
        Long userId,
        UserRole userRole,
        String email,
        UserGender gender,
        UserStatus status,
        String fname,
        String lname,
        Boolean isAccountNonLocked,
        Date createdAt,
        URI path
) {
    public UserResponseDTO(User user) {
        this(
                user.getId(),
                user.getUserRole(),
                user.getEmail(),
                user.getGender(),
                user.getStatus(),
                user.getFirstName(),
                user.getLastName(),
                user.isAccountNonLocked(),
                user.getCreatedAt(),
                URI.create("/api/users/" + user.getId())
        );
    }
}

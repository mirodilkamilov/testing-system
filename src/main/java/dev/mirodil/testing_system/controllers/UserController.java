package dev.mirodil.testing_system.controllers;

import dev.mirodil.testing_system.dtos.UserResponseDTO;
import dev.mirodil.testing_system.exceptions.ResourceNotFoundException;
import dev.mirodil.testing_system.models.UserRole;
import dev.mirodil.testing_system.services.UserService;
import dev.mirodil.testing_system.utils.AuthUtil;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    // TODO: Implement proper authorization
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public Page<UserResponseDTO> getAllUsers(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(5) @Max(25) int size) {
        return userService.getUsers(page, size);
    }

    @GetMapping("/{id}")
    public UserResponseDTO getUserById(@PathVariable Long id) {
        Optional<UserResponseDTO> userOptionalDTO = AuthUtil.getAuthenticatedUser();
        if (userOptionalDTO.isEmpty()) {
            throw new ResourceNotFoundException();
        }

        UserResponseDTO currentUser = userOptionalDTO.get();
        if (!currentUser.userId().equals(id) || currentUser.userRole() == UserRole.ADMIN) {
            throw new ResourceNotFoundException();
        }

        return currentUser;
    }
}

/*
 TODO:
 1. Pagination and content wrapping json response
 2. Authorization
 3. Tests model
 4. TestEvents model
 */

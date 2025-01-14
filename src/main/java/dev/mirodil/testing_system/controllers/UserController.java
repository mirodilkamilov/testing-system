package dev.mirodil.testing_system.controllers;

import dev.mirodil.testing_system.dtos.UserResponseDTO;
import dev.mirodil.testing_system.exceptions.ResourceNotFoundException;
import dev.mirodil.testing_system.models.UserRole;
import dev.mirodil.testing_system.services.UserService;
import dev.mirodil.testing_system.utils.AuthUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
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
    public List<UserResponseDTO> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    public UserResponseDTO getUserById(@PathVariable Long id) {
        Optional<UserResponseDTO> userOptionalDTO = AuthUtil.getAuthenticatedUser();
        if (userOptionalDTO.isEmpty()) {
            throw new ResourceNotFoundException();
        }

        UserResponseDTO currentUser = userOptionalDTO.get();
        if (!currentUser.getUserId().equals(id) || currentUser.getUserRole() == UserRole.ADMIN) {
            throw new ResourceNotFoundException();
        }

        return currentUser;
    }
}

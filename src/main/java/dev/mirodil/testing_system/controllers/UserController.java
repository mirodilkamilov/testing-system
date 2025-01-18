package dev.mirodil.testing_system.controllers;

import dev.mirodil.testing_system.dtos.UserResponseDTO;
import dev.mirodil.testing_system.exceptions.ResourceNotFoundException;
import dev.mirodil.testing_system.models.User;
import dev.mirodil.testing_system.models.UserRole;
import dev.mirodil.testing_system.services.UserService;
import dev.mirodil.testing_system.utils.AuthUtil;
import dev.mirodil.testing_system.utils.PageWithFilterRequest;
import dev.mirodil.testing_system.utils.ValidationUtil;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public Page<UserResponseDTO> getAllUsers(PageWithFilterRequest pageable) {
        ValidationUtil.forceValidPageable(
                pageable,
                User.getAllowedSortAttributes(),
                User.getAllowedFilterAttributes()
        );
        return userService.getUsers(pageable);
    }

    @GetMapping("/{id}")
    public WrapResponseWithContentKey<?> getUserById(@PathVariable Long id) {
        Optional<UserResponseDTO> userOptionalDTO = AuthUtil.getAuthenticatedUser();
        if (userOptionalDTO.isEmpty()) {
            throw new ResourceNotFoundException();
        }

        UserResponseDTO currentUser = userOptionalDTO.get();
        if (!currentUser.userId().equals(id) || currentUser.userRole() == UserRole.ADMIN) {
            throw new ResourceNotFoundException();
        }

        return new WrapResponseWithContentKey<>(currentUser);
    }
}

/*
 TODO:
 2. Authorization
 3. Tests model
 4. TestEvents model
 */

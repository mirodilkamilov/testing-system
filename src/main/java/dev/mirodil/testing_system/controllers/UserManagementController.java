package dev.mirodil.testing_system.controllers;

import dev.mirodil.testing_system.dtos.UserCreateRequestDTO;
import dev.mirodil.testing_system.dtos.UserResponseDTO;
import dev.mirodil.testing_system.models.User;
import dev.mirodil.testing_system.services.UserService;
import dev.mirodil.testing_system.utils.PageWithFilterRequest;
import dev.mirodil.testing_system.utils.ValidationUtil;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
public class UserManagementController {
    private final UserService userService;

    public UserManagementController(UserService userService) {
        this.userService = userService;
    }

    @PreAuthorize("hasAuthority('VIEW_PROFILE')")
    @GetMapping("/profile")
    public WrapResponseWithContentKey<?> getProfile() {
        UserResponseDTO currentUserDTO = userService.getProfile();
        return new WrapResponseWithContentKey<>(currentUserDTO);
    }

    @PreAuthorize("hasAuthority('MANAGE_ALL_USERS')")
    @GetMapping("/users")
    public Page<UserResponseDTO> getAllUsers(PageWithFilterRequest pageable) {
        ValidationUtil.forceValidPageable(
                pageable,
                User.getAllowedSortAttributes(),
                User.getAllowedFilterAttributes()
        );
        return userService.getUsers(pageable);
    }

    @PreAuthorize("hasAuthority('MANAGE_ALL_USERS')")
    @GetMapping("/users/{id}")
    public WrapResponseWithContentKey<?> getUserById(@PathVariable Long id) {
        UserResponseDTO userDTO = userService.getUserById(id);
        return new WrapResponseWithContentKey<>(userDTO);
    }

    @PreAuthorize("hasAuthority('MANAGE_ALL_USERS')")
    @PostMapping("/users")
    public ResponseEntity<?> createUser(@Valid @RequestBody UserCreateRequestDTO request) {
        UserResponseDTO userDTO = userService.createUser(request);
        return ResponseEntity.created(userDTO.path()).body(
                new WrapResponseWithContentKey<>(userDTO)
        );
    }
}

/*
 TODO:
 3. Tests model
 4. TestEvents model
 */

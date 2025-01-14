package dev.mirodil.testing_system.controllers;

import dev.mirodil.testing_system.dtos.AuthResponseDTO;
import dev.mirodil.testing_system.dtos.UserLoginRequestDTO;
import dev.mirodil.testing_system.dtos.UserRegisterRequestDTO;
import dev.mirodil.testing_system.dtos.UserResponseDTO;
import dev.mirodil.testing_system.responses.GenericErrorResponse;
import dev.mirodil.testing_system.services.UserService;
import dev.mirodil.testing_system.utils.AuthUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody UserLoginRequestDTO request, HttpServletRequest servletRequest) {
        Optional<UserResponseDTO> userOptionalDTO = userService.getUserByEmail(request.getEmail());
        ResponseEntity<Map<String, Object>> wrongCredentialsErrorResponse = GenericErrorResponse.returnResponse(
                "Email or password is incorrect",
                HttpStatus.BAD_REQUEST,
                "/api/auth/login"
        );

        if (userOptionalDTO.isEmpty()) {
            return wrongCredentialsErrorResponse;
        }

        UserResponseDTO userDTO = userOptionalDTO.get();
        if (!AuthUtil.isPasswordMatches(request.getPassword(), userDTO.getPassword())) {
            return wrongCredentialsErrorResponse;
        }

        if (!userDTO.isAccountNonLocked()) {
            return GenericErrorResponse.returnResponse(
                    "Your account is locked. Please contact support.",
                    HttpStatus.FORBIDDEN,
                    "/api/auth/login"
            );
        }

        Map<String, Object> jwtTokenDetails = AuthUtil.generateTokenDetails(userDTO.getEmail());
        AuthUtil.setAuthenticationToSecurityContext(userDTO, servletRequest);
        return ResponseEntity.ok(
                new AuthResponseDTO(userDTO, jwtTokenDetails)
        );
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@Valid @RequestBody UserRegisterRequestDTO request) {
        UserResponseDTO userDTO = userService.createUser(request);
        Map<String, Object> jwtTokenDetails = AuthUtil.generateTokenDetails(userDTO.getEmail());
        return ResponseEntity.created(userDTO.getPath()).body(
                new AuthResponseDTO(userDTO, jwtTokenDetails)
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(HttpServletRequest request) {
        SecurityContextHolder.clearContext();

        AuthUtil.blacklistToken(AuthUtil.extractTokenFromRequest(request));

        return ResponseEntity.ok(Collections.singletonMap("message", "Successfully logged out"));
    }
}

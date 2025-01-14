package dev.mirodil.testing_system.configs;

import dev.mirodil.testing_system.dtos.UserResponseDTO;
import dev.mirodil.testing_system.exceptions.InvalidTokenException;
import dev.mirodil.testing_system.exceptions.ResourceNotFoundException;
import dev.mirodil.testing_system.exceptions.UserAccountBlocked;
import dev.mirodil.testing_system.services.UserService;
import dev.mirodil.testing_system.utils.AuthUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final UserService userService;

    public JwtAuthenticationFilter(UserService userService) {
        this.userService = userService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        SecurityContext context = SecurityContextHolder.getContext();

        String token = authHeader.substring(7);
        //=== Exceptions are handled in separate ExceptionHandlerFilter ===//
        String username = AuthUtil.extractUsername(token);

        UserResponseDTO userDTO = userService.getUserByEmail(username).orElseThrow(ResourceNotFoundException::new);
        if (AuthUtil.isTokenExpired(token)) {
            throw new InvalidTokenException("Token expired");
//            Alternative way without throwing exceptions
//            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Token expired");
//            return;
        }

        if (AuthUtil.isUserAuthenticated()) {
            if (!userDTO.isAccountNonLocked()) {
                throw new UserAccountBlocked();
            }
            filterChain.doFilter(request, response);
            return;
        }

        // Set the authentication in the SecurityContext
        UsernamePasswordAuthenticationToken authenticationToken =
                UsernamePasswordAuthenticationToken.authenticated(
                        userDTO,
                        null,
                        userDTO.getGrantedAuthorities()
                );
        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        // Continue with the filter chain
        filterChain.doFilter(request, response);
    }
}
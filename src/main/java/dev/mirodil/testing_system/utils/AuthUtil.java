package dev.mirodil.testing_system.utils;

import dev.mirodil.testing_system.configs.SecurityConfig;
import dev.mirodil.testing_system.dtos.UserResponseDTO;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

public class AuthUtil {
    private static final PasswordEncoder passwordEncoder = SecurityConfig.getPasswordEncoder();
    private static final String secretKeyPlain = System.getenv("SECRET_KEY");
    private static final SecretKey key = Keys.hmacShaKeyFor(secretKeyPlain.getBytes());
    private static final int oneDayInMillis = 24 * 60 * 60 * 1000;

    public static boolean isUserAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null &&
                authentication.isAuthenticated() &&
                !(authentication instanceof AnonymousAuthenticationToken);
    }

    public static Optional<UserResponseDTO> getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            UserResponseDTO userDTO = (UserResponseDTO) authentication.getPrincipal();
            return Optional.of(userDTO);
        }
        return Optional.empty();
    }

    private static Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public static String extractUsername(String token) {
        return getClaims(token).getSubject();
    }

    public static boolean isTokenExpired(String token) {
        Claims claims = getClaims(token);
        return claims.getExpiration().before(new Date());
    }

    public static Map<String, Object> generateTokenDetails(String subject) {
        String jwtToken = generateToken(subject);
        Date expiresAt = getTokenExpiresAt(jwtToken);

        return Map.of(
                "access", jwtToken,
                "issuedAt", new Date(System.currentTimeMillis()),
                "expiresAt", expiresAt,
                "type", "Bearer"
        );
    }

    public static Date getTokenExpiresAt(String token) {
        Claims claims = getClaims(token);
        return claims.getExpiration();
    }

    public static String generateToken(String subject) {
        Date iat = new Date(System.currentTimeMillis());
        Date exp = new Date(iat.getTime() + oneDayInMillis);
        return generateToken(subject, exp);
    }

    public static String generateToken(String subject, Date expDate) {
        Date iat = new Date(System.currentTimeMillis());
        if (expDate.before(iat)) {
            throw new IllegalArgumentException("JWT token's expiration date cannot be before it's issued date");
        }
        return Jwts
                .builder()
                .subject(subject)
                .issuedAt(iat)
                .expiration(expDate)
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    public static String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    public static boolean isPasswordMatches(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
}

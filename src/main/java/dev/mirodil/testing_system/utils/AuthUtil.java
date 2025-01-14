package dev.mirodil.testing_system.utils;

import dev.mirodil.testing_system.configs.SecurityConfig;
import dev.mirodil.testing_system.dtos.UserResponseDTO;
import dev.mirodil.testing_system.exceptions.InvalidTokenException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class AuthUtil {
    public static final long ONE_DAY_IN_MILLIS = 24 * 60 * 60 * 1000;
    private static final PasswordEncoder passwordEncoder = SecurityConfig.getPasswordEncoder();
    private static final String secretKeyPlain = System.getenv("SECRET_KEY");
    private static final SecretKey key = Keys.hmacShaKeyFor(secretKeyPlain.getBytes());
    private static StringRedisTemplate redisTemplate;

    private AuthUtil() {
    }

    public static void setRedisTemplate(StringRedisTemplate redisTemplate) {
        AuthUtil.redisTemplate = redisTemplate;
    }

    public static void blacklistToken(String token) {
        redisTemplate.opsForValue().set(
                token,
                "blackListed",
                ONE_DAY_IN_MILLIS,
                TimeUnit.MILLISECONDS
        );
    }

    public static boolean isTokenBlacklisted(String token) {
        return redisTemplate.hasKey(token);
    }

    public static void checkTokenBlacklisted(String token) throws InvalidTokenException {
        if (isTokenBlacklisted(token)) {
            throw new InvalidTokenException();
        }
    }

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

    public static String extractTokenFromRequest(HttpServletRequest request) throws InvalidTokenException {
        final String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new InvalidTokenException("Authorization header is missing or invalid", HttpStatus.BAD_REQUEST);
        }

        return authHeader.substring(7);
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
        Date exp = new Date(iat.getTime() + ONE_DAY_IN_MILLIS);
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

    public static void setAuthenticationToSecurityContext(UserResponseDTO userDTO, HttpServletRequest request) {
        UsernamePasswordAuthenticationToken authenticationToken =
                UsernamePasswordAuthenticationToken.authenticated(
                        userDTO,
                        null,
                        userDTO.getGrantedAuthorities()
                );
        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    }
}

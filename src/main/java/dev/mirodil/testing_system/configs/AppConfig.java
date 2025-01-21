package dev.mirodil.testing_system.configs;

import dev.mirodil.testing_system.repositories.RoleWithPermissionResultSetExtractor;
import dev.mirodil.testing_system.repositories.UserWithPermissionResultSetExtractor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
public class AppConfig {
    @Bean
    public StringRedisTemplate getStringRedisTemplate(LettuceConnectionFactory lettuceConnectionFactory) {
        return new StringRedisTemplate(lettuceConnectionFactory);
    }

    @Bean
    public UserWithPermissionResultSetExtractor userWithPermissionResultSetExtractor() {
        return new UserWithPermissionResultSetExtractor();
    }

    @Bean
    public RoleWithPermissionResultSetExtractor roleWithPermissionResultSetExtractor() {
        return new RoleWithPermissionResultSetExtractor();
    }
}

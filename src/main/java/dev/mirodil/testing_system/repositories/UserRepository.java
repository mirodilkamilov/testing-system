package dev.mirodil.testing_system.repositories;

import dev.mirodil.testing_system.models.User;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends CrudRepository<User, Long>, CustomUserRepository {
    Optional<User> findUserByEmail(String email);

    Optional<User> findUserById(Long id);

    @Query("SELECT * FROM users LIMIT :limit OFFSET :offset")
    List<User> findUsersWithPagination(long limit, long offset);

    @Query("SELECT COUNT(*) FROM users")
    long countUsersById();
}

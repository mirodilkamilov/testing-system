package dev.mirodil.testing_system.repositories;

import dev.mirodil.testing_system.models.User;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CustomUserRepository {
    List<User> findAndSortUsersWithPagination(Pageable pageable);
}
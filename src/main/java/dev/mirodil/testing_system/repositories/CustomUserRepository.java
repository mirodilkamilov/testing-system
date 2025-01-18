package dev.mirodil.testing_system.repositories;

import dev.mirodil.testing_system.models.User;
import dev.mirodil.testing_system.services.PageWithFilterRequest;

import java.util.List;

public interface CustomUserRepository {
    List<User> findAndSortUsersWithPagination(PageWithFilterRequest pageable);
}
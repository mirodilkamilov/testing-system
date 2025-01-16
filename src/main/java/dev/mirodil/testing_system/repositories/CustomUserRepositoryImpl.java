package dev.mirodil.testing_system.repositories;

import dev.mirodil.testing_system.models.User;
import dev.mirodil.testing_system.utils.DataUtil;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CustomUserRepositoryImpl implements CustomUserRepository {
    private final JdbcTemplate namedJdbcTemplate;

    public CustomUserRepositoryImpl(JdbcTemplate namedJdbcTemplate) {
        this.namedJdbcTemplate = namedJdbcTemplate;
    }

    @Override
    public List<User> findAndSortUsersWithPagination(Pageable pageable) {
        Sort sort = pageable.getSort();
        String baseQuery = "SELECT * FROM users";
        String orderByClause = "";

        if (sort.isSorted()) {
            orderByClause = DataUtil.convertSortOrdersToOrderByClause(sort);
        }
        String finalQuery = baseQuery + orderByClause + " LIMIT ? OFFSET ?";

        return namedJdbcTemplate.query(
                finalQuery,
                new UserRowMapper(),
                pageable.getPageSize(),
                pageable.getOffset()
        );
    }
}
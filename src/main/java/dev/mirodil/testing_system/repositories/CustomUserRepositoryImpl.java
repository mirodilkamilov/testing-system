package dev.mirodil.testing_system.repositories;

import dev.mirodil.testing_system.models.User;
import dev.mirodil.testing_system.utils.PageWithFilterRequest;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static dev.mirodil.testing_system.utils.DataUtil.appendOrderByClause;
import static dev.mirodil.testing_system.utils.DataUtil.appendWhereClause;

@Repository
public class CustomUserRepositoryImpl implements CustomUserRepository {
    private final JdbcTemplate jdbcTemplate;

    public CustomUserRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<User> findAndSortUsersWithPagination(PageWithFilterRequest pageable) {
        // Base query
        StringBuilder queryBuilder = new StringBuilder("SELECT * FROM users");

        // Add filters dynamically
        Map<String, String> filters = pageable.getFilters();
        List<Object> queryParams = new ArrayList<>(
                appendWhereClause(queryBuilder, filters)
        );

        // Add sorting dynamically
        Sort sort = pageable.getSort();
        appendOrderByClause(queryBuilder, sort);

        // Add pagination
        queryBuilder.append(" LIMIT ? OFFSET ?");
        queryParams.add(pageable.getPageSize());
        queryParams.add(pageable.getOffset());

        // Execute the query
        return jdbcTemplate.query(
                queryBuilder.toString(),
                new UserRowMapper(),
                queryParams.toArray()
        );
    }
}
package dev.mirodil.testing_system.utils;

import dev.mirodil.testing_system.models.Permission;
import dev.mirodil.testing_system.models.Role;
import dev.mirodil.testing_system.models.User;
import dev.mirodil.testing_system.models.enums.PermissionType;
import dev.mirodil.testing_system.models.enums.UserGender;
import dev.mirodil.testing_system.models.enums.UserRole;
import dev.mirodil.testing_system.models.enums.UserStatus;
import org.springframework.data.domain.Sort;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DataUtil {
    public static void appendOrderByClause(StringBuilder queryBuilder, Sort sort) {
        if (sort.isEmpty()) {
            return;
        }

        queryBuilder.append(" ORDER BY ");
        queryBuilder.append(sort.stream()
                .map(order -> convertToSnakeCase(order.getProperty()) + " " + order.getDirection().name())
                .collect(Collectors.joining(", ")));
    }

    public static List<String> appendWhereClause(StringBuilder queryBuilder, Map<String, String> filters) {
        List<String> filterValues = new ArrayList<>();
        if (!filters.isEmpty()) {
            queryBuilder.append(" WHERE ");
            queryBuilder.append(filters.entrySet().stream()
                    .map(entry -> {
                        filterValues.add("%" + entry.getValue() + "%"); // Add the filter value as a parameter
                        return convertToSnakeCase(entry.getKey()) + " LIKE ?";
                    })
                    .collect(Collectors.joining(" AND ")));
        }

        return filterValues;
    }

    public static User extractUserFromResultSet(ResultSet rs) throws SQLException {
        User user = new User(
                rs.getString("email"),
                rs.getString("password"),
                rs.getLong("role_id"),
                rs.getString("fname"),
                rs.getString("lname"),
                UserGender.valueOf(rs.getString("gender"))
        );
        user.setId(rs.getLong("user_id"));

        Role role = new Role(
                rs.getLong("role_id"),
                UserRole.valueOf(rs.getString("role_name"))
        );
        user.setUserRole(role);
        user.setUserStatus(UserStatus.valueOf(rs.getString("status")));
        user.setCreatedAt(rs.getTimestamp("created_at"));

        return user;
    }

    public static Role extractRoleFromResultSet(ResultSet rs) throws SQLException {
        return new Role(
                rs.getLong("role_id"),
                UserRole.valueOf(rs.getString("role_name"))
        );
    }

    public static Permission extractPermissionFromResultSet(ResultSet rs) throws SQLException {
        return new Permission(
                rs.getLong("permission_id"),
                PermissionType.valueOf(rs.getString("permission_name")),
                rs.getString("permission_description")
        );
    }

    public static String convertToSnakeCase(String camelCase) {
        if (camelCase == null || camelCase.isEmpty()) {
            return camelCase;
        }
        return camelCase.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }
}

package dev.mirodil.testing_system.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.mirodil.testing_system.models.*;
import dev.mirodil.testing_system.models.enums.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DataUtil {
    private static final Logger logger = LoggerFactory.getLogger(DataUtil.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();


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

    public static String convertToSnakeCase(String camelCase) {
        if (camelCase == null || camelCase.isEmpty()) {
            return camelCase;
        }
        return camelCase.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }

    public static User extractUserFromResultSet(ResultSet rs) throws SQLException {
        User user = new User(
                rs.getLong("user_id"),
                rs.getString("email"),
                rs.getString("password"),
                rs.getLong("role_id"),
                rs.getString("fname"),
                rs.getString("lname"),
                UserGender.valueOf(rs.getString("gender")),
                UserStatus.valueOf(rs.getString("status")),
                rs.getTimestamp("created_at")
        );
        Role role = extractRoleFromResultSet(rs);
        user.setUserRole(role);

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

    public static TestEvent extractTestEventFromResultSet(ResultSet rs) throws SQLException {
        TestEvent testEvent = new TestEvent(
                rs.getLong("test_event_id"),
                rs.getLong("test_taker_id"),
                rs.getLong("test_id"),
                rs.getTimestamp("event_datetime"),
                TestEventStatus.valueOf(rs.getString("test_event_status")),
                (Double) rs.getObject("score"),
                (Boolean) rs.getObject("is_passed"),
                rs.getTimestamp("started_at"),
                rs.getTimestamp("finished_at"),
                rs.getTimestamp("test_event_created_at")
        );
        if (hasColumn(rs, "test_attempt")) {
            List<TestAttempt> testAttempt = extractTestAttemptFromResultSet(rs);
            testEvent.setTestAttempt(testAttempt);
        }

        testEvent.setTestTaker(extractTestTakerFromResultSet(rs));

        testEvent.setTest(extractTestFromResultSet(rs));

        return testEvent;
    }

    private static List<TestAttempt> extractTestAttemptFromResultSet(ResultSet rs) throws SQLException {
        String testAttemptJson = rs.getString("test_attempt");
        if (testAttemptJson != null && !testAttemptJson.isEmpty()) {
            try {
                return objectMapper.readValue(testAttemptJson, new TypeReference<>() {
                });
            } catch (Exception e) {
                throw new SQLException("Failed to deserialize test_attempt JSON", e);
            }
        }

        return List.of();
    }

    private static User extractTestTakerFromResultSet(ResultSet rs) throws SQLException {
        User testTaker = new User(
                rs.getLong("test_taker_id"),
                rs.getString("email"),
                rs.getString("fname"),
                rs.getString("lname")
        );
        String[] otherUserColumns = {"gender", "user_status", "user_created_at"};
        if (hasColumns(rs, otherUserColumns)) {
            testTaker.setGender(UserGender.valueOf(rs.getString("gender")));
            testTaker.setUserStatus(UserStatus.valueOf(rs.getString("user_status")));
            testTaker.setCreatedAt(rs.getTimestamp("user_created_at"));
        }
        if (isColumnSet(rs, "role_name")) {
            testTaker.setUserRole(extractRoleFromResultSet(rs));
        }

        return testTaker;
    }

    public static Test extractTestFromResultSet(ResultSet rs) throws SQLException {
        return new Test(
                rs.getLong("test_id"),
                rs.getString("title"),
                hasColumn(rs, "description") ? rs.getString("description") : null,
                rs.getInt("duration"),
                rs.getInt("no_of_questions"),
                rs.getInt("passing_percentage"),
                hasColumn(rs, "should_shuffle") ? rs.getBoolean("should_shuffle") : null,
                hasColumn(rs, "should_randomly_pick") ? rs.getBoolean("should_randomly_pick") : null,
                hasColumn(rs, "deleted_at") ? rs.getTimestamp("deleted_at") : null
        );
    }

    /// Note: Int, Double, Boolean is assumed set even if it's null
    public static boolean isColumnSet(ResultSet rs, String column) throws SQLException {
        if (rs == null || column == null || column.trim().isEmpty()) {
            return false;
        }

        if (!hasColumn(rs, column)) {
            return false;
        }

        Object value = rs.getObject(column);
        if (value == null) {
            return false;
        }

        if (value instanceof String) {
            return !((String) value).trim().isEmpty();
        }

        return true;
    }

    public static boolean hasColumn(ResultSet rs, String column) {
        try {
            rs.findColumn(column);
            return true;
        } catch (SQLException e) {
            logger.debug("column doesn't exist {}", column);
        }
        return false;
    }

    public static boolean hasColumns(ResultSet rs, String[] columns) {
        for (String column : columns) {
            if (!hasColumn(rs, column)) {
                return false;
            }
        }
        return true;
    }
}

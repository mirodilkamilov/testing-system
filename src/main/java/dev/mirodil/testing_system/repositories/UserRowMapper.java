package dev.mirodil.testing_system.repositories;

import dev.mirodil.testing_system.models.User;
import dev.mirodil.testing_system.models.UserGender;
import dev.mirodil.testing_system.models.UserRole;
import dev.mirodil.testing_system.models.UserStatus;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class UserRowMapper implements RowMapper<User> {

    @Override
    public User mapRow(ResultSet rs, int rowNum) throws SQLException {
        User user = new User(
                rs.getString("email"),
                rs.getString("password"),
                rs.getString("fname"),
                rs.getString("lname"),
                UserGender.valueOf(rs.getString("gender"))
        );
        user.setId(rs.getLong("id"));
        user.setUserRole(UserRole.valueOf(rs.getString("role")));
        user.setUserStatus(UserStatus.valueOf(rs.getString("status")));
        user.setCreatedAt(rs.getDate("created_at"));

        return user;
    }
}

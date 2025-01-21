package dev.mirodil.testing_system.repositories;

import dev.mirodil.testing_system.models.User;
import dev.mirodil.testing_system.utils.DataUtil;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class UserRowMapper implements RowMapper<User> {

    @Override
    public User mapRow(ResultSet rs, int rowNum) throws SQLException {
        return DataUtil.extractUserFromResultSet(rs);
    }
}

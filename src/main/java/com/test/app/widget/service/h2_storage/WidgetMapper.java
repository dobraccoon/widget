package com.test.app.widget.service.h2_storage;

import com.test.app.widget.service.Widget;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

@Component
public class WidgetMapper implements RowMapper<Widget> {
    @Override
    public Widget mapRow(final ResultSet rs, final int rowNum) throws SQLException {
        return new Widget(
                UUID.fromString(rs.getString("ID")),
                rs.getInt("COORDINATE_X"),
                rs.getInt("COORDINATE_Y"),
                rs.getInt("INDEX_Z"),
                rs.getInt("WIDTH"),
                rs.getInt("HEIGHT"),
                rs.getTimestamp("LAST_MODIFICATION_DATE_TIME").toLocalDateTime()
        );
    }
}

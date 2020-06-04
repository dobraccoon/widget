package com.test.app.widget.service.h2_storage;

import com.test.app.widget.service.Widget;
import org.springframework.jdbc.IncorrectResultSetColumnCountException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Repository
public class H2StorageRepository {

    private static final String UPDATE = "UPDATE WIDGETS " +
                                         "SET COORDINATE_X = :coordinateX, " +
                                         "    COORDINATE_Y = :coordinateY, " +
                                         "    INDEX_Z = :indexZ, " +
                                         "    WIDTH = :width, " +
                                         "    HEIGHT = :height, " +
                                         "    LAST_MODIFICATION_DATE_TIME = now() AT TIME ZONE 'UTC' " +
                                         "WHERE ID = :id";
    private static final String DELETE_BY_ID = "DELETE FROM WIDGETS " +
                                               "WHERE ID = :id";
    private static final String LOAD_BY_ID = "SELECT * FROM WIDGETS " +
                                             "WHERE ID = :id";
    private static final String LOAD_ALL_ORDERED_BY_INDEX_Z = "SELECT * FROM  WIDGETS " +
                                                              "ORDER BY index_z";
    private static final String INCREMENT_ALL_WIDGETS_Z_INDEXES_BIGGER_OR_EQUALS_THIS =
            "UPDATE WIDGETS " +
            "SET INDEX_Z = INDEX_Z + :incrementValue " +
            "WHERE INDEX_Z >= :indexZ";
    private static final String GET_MAX_Z_INDEX_VALUE =
            "SELECT coalesce(MAX(INDEX_Z), 0) " +
            "FROM WIDGETS ";
    private static final String FIND_BY_INDEX_Z_VALUE =
            "SELECT * " +
            "FROM WIDGETS  " +
            "WHERE INDEX_Z = :indexZ";
    private static final String LOAD_PAGING =
            "SELECT  * " +
            "FROM    ( SELECT    ROW_NUMBER() OVER ( ORDER BY INDEX_Z ) AS row_number, * " +
            "          FROM      WIDGETS " +
            "        ) AS rcr " +
            "WHERE   rcr.row_number >= :page" +
            "    AND rcr.row_number <= :size " +
            "ORDER BY rcr.row_number";
    private static final String LOAD_BY_SELECTED_AREA =
            "SELECT * " +
            "FROM WIDGETS " +
            "WHERE TRUE AND " +
            "    (COORDINATE_X + WIDTH / 2) <= :rightX AND " +
            "    (COORDINATE_X - WIDTH / 2) >= :leftX AND " +
            "    (COORDINATE_Y + HEIGHT / 2) <= :rightY AND " +
            "    (COORDINATE_Y - HEIGHT / 2) >= :leftY";

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;
    private final WidgetMapper widgetMapper;

    public H2StorageRepository(
            final NamedParameterJdbcTemplate jdbcTemplate,
            final WidgetMapper widgetMapper
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate.getJdbcTemplate())
                .withTableName("WIDGETS");
        this.widgetMapper = widgetMapper;
    }

    Widget create(
            final Widget widget
    ) {
        simpleJdbcInsert.execute(doMapping(widget));
        return widget;
    }

    Widget update(
            final Widget widget
    ) {
        jdbcTemplate.update(
                UPDATE,
                doMapping(widget)
        );
        return widget;
    }

    UUID deleteById(final UUID id) {
        jdbcTemplate.update(
                DELETE_BY_ID,
                new MapSqlParameterSource().addValue("id", id)
        );
        return id;
    }

    Widget loadById(final UUID id) {
        return jdbcTemplate.queryForObject(
                LOAD_BY_ID,
                new MapSqlParameterSource().addValue("id", id),
                widgetMapper
        );
    }

    List<Widget> loadAllOrderedByIndexZ() {
        return jdbcTemplate.query(
                LOAD_ALL_ORDERED_BY_INDEX_Z,
                widgetMapper
        );
    }

    void incrementAllWidgetsZIndexesBiggerOrEqualsThis(
            final int indexZ,
            final int incrementValue
    ) {
        jdbcTemplate.update(
                INCREMENT_ALL_WIDGETS_Z_INDEXES_BIGGER_OR_EQUALS_THIS,
                new MapSqlParameterSource()
                        .addValue("indexZ", indexZ)
                        .addValue("incrementValue", incrementValue)
        );
    }

    int getMaxZIndexValue() {
        return jdbcTemplate.queryForObject(
                GET_MAX_Z_INDEX_VALUE,
                new MapSqlParameterSource(),
                Integer.class
        );
    }

    Optional<Widget> findByIndexZValue(final int indexZ) {
        final List<Widget> result = jdbcTemplate.query(
                FIND_BY_INDEX_Z_VALUE,
                new MapSqlParameterSource().addValue("indexZ", indexZ),
                widgetMapper
        );
        if (result.size() == 1) {
            return Optional.of(result.get(0));
        } else {
            if (result.size() > 1) {
                throw new IncorrectResultSetColumnCountException(1, result.size());
            }
            return Optional.empty();
        }
    }

    List<Widget> loadPaging(final int page, final int size) {
        return jdbcTemplate.query(
                LOAD_PAGING,
                new MapSqlParameterSource()
                        .addValue("page", page)
                        .addValue("size", size),
                widgetMapper
        );
    }

    List<Widget> loadBySelectedArea(
            final int leftX,
            final int leftY,
            final int rightX,
            final int rightY
    ) {
        return jdbcTemplate.query(
                LOAD_BY_SELECTED_AREA,
                new MapSqlParameterSource()
                        .addValue("leftX", leftX)
                        .addValue("leftY", leftY)
                        .addValue("rightX", rightX)
                        .addValue("rightY", rightY),
                widgetMapper
        );
    }

    private MapSqlParameterSource doMapping(final Widget widget) {
        return new MapSqlParameterSource()
                .addValue("id", widget.getId())
                .addValue("coordinateX", widget.getCoordinateX())
                .addValue("coordinateY", widget.getCoordinateY())
                .addValue("indexZ", widget.getIndexZ())
                .addValue("width", widget.getWidth())
                .addValue("height", widget.getHeight())
                .addValue("lastModificationDateTime", widget.getLastModificationDateTime());
    }
}

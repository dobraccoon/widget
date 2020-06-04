CREATE TABLE WIDGETS (
    ID                          UUID PRIMARY KEY,
    COORDINATE_X                INT       NOT NULL,
    COORDINATE_Y                INT       NOT NULL,
    INDEX_Z                     INT       NOT NULL,
    WIDTH                       INT       NOT NULL,
    HEIGHT                      INT       NOT NULL,
    LAST_MODIFICATION_DATE_TIME TIMESTAMP NOT NULL DEFAULT (now() AT TIME ZONE 'UTC')
);

CREATE UNIQUE INDEX UI_rectangular_widgets_ON_index_z ON WIDGETS(INDEX_Z);


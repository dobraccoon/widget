package com.test.app.widget.service;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

/**
 * Widget on the plane
 */
@ApiModel("Widget")
@Table("WIDGETS")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Component
public class Widget {
    /**
     * Widget identifier
     */
    @ApiModelProperty("Widget identifier")
    @Id
    private UUID id = UUID.randomUUID();
    /**
     * X coordinate on plane
     */
    @ApiModelProperty("X coordinate on plane")
    private int coordinateX;
    /**
     * Y coordinate on plane
     */
    @ApiModelProperty("Y coordinate on plane")
    private int coordinateY;

    /**
     * Z-index​is a unique sequence common to all widgets
     * that determines the order of widgets (regardless of their coordinates).Gaps are allowed.
     * The higher the value, the higher the widget lies on the plane
     */
    @ApiModelProperty("Z-index is a unique sequence common to all widgets\n" +
                      "that determines the order of widgets (regardless of their coordinates).Gaps are allowed.\n" +
                      "The higher the value, the higher the widget lies on the plane")
    @Setter
    private Integer indexZ;
    /**
     * Widget width
     */
    @ApiModelProperty("Widget width")
    private int width;
    /**
     * Widget height
     */
    @ApiModelProperty("Widget height")
    private int height;

    /**
     * Widget last modification Date/Time in UTC format
     */
    @Setter
    @ApiModelProperty("Widget last modification Date/Time in UTC format")
    private LocalDateTime lastModificationDateTime = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();

    public Widget(
            final int coordinateX,
            final int coordinateY,
            final Integer indexZ,
            final int width,
            final int height
    ) {
        this.coordinateX = coordinateX;
        this.coordinateY = coordinateY;
        this.indexZ = indexZ;
        this.width = width;
        this.height = height;
    }
}

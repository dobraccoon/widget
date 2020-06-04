package com.test.app.widget.rest.inputs;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.lang.Nullable;

/**
 * Input for creating widget
 */
@ApiModel("Input widget model for creating new")
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class InputWidget {
    @ApiModelProperty("X coordinate on plane")
    private int coordinateX;
    @ApiModelProperty("Y coordinate on plane")
    private int coordinateY;
    @ApiModelProperty("Z-index is a unique sequence common to all widgets\n" +
                      "that determines the order of widgets (regardless of their coordinates).Gaps are allowed.\n" +
                      "The higher the value, the higher the widget lies on the plane")
    @Setter
    @Nullable
    private Integer indexZ;
    @ApiModelProperty(" Widget width")
    private int width;
    @ApiModelProperty("Widget height")
    private int height;
}

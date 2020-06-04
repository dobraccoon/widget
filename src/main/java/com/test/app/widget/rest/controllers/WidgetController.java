package com.test.app.widget.rest.controllers;


import com.test.app.widget.rest.inputs.InputWidget;
import com.test.app.widget.service.Widget;
import com.test.app.widget.service.WidgetService;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@RequestMapping("/widget")
@RestController
public class WidgetController {

    private final WidgetService widgetService;


    @ApiOperation(value = "Create new widget")
    @Transactional
    @PostMapping("/create")
    public Widget create(@RequestBody final InputWidget widgetInput) {
        return widgetService.create(widgetInput);
    }

    @ApiOperation(value = "Updating widget existed into current storage")
    @Transactional
    @PutMapping("/update")
    public Widget update(@RequestBody final Widget widget) {
        return widgetService.update(widget);
    }

    @ApiOperation(value = "Remove widget by id from current storage")
    @Transactional
    @DeleteMapping("/deleteById")
    public void deleteById(@RequestParam(name = "id") final @NonNull UUID id) {
        widgetService.deleteById(id);
    }

    @ApiOperation(value = "Hello page")
    @GetMapping
    public String mainPageMapping() {
        return "Hello i'm working =)";
    }

    @ApiOperation(value = "Getting widget by id")
    @GetMapping("/getById")
    public Widget getById(@RequestParam(name = "id") final @NonNull UUID id) {
        return widgetService.loadById(id);
    }

    @ApiOperation(value = "Getting all widgets into current storage ordered by Z-Index")
    @GetMapping("/getAll")
    public List<Widget> getAll() {
        return widgetService.loadAllOrderedByIndexZ();
    }

    @ApiOperation(value = "Getting paging widgets into current storage ordered by Z-Index")
    @GetMapping("/getPaging")
    public List<Widget> getPaging(
            @RequestParam("page") @Nullable final Integer page,
            @RequestParam("size") @Nullable final Integer size
    ) {
        return widgetService.loadPaging(page, size);
    }

    @ApiOperation(value = "Getting widgets filtered by selected area")
    @GetMapping("/getBySelectedArea")
    public List<Widget> getBySelectedArea(
            @RequestParam("leftX") @Nullable final int leftX,
            @RequestParam("leftY") @Nullable final int leftY,
            @RequestParam("rightX") @Nullable final int rightX,
            @RequestParam("rightY") @Nullable final int rightY
    ) {
        return widgetService.loadBySelectedArea(leftX, leftY, rightX, rightY);
    }
}

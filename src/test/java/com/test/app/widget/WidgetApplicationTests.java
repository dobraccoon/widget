package com.test.app.widget;

import com.test.app.widget.rest.controllers.WidgetController;
import com.test.app.widget.rest.inputs.InputWidget;
import com.test.app.widget.service.Widget;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class WidgetApplicationTests {

    @Autowired
    private WidgetController controller;

    @Test
    public void contexLoads() {
        assertThat(controller).isNotNull();
    }

    @Test
    public void createTest() {
        final InputWidget inputForCreatingWidget = getRandomInputWidget(1);
        final Widget createdWidget = controller.create(inputForCreatingWidget);
        assertThat(createdWidget != null && createdWidget.getId() != null).isTrue();
        assertThat(isWidgetsFieldsAreEquals(inputForCreatingWidget, createdWidget)).isTrue();
    }

    @Test
    public void loadByIdTest() {
        final Widget createdWidget = controller.create(getRandomInputWidget(1));
        final Widget loadedWidget = controller.getById(createdWidget.getId());
        assertThat(loadedWidget.getId().equals(createdWidget.getId())).isTrue();
        assertThat(isWidgetsFieldsAreEquals(createdWidget, loadedWidget)).isTrue();
        removeWidgetsByIds(Collections.singleton(createdWidget.getId()));
    }

    @Test
    public void updateTest() {
        final Widget createdWidget = controller.create(getRandomInputWidget(1));
        final Widget widgetForUpdate = new Widget(
                createdWidget.getId(),
                createdWidget.getCoordinateX() + 11,
                createdWidget.getCoordinateY() + 12,
                createdWidget.getIndexZ() + 13,
                createdWidget.getWidth() + 100,
                createdWidget.getHeight() + 110,
                createdWidget.getLastModificationDateTime()
        );

        final Widget updatedWidget = controller.update(widgetForUpdate);
        assertThat(isWidgetsFieldsAreEquals(widgetForUpdate, updatedWidget)).isTrue();
        assertThat(isWidgetsFieldsAreEquals(createdWidget, updatedWidget)).isFalse();
        removeWidgetsByIds(Collections.singleton(updatedWidget.getId()));
    }

    @Test
    public void deleteByIdTest() {
        final Widget createdWidget = controller.create(getRandomInputWidget(1));
        controller.deleteById(createdWidget.getId());
        assertThat(controller.getAll()
                             .stream()
                             .anyMatch(widget -> widget.getId().equals(createdWidget.getId()))).isFalse();
    }

    @Test
    public void newWidgetWithNullIndexZValueTest() {
        final Set<UUID> testWidgetIdsForRemoving = new HashSet<>();
        //Create 5 different widgets
        getRandomInputWidgetsWithUniqueZIndexes(5)
                .forEach(widgetInout ->
                        testWidgetIdsForRemoving.add(controller.create(widgetInout).getId())
                );
        // Create the 6th widget without Z-index
        final Widget createdWidget = controller.create(getRandomInputWidget(null));
        testWidgetIdsForRemoving.add(createdWidget.getId());

        // Find widget with max Z-index
        final Widget widgetWithMaxZIndex =
                controller.getAll()
                          .stream()
                          .max(Comparator.comparing(Widget::getIndexZ))
                          .orElseThrow(RuntimeException::new);
        //widget with max Z-index must be equals created 6th widget without Z-index
        assertThat(widgetWithMaxZIndex.getId().equals(createdWidget.getId())).isTrue();
        assertThat(isWidgetsFieldsAreEquals(createdWidget, widgetWithMaxZIndex)).isTrue();
        removeWidgetsByIds(testWidgetIdsForRemoving);
    }

    @Test
    public void widgetOffsetTest() {
        final List<Widget> createdWidgets = new ArrayList<>();

        //Create 10 different widgets
        getRandomInputWidgetsWithUniqueZIndexes(4)
                .forEach(widgetInout ->
                        createdWidgets.add(controller.create(widgetInout))
                );
        //sort created widgets by Z-index
        final List<Widget> sortedByZIndexWidgets =
                createdWidgets.stream()
                              .sorted(Comparator.comparing(Widget::getIndexZ))
                              .collect(Collectors.toList());

        // Choose Z-index from created widgets
        final int indexZForNewWidgetFromExistedWidget =
                sortedByZIndexWidgets.get(sortedByZIndexWidgets.size() / 2).getIndexZ();

        // Widgets with Z-index greater or equal than chosen Z-index
        final Map<UUID, Widget> widgetsMapSortedByZIndexBiggerOrEqualsByZIndex =
                sortedByZIndexWidgets.stream()
                                     .filter(widget -> widget.getIndexZ() >= indexZForNewWidgetFromExistedWidget)
                                     .map(widget -> new Widget(
                                             widget.getId(),
                                             widget.getCoordinateX(),
                                             widget.getCoordinateY(),
                                             widget.getIndexZ(),
                                             widget.getWidth(),
                                             widget.getHeight(),
                                             widget.getLastModificationDateTime()
                                     ))
                                     .collect(Collectors.toMap(Widget::getId, widget -> widget));

        // Create new widget with existed Z-index
        createdWidgets.add(controller.create(getRandomInputWidget(indexZForNewWidgetFromExistedWidget)));

        // Widgets with Z-index greater than chosen Z-index after creating new widget with existed Z-index
        final Map<UUID, Widget> widgetsMapWithBiggerZIndexAfterAddNewOneWithSame =
                controller.getAll().stream()
                          .filter(widget -> widget.getIndexZ() > indexZForNewWidgetFromExistedWidget)
                          .collect(Collectors.toMap(Widget::getId, widget -> widget));

        // Check that widgets was offset by Z-index
        widgetsMapSortedByZIndexBiggerOrEqualsByZIndex.forEach(
                (id, widget) -> {
                    assertThat(
                            widgetsMapWithBiggerZIndexAfterAddNewOneWithSame.get(id).getIndexZ() > (widget.getIndexZ())
                    ).isTrue();
                });

        removeWidgetsByIds(createdWidgets.stream()
                                         .map(Widget::getId)
                                         .collect(Collectors.toSet()));

    }

    @Test
    public void defaultPagingQueryTest() {
        final Set<UUID> testWidgetIdsForRemoving = new HashSet<>();
        //Create 30 different widgets
        getRandomInputWidgetsWithUniqueZIndexes(30)
                .forEach(widgetInout ->
                        testWidgetIdsForRemoving.add(controller.create(widgetInout).getId())
                );

        assertThat(controller.getPaging(null, null).size() == 10).isTrue();
        removeWidgetsByIds(testWidgetIdsForRemoving);
    }

    @Test
    public void pagingQueryTest() {
        final Set<UUID> testWidgetIdsForRemoving = new HashSet<>();
        //Create 30 different widgets
        getRandomInputWidgetsWithUniqueZIndexes(30)
                .forEach(widgetInout ->
                        testWidgetIdsForRemoving.add(controller.create(widgetInout).getId())
                );

        final int widgetsLimit = 27;
        assertThat(controller.getPaging(0, widgetsLimit).size() == widgetsLimit).isTrue();
        removeWidgetsByIds(testWidgetIdsForRemoving);
    }

    @Test
    public void getBySelectedAreaTest() {
        final Widget widget1 = controller.create(
                new InputWidget(50, 50, 1, 100, 100)
        );
        final Widget widget2 = controller.create(
                new InputWidget(50, 100, 2, 100, 100)
        );
        final Widget widget3 = controller.create(
                new InputWidget(100, 100, 1, 100, 100)
        );
        final Widget widget4 = controller.create(
                new InputWidget(50, 100, 4, 110, 100)
        );

        final List<Widget> filteredWidgets = controller.getBySelectedArea(0, 0, 100, 150);
        assertThat(filteredWidgets.stream().anyMatch(widget -> widget.getId().equals(widget1.getId()))).isTrue();
        assertThat(filteredWidgets.stream().anyMatch(widget -> widget.getId().equals(widget2.getId()))).isTrue();
        assertThat(filteredWidgets.stream().anyMatch(widget -> widget.getId().equals(widget3.getId()))).isFalse();
        assertThat(filteredWidgets.stream().anyMatch(widget -> widget.getId().equals(widget4.getId()))).isFalse();
        removeWidgetsByIds(new HashSet<>(Arrays.asList(widget1.getId(), widget2.getId(), widget3.getId())));
    }


    private InputWidget getRandomInputWidget(final Integer indexZ) {
        final Random rand = new Random();
        return new InputWidget(
                rand.nextInt(100),
                rand.nextInt(200),
                indexZ,
                rand.nextInt(1000),
                rand.nextInt(1000)
        );
    }

    private List<InputWidget> getRandomInputWidgetsWithUniqueZIndexes(final int widgetsCount) {
        final List<InputWidget> result = new ArrayList<>();

        for (int i = 0; i < widgetsCount; i++) {
            result.add(getRandomInputWidget(i));
        }

        return result;
    }

    private void removeWidgetsByIds(final Set<UUID> widgetsIds) {
        widgetsIds.forEach(id -> controller.deleteById(id));
    }

    private boolean isWidgetsFieldsAreEquals(
            final InputWidget inputWidget,
            final Widget widget
    ) {
        if (inputWidget.getIndexZ() == null || widget.getIndexZ() == null) {
            throw new AssertionError();
        }
        return inputWidget.getCoordinateX() == widget.getCoordinateX() &&
               inputWidget.getCoordinateY() == widget.getCoordinateY() &&
               inputWidget.getIndexZ().equals(widget.getIndexZ()) &&
               inputWidget.getHeight() == widget.getHeight() &&
               inputWidget.getWidth() == widget.getWidth();
    }

    private boolean isWidgetsFieldsAreEquals(
            final Widget widgetOne,
            final Widget widgetTwo
    ) {
        if (widgetOne.getIndexZ() == null || widgetTwo.getIndexZ() == null) {
            throw new AssertionError();
        }
        return widgetOne.getCoordinateX() == widgetTwo.getCoordinateX() &&
               widgetOne.getCoordinateY() == widgetTwo.getCoordinateY() &&
               widgetOne.getIndexZ().equals(widgetTwo.getIndexZ()) &&
               widgetOne.getHeight() == widgetTwo.getHeight() &&
               widgetOne.getWidth() == widgetTwo.getWidth();
    }

    private InputWidget configInputWid() {
        return new InputWidget(

        );
    }
}


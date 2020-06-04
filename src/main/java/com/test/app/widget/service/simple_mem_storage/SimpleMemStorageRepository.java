package com.test.app.widget.service.simple_mem_storage;

import com.test.app.widget.service.Widget;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.stream.Collectors;


@AllArgsConstructor
@Component
public class SimpleMemStorageRepository {

    private final Map<UUID, Widget> storage = new ConcurrentHashMap<>();
    /**
     * Structure for control Z-Index order where:
     * key: Z-Index
     * value: widgetId
     */
    private final SortedMap<Integer, UUID> widgetIdOrderedByIndexZMap = new ConcurrentSkipListMap<>(Collections.reverseOrder());

    Widget create(final Widget newWidget) {
        if (findByIndexZValue(newWidget.getIndexZ()).isPresent()) {
            // Z-indexes was not recounted before update
            throw new RuntimeException(String.format(
                    "Can't create new widget id=%s with duplicate Z-index value=%s",
                    newWidget.getId(),
                    newWidget.getIndexZ()
            ));
        }
        setWidgetLastModificationTimeAsNow(newWidget);
        storage.put(newWidget.getId(), newWidget);
        widgetIdOrderedByIndexZMap.put(newWidget.getIndexZ(), newWidget.getId());
        return newWidget;
    }

    Widget update(final Widget newWidget) {
        final Widget oldWidget = loadById(newWidget.getId());

        if (oldWidget.getIndexZ().equals(newWidget.getIndexZ())) {
            /*
            This widget Z-index was not updated
            Update only storage
            */
            setWidgetLastModificationTimeAsNow(newWidget);
            storage.put(newWidget.getId(), newWidget);
            return newWidget;
        } else {
            if (findByIndexZValue(newWidget.getIndexZ()).isPresent()) {
                // Z-indexes was not recounted before update
                throw new RuntimeException(String.format(
                        "Can't update widget id=%s with duplicate Z-index value=%s",
                        newWidget.getId(),
                        newWidget.getIndexZ()
                ));
            }

            widgetIdOrderedByIndexZMap.remove(oldWidget.getIndexZ());
            widgetIdOrderedByIndexZMap.put(newWidget.getIndexZ(), newWidget.getId());
            setWidgetLastModificationTimeAsNow(newWidget);
            storage.put(newWidget.getId(), newWidget);
        }

        return newWidget;
    }

    UUID removeById(final @NonNull UUID id) {
        widgetIdOrderedByIndexZMap.remove(storage.get(id).getIndexZ());
        return storage.remove(id).getId();
    }

    void incrementAndUpdateZIndexById(final UUID widgetId, final int incrementNumber) {
        final int oldZIndexValue = storage.get(widgetId).getIndexZ();
        storage.get(widgetId).setIndexZ(oldZIndexValue + incrementNumber);
        widgetIdOrderedByIndexZMap.remove(oldZIndexValue);
        widgetIdOrderedByIndexZMap.put(oldZIndexValue + incrementNumber, widgetId);
    }

    @NonNull Widget loadById(final @NonNull UUID id) {
        return findById(id).orElseThrow(RuntimeException::new);
    }

    Optional<Widget> findById(final @NonNull UUID id) {
        return Optional.of(storage.getOrDefault(id, null));
    }

    Widget loadByIndexZValue(final int widgetIndexZ) {
        return loadById(widgetIdOrderedByIndexZMap.get(widgetIndexZ));
    }

    List<Widget> loadPaging(final int page, final int size) {
        if (page < 0) {
            throw new IllegalArgumentException("Error: page value can't be < 0");
        }
        final int startPaging = Math.max(page - 1, 0);
        return loadAllSortedByIndexZ().stream()
                                      .skip(startPaging)
                                      .limit(size)
                                      .collect(Collectors.toList());
    }

    List<Widget> loadAllSortedByIndexZ() {
        return storage.values()
                      .stream()
                      .sorted(Comparator.comparing(Widget::getIndexZ))
                      .collect(Collectors.toList());
    }

    Optional<Widget> findByIndexZValue(final int widgetIndexZ) {
        final UUID widgetId = widgetIdOrderedByIndexZMap.get(widgetIndexZ);
        return widgetId != null ? findById(widgetId) : Optional.empty();
    }

    /**
     * Find max Z-index value into storage or 0 if storage is empty
     *
     * @return max Z-index value into storage or 0 if storage is empty
     */
    int getMaxZIndexValue() {
        return storage.isEmpty() ? 0 : widgetIdOrderedByIndexZMap.firstKey();
    }

    List<Integer> loadAllZIndexesByDescOrder() {
        return new ArrayList<>(widgetIdOrderedByIndexZMap.keySet());
    }

    List<Widget> loadBySelectedArea(
            final int leftX,
            final int leftY,
            final int rightX,
            final int rightY
    ) {
        return storage.values()
                      .stream()
                      .filter(w -> (w.getCoordinateX() + (w.getWidth() / 2)) <= rightX)
                      .filter(w -> (w.getCoordinateX() - (w.getWidth() / 2)) >= leftX)
                      .filter(w -> (w.getCoordinateX() + (w.getHeight() / 2)) <= rightY)
                      .filter(w -> (w.getCoordinateX() - (w.getHeight() / 2)) >= leftY)
                      .collect(Collectors.toList());
    }

    /**
     * Set last modification Date/Time as now in UTC format
     */
    private void setWidgetLastModificationTimeAsNow(final Widget widget) {
        widget.setLastModificationDateTime(OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime());
    }
}

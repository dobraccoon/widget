package com.test.app.widget.service.h2_storage;

import com.test.app.widget.rest.inputs.InputWidget;
import com.test.app.widget.service.Widget;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@AllArgsConstructor
@Service
public class H2StorageService {
    private final H2StorageRepository h2StorageRepository;

    public Widget create(final InputWidget widgetInput) {
        final Widget createWidget = new Widget(
                widgetInput.getCoordinateX(),
                widgetInput.getCoordinateY(),
                widgetInput.getIndexZ(),
                widgetInput.getWidth(),
                widgetInput.getHeight()
        );
        recountIndexZ(createWidget);
        return h2StorageRepository.create(createWidget);
    }

    public Widget update(final Widget widget) {
        recountIndexZ(widget);
        return h2StorageRepository.update(widget);
    }

    public UUID deleteById(final @NonNull UUID id) {
        return h2StorageRepository.deleteById(id);
    }

    public Widget loadById(final @NonNull UUID id) {
        return h2StorageRepository.loadById(id);
    }

    public List<Widget> loadAllOrderedByIndexZ() {
        return h2StorageRepository.loadAllOrderedByIndexZ();
    }

    public List<Widget> loadPaging(final int page, final int size) {
        return h2StorageRepository.loadPaging(page, size);
    }

    public List<Widget> loadBySelectedArea(
            final int leftX,
            final int leftY,
            final int rightX,
            final int rightY
    ) {
        return h2StorageRepository.loadBySelectedArea(leftX, leftY, rightX, rightY);
    }

    /**
     * Recount all widgets Z-index values if it's necessary
     *
     * @param widget widget whose Z-index must be checked, for recounting other widget Z-indexes
     *               if widget#indexZ is Null, it will be update for existed maxIndexZValue+1
     *               for making this widget like foreground
     */
    private void recountIndexZ(final Widget widget) {
        if (widget.getIndexZ() == null) {
            /*
            Z-index is Null, it means it's foreground widget
            */
            final int foregroundZIndex = h2StorageRepository.getMaxZIndexValue() + 1;
            widget.setIndexZ(foregroundZIndex);
        } else {
            final Optional<Widget> optWidget = h2StorageRepository.findByIndexZValue(widget.getIndexZ());
            /**
             Need to recount only if same Z-index is present into storage
             AND
             if it's not Z-index of this{@link widget} widget
             */
            if (optWidget.isPresent() && !optWidget.get().getId().equals(widget.getId())) {
                h2StorageRepository.incrementAllWidgetsZIndexesBiggerOrEqualsThis(widget.getIndexZ(), 1);
            }
        }
    }
}

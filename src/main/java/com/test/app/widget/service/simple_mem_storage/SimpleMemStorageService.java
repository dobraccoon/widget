package com.test.app.widget.service.simple_mem_storage;

import com.test.app.widget.rest.inputs.InputWidget;
import com.test.app.widget.service.Widget;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class SimpleMemStorageService {
    private final SimpleMemStorageRepository simpleMemStorageRepository;

    public Widget create(final InputWidget widget) {
        final Widget widgetForCreating = new Widget(
                widget.getCoordinateX(),
                widget.getCoordinateY(),
                widget.getIndexZ(),
                widget.getWidth(),
                widget.getHeight()
        );
        recountIndexZ(widgetForCreating);

        return simpleMemStorageRepository.create(
                new Widget(
                        widgetForCreating.getCoordinateX(),
                        widgetForCreating.getCoordinateY(),
                        widgetForCreating.getIndexZ(),
                        widgetForCreating.getWidth(),
                        widgetForCreating.getHeight()
                ));
    }

    public Widget update(final Widget widget) {
        recountIndexZ(widget);
        return simpleMemStorageRepository.update(widget);
    }

    public UUID deleteById(final @NonNull UUID id) {
        return simpleMemStorageRepository.removeById(id);
    }

    public Widget loadById(final @NonNull UUID id) {
        return simpleMemStorageRepository.loadById(id);
    }

    public List<Widget> loadAllOrderedByIndexZ() {
        return simpleMemStorageRepository.loadAllSortedByIndexZ();
    }

    public List<Widget> loadPaging(final int page, final int size) {
        return simpleMemStorageRepository.loadPaging(page, size);
    }

    public List<Widget> loadBySelectedArea(
            final int leftX,
            final int leftY,
            final int rightX,
            final int rightY
    ) {
        return simpleMemStorageRepository.loadBySelectedArea(leftX, leftY, rightX, rightY);
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
            final int foregroundZIndex = simpleMemStorageRepository.getMaxZIndexValue() + 1;
            widget.setIndexZ(foregroundZIndex);
        } else {
            final Optional<Widget> optWidget = simpleMemStorageRepository.findByIndexZValue(widget.getIndexZ());
            /**
             Need to recount only if same Z-index is present into storage
             AND
             if it's not Z-index of this{@link widget}
             */
            if (optWidget.isPresent() && !optWidget.get().getId().equals(widget.getId())) {

                // Getting Z-indexes into desc order
                final List<Integer> indexesZCurrentDescOrder = simpleMemStorageRepository.loadAllZIndexesByDescOrder();

                for (final int iterIndexZ : indexesZCurrentDescOrder) {
                    /*
                    If was founded widget with same indexZ, replace it on indexZ+1 position
                     */
                    if (iterIndexZ == widget.getIndexZ()) {
                        final Widget indexZUpdatedWidget = simpleMemStorageRepository.loadByIndexZValue(iterIndexZ);
                        //update indexZ value
                        incrementAndUpdateZIndexById(indexZUpdatedWidget.getId(), 1);
                        break;
                    }

                    /**
                     Replace each widget with indexZ bigger then new indexZ{@link widgetIndexZ}
                     */
                    final Widget indexZUpdatedWidget = simpleMemStorageRepository.loadByIndexZValue(iterIndexZ);
                    //update indexZ value
                    incrementAndUpdateZIndexById(indexZUpdatedWidget.getId(), 1);
                }
            }
        }
    }

    private void incrementAndUpdateZIndexById(final UUID widgetId, final int incrementValue) {
        simpleMemStorageRepository.incrementAndUpdateZIndexById(widgetId, incrementValue);
    }

}


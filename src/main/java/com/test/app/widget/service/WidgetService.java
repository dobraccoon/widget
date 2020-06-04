package com.test.app.widget.service;

import com.test.app.widget.configs.ConfigProperties;
import com.test.app.widget.rest.inputs.InputWidget;
import com.test.app.widget.service.h2_storage.H2StorageService;
import com.test.app.widget.service.simple_mem_storage.SimpleMemStorageService;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@Service
public class WidgetService {
    private final SimpleMemStorageService simpleMemStorageService;
    private final H2StorageService h2StorageService;
    private ConfigProperties configProperties;

    public Widget create(final InputWidget widgetInput) {
        checkRateLimit();
        if (configProperties.getH2storage().isEnabled()) {
            return h2StorageService.create(widgetInput);
        } else {
            return simpleMemStorageService.create(widgetInput);
        }
    }

    public Widget update(final Widget widget) {
        checkRateLimit();
        if (configProperties.getH2storage().isEnabled()) {
            return h2StorageService.update(widget);
        } else {
            return simpleMemStorageService.update(widget);
        }

    }

    public UUID deleteById(final @NonNull UUID id) {
        checkRateLimit();
        if (configProperties.getH2storage().isEnabled()) {
            return h2StorageService.deleteById(id);
        } else {
            return simpleMemStorageService.deleteById(id);
        }
    }

    public Widget loadById(final @NonNull UUID id) {
        checkRateLimit();
        if (configProperties.getH2storage().isEnabled()) {
            return h2StorageService.loadById(id);
        } else {
            return simpleMemStorageService.loadById(id);
        }
    }

    public List<Widget> loadAllOrderedByIndexZ() {
        checkRateLimit();
        if (configProperties.getH2storage().isEnabled()) {
            return h2StorageService.loadAllOrderedByIndexZ();
        } else {
            return simpleMemStorageService.loadAllOrderedByIndexZ();
        }
    }

    public List<Widget> loadPaging(
            @Nullable Integer page,
            @Nullable Integer size
    ) {
        checkRateLimit();
        /*
        Check if is it necessary to get default paging params
         */
        page = page == null ? 0 : page;
        size = (size == null) ? configProperties.getPagingConfig().getDefaultPagingResultSize() : size;
        //Check about paging limit
        if (size > configProperties.getPagingConfig().getWidgetLimitForLoadingPerQuery()) {
            throw new IllegalArgumentException(
                    String.format(
                            "Error: can't load more then %s widgets by paging query",
                            configProperties.getPagingConfig().getWidgetLimitForLoadingPerQuery()
                    )
            );
        }

        if (configProperties.getH2storage().isEnabled()) {
            return h2StorageService.loadPaging(page, size);
        } else {
            return simpleMemStorageService.loadPaging(page, size);
        }
    }

    public List<Widget> loadBySelectedArea(
            final int leftX,
            final int leftY,
            final int rightX,
            final int rightY
    ) {
        checkRateLimit();
        if (configProperties.getH2storage().isEnabled()) {
            return h2StorageService.loadBySelectedArea(leftX, leftY, rightX, rightY);
        } else {
            return simpleMemStorageService.loadBySelectedArea(leftX, leftY, rightX, rightY);
        }
    }

    private void checkRateLimit() {
        final ConfigProperties.RateLimiting rateLimiting = configProperties.getRateLimiting();

        if (rateLimiting.getNextResetDateTime().isAfter(LocalDateTime.now())) {
            if (rateLimiting.getAvailableRequests() == 0) {
                throw new RuntimeException("Error: Rate limit is reached");
            } else {
                rateLimiting.decrementAvailableRequests();
            }
        } else {
            rateLimiting.setNextResetDateTime(LocalDateTime.now().plusMinutes(1));
            rateLimiting.setAvailableRequests(rateLimiting.getLimit());
        }
    }
}

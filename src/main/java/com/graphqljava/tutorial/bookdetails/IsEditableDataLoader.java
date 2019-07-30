package com.graphqljava.tutorial.bookdetails;

import org.dataloader.DataLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.dataloader.BatchLoader;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;


@Component
public class IsEditableDataLoader {
    private static final Logger logger = LoggerFactory.getLogger(IsEditableDataLoader.class);

    // a batch loader function that will be called with N or more keys for batch loading
    static BatchLoader<String, Boolean> isEditableBatchLoader = keys ->
            CompletableFuture.supplyAsync(() -> getEditableDataByKeys(keys));

    public static DataLoader getLoader() {
        return new DataLoader(isEditableBatchLoader);
    }

    static private List<Boolean> getEditableDataByKeys(List<String> keys) {
        logger.info("data fetch method called with {}", keys);
        return keys.stream().map(key -> key.contains("2")).collect(Collectors.toList());
    }
}

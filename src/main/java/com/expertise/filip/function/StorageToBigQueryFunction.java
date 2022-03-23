package com.expertise.filip.function;

import com.expertise.filip.function.StorageToBigQueryFunction.GCSEvent;
import com.google.cloud.functions.BackgroundFunction;
import com.google.cloud.functions.Context;
import com.expertise.filip.wrapper.BigQueryWrapper;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StorageToBigQueryFunction implements BackgroundFunction<GCSEvent> {

    private static final Logger LOGGER = Logger.getLogger(StorageToBigQueryFunction.class.getName());

    @Autowired
    private BigQueryWrapper bigQueryWrapper;

    @Override
    public void accept(GCSEvent event, Context context) {
        LOGGER.log(Level.INFO, "Merge started with {0} file.", event.name);
        bigQueryWrapper.runExternalTableMergeQuery(event.name);
        LOGGER.log(Level.INFO, "Merge finished for {0} file.", event.name);
    }

    public static class GCSEvent {
        String bucket;
        String name;
        String metageneration;
    }

}

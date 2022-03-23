package com.expertise.filip.scheduler;

import com.expertise.filip.wrapper.BigQueryWrapper;
import com.expertise.filip.wrapper.StorageWrapper;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
public class SchedulerConfig {

    private static final Logger LOGGER = Logger.getLogger(SchedulerConfig.class.getName());
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    @Autowired
    private StorageWrapper storageWrapper;

    @Autowired
    private BigQueryWrapper bigQueryWrapper;

//    @Scheduled(fixedRate = 86400000)
    @Scheduled(fixedRate = 60000)
    public void localToStorage() {
        storageWrapper.uploadLocalFilesToStorage();
        LOGGER.log(Level.INFO, "Upload from local to storage finished at {0}", dateFormat.format(new Date()));
    }

//    @Scheduled(fixedRate = 3600000)
    @Scheduled(fixedRate = 60000)
    public void storageToQuery() {
        for (String storageFileToProces : storageWrapper.listFilesByFileNamePattern()) {
            bigQueryWrapper.runExternalTableMergeQuery(storageFileToProces);
        }
        LOGGER.log(Level.INFO, "Upload from storage to query finished at {0}", dateFormat.format(new Date()));
    }
}

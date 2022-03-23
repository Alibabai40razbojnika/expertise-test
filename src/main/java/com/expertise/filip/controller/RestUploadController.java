package com.expertise.filip.controller;

import com.expertise.filip.wrapper.BigQueryWrapper;
import com.expertise.filip.wrapper.StorageWrapper;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/upload")
public class RestUploadController {

    private static final Logger LOGGER = Logger.getLogger(RestUploadController.class.getName());
    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("dd.mm.yyyy HH:MM:ss");

    @Autowired
    private BigQueryWrapper bigQueryWrapper;

    @Autowired
    private StorageWrapper storageWrapper;

    @PostMapping("/local/to/storage")
    public String putFilesFromLocalToGoogleStorage() {
        String message = storageWrapper.uploadLocalFilesToStorage();
        LOGGER.log(Level.INFO, message);
        return message;
    }

    @PostMapping("/storage/to/query")
    public void storageToQuery() {
        for (String storageFileToProces : storageWrapper.listFilesByFileNamePattern()) {
            bigQueryWrapper.runExternalTableMergeQuery(storageFileToProces);
        }
        LOGGER.log(Level.INFO, "Upload from storage to query finished at {0}", SIMPLE_DATE_FORMAT.format(new Date()));
    }

}

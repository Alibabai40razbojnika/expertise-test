package com.expertise.filip.controller;

import com.expertise.filip.wrapper.BigQueryWrapper;
import com.expertise.filip.wrapper.StorageWrapper;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/setup")
public class RestSetupController {

    private static final Logger LOGGER = Logger.getLogger(RestSetupController.class.getName());

    //<editor-fold defaultstate="collapsed" desc="Wrappers">
    @Autowired
    private BigQueryWrapper bigQueryWrapper;

    @Autowired
    private StorageWrapper storageWrapper;
    //</editor-fold>

    /**
     * Creating table in Google Cloud BigQuery
     *
     * @return String data set created or not 
     */
    @PostMapping("/bigquery/dataset")
    public String getSetUpDatasetResult() {
        return bigQueryWrapper.runCreateDataset();
    }

    /**
     * Creating table in Google Cloud BigQuery
     *
     * @return String table created or not
     */
    @PostMapping("/bigquery/formatedtable")
    public String getSetUpFormatedTableResult() {
        return bigQueryWrapper.runCreateFormatedTable();
    }

    /**
     * Creating bucket in Google Cloud Storage
     *
     * @return String bucket created or not
     */
    @PostMapping("/storage/bucket")
    public String getCreateBucketResult() {
        return storageWrapper.createBucket();
    }
}

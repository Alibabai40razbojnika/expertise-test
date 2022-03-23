package com.expertise.filip.wrapper;

import com.expertise.filip.dto.TractorFormatedDto;
import com.expertise.filip.util.CustomProperties;
import com.expertise.filip.util.QueryUtils;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQueryException;
import com.google.cloud.bigquery.BigQueryOptions;
import com.google.cloud.bigquery.JobException;
import com.google.cloud.bigquery.QueryJobConfiguration;
import com.google.cloud.bigquery.TableResult;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BigQueryWrapper {

    private static final Logger LOGGER = Logger.getLogger(BigQueryWrapper.class.getName());

    @Autowired
    private CustomProperties properties;

    @Autowired
    private QueryUtils queryUtils;

    private BigQuery bigQuery;

    @PostConstruct
    public void init() {
        GoogleCredentials credentials;
        try (FileInputStream serviceAccountStream = new FileInputStream(properties.getCredentialsPath())) {
            credentials = ServiceAccountCredentials.fromStream(serviceAccountStream);
            bigQuery = BigQueryOptions.newBuilder()
                    .setCredentials(credentials)
                    .setProjectId(properties.getProjectId())
                    .setLocation(properties.getLocation())
                    .build()
                    .getService();
        } catch (FileNotFoundException ex) {
            LOGGER.log(Level.WARNING, "FileNotFoundException", ex);
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "IOException", ex);
        }

    }

    /**
     * Dataset creation
     *
     * @return message containing result
     */
    public String runCreateDataset() {
        try {
            LOGGER.log(Level.INFO, "Dataset was created {0}.", bigQuery.create(queryUtils.createDataset()).getDatasetId().getDataset());
            return "Dataset successfully created";
        } catch (BigQueryException | NullPointerException ex) {
            LOGGER.log(Level.WARNING, "Dataset was not created.", ex);
            return ex.getMessage();
        }
    }

    /**
     * Table creation
     *
     * @return message containing result
     */
    public String runCreateFormatedTable() {
        try {
            LOGGER.log(Level.INFO, "Table was created {0}.", bigQuery.create(queryUtils.createFormatedTable()).getTableId().getTable());
            return "Table successfully created";
        } catch (BigQueryException | NullPointerException ex) {
            LOGGER.log(Level.WARNING, "Table was not created.", ex);
            return ex.getMessage();
        }
    }

    /**
     * Merge new data with data already stored in table, new data is inserted,
     * old data is updated
     *
     * @param fileName name of the file containing values for merge
     */
    public void runExternalTableMergeQuery(String fileName) {
        LOGGER.log(Level.INFO, "Query execution started on {0}.", fileName);
        try {
            bigQuery.query(queryUtils.executeMergeQuery(fileName));
            LOGGER.log(Level.INFO, "Query executed.");
        } catch (BigQueryException | NullPointerException | InterruptedException | JobException ex) {
            LOGGER.log(Level.WARNING, "Query on file " + fileName + " was not executed!", ex);
        }
    }

    /**
     * Query table with two parameters as a composite primary key
     *
     * @param serialNumber serial number of tractor
     * @param date date for which information is requested
     * @return List containing single tractor or no tractor value
     */
    public List<TractorFormatedDto> runSelectQuery(String serialNumber, String date) {
        List<TractorFormatedDto> formatedTractorData = new ArrayList();
        try {

            String query = queryUtils.createSelectQuery(serialNumber, date);

            QueryJobConfiguration queryConfig = QueryJobConfiguration.newBuilder(query).build();
            TableResult results = bigQuery.query(queryConfig);

            // values into TractorFormatDto
            results.iterateAll().forEach(fieldValueList -> formatedTractorData.add(new TractorFormatedDto(fieldValueList)));
            LOGGER.log(Level.INFO, "Query performed successfully.");
        } catch (BigQueryException | InterruptedException e) {
            LOGGER.log(Level.WARNING, "Query not performed successfully.", e);
        }
        return formatedTractorData;
    }
}

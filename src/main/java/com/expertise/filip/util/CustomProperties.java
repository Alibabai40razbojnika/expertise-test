package com.expertise.filip.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:custom.properties")
public class CustomProperties {

    @Value("${bigquery.projectId}")
    private String projectId;

    @Value("${bigquery.datasetName}")
    private String datasetName;

    @Value("${bigquery.mainTableName}")
    private String mainTableName;

    @Value("${bigquery.formatedTableName}")
    private String formatedTableName;

    @Value("${bigquery.field.delimiter}")
    private String fieldDelimiter;

    @Value("${google.credentials.path}")
    private String credentialsPath;

    @Value("${google.maps.api.key}")
    private String mapsApiKey;

    @Value("${local.unprocessed.file.path}")
    private String localUnprocessedFilePath;

    @Value("${local.processed.file.path}")
    private String localProcessedFilePath;

    @Value("${storage.bucketName}")
    private String bucketName;

    @Value("${storage.location}")
    private String location;
    
    @Value("${file.format.regular.expression}")
    private String fileFormatRegExp;

    public String getProjectId() {
        return projectId;
    }

    public String getDatasetName() {
        return datasetName;
    }

    public String getMainTableName() {
        return mainTableName;
    }

    public String getFormatedTableName() {
        return formatedTableName;
    }

    public String getFieldDelimiter() {
        return fieldDelimiter;
    }

    public String getCredentialsPath() {
        return credentialsPath;
    }

    public String getMapsApiKey() {
        return mapsApiKey;
    }

    public String getLocalUnprocessedFilePath() {
        return localUnprocessedFilePath;
    }

    public String getLocalProcessedFilePath() {
        return localProcessedFilePath;
    }

    public String getBucketName() {
        return bucketName;
    }

    public String getLocation() {
        return location;
    }

    public String getFileFormatRegExp() {
        return fileFormatRegExp;
    }

}

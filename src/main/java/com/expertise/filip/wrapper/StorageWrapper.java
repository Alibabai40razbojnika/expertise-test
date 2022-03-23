package com.expertise.filip.wrapper;

import com.expertise.filip.util.CustomProperties;
import com.google.api.gax.paging.Page;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.BucketInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageClass;
import com.google.cloud.storage.StorageOptions;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Storage.BlobListOption;
import com.google.cloud.storage.StorageException;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StorageWrapper {

    private static final Logger LOGGER = Logger.getLogger(StorageWrapper.class.getName());

    @Autowired
    private CustomProperties properties;

    private Storage storage;

    /**
     * Initialization of storage class for future use, require correct
     * credentials file
     */
    @PostConstruct
    public void init() {
        GoogleCredentials credentials;
        try (FileInputStream serviceAccountStream = new FileInputStream(properties.getCredentialsPath())) {
            credentials = ServiceAccountCredentials.fromStream(serviceAccountStream);

            storage = StorageOptions.newBuilder()
                    .setCredentials(credentials)
                    .setProjectId(properties.getProjectId())
                    .build()
                    .getService();
        } catch (FileNotFoundException ex) {
            LOGGER.log(Level.WARNING, "File containg credentials not found", ex);
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "IO", ex);
        }

    }

    /**
     * Creation of the bucket on Google Cloud Storage
     *
     * @return indicator that bucket is created (true) or not (false)
     */
    public String createBucket() {
        try {
            Bucket bucket = storage.create(
                    BucketInfo.newBuilder(properties.getBucketName())
                            .setStorageClass(StorageClass.STANDARD)
                            .setLocation(properties.getLocation())
                            .build());
            LOGGER.log(Level.INFO, "Created bucket ({0}) in ({1}) with storage class ({2})", new String[]{bucket.getName(), bucket.getLocation(), bucket.getStorageClass().toString()});
            return "Bucket successfully created.";
        } catch (StorageException ex) {
            LOGGER.log(Level.WARNING, "Storage", ex);
            return ex.getMessage();
        }
    }

    /**
     * Local storage files upload to Google Cloud Storage, uploaded local files
     * are moved to backup folder
     *
     * @return message containing result of upload
     */
    public String uploadLocalFilesToStorage() {
        File directory = new File(properties.getLocalUnprocessedFilePath());
        FileFilter filter = new RegexFileFilter(properties.getFileFormatRegExp());
        File[] files = directory.listFiles(filter);
        boolean allFilesUploaded = true;
        for (File file : files) {
            if (uploadObject(file.getName())) {
                //
                if (file.renameTo(new File(properties.getLocalProcessedFilePath() + file.getName()))) {
                    LOGGER.log(Level.INFO, "{0} successfully moved to {1}", new Object[]{file.getName(), properties.getLocalProcessedFilePath()});
                } else {
                    LOGGER.log(Level.WARNING, "{0} failed to move to {1}", new Object[]{file.getName(), properties.getLocalProcessedFilePath()});
                    file.delete();
                }
                LOGGER.log(Level.INFO, "{0} uploaded to google storage.", file.getName());
            } else {
                LOGGER.log(Level.WARNING, "{0} failed to upload to google storage.", file.getName());
                allFilesUploaded = false;
            }
        }
        return allFilesUploaded ? "All files are uploaded to Google Cloud Storage." : "Not all files are uploaded, check logs!";
    }



    /**
     * File upload to Google Cloud Storage
     *
     * @param fileName - local file name to be uploaded
     */
    private boolean uploadObject(String fileName) {
        boolean success;
        BlobId blobId = BlobId.of(properties.getBucketName(), fileName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
        try {
            storage.create(blobInfo, Files.readAllBytes(Paths.get(properties.getLocalUnprocessedFilePath() + fileName)));
            LOGGER.log(Level.INFO, "File {0} uploaded to bucket {1} as {2}.", new String[]{properties.getLocalUnprocessedFilePath(), properties.getBucketName(), fileName});
            success = true;
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "IOException with file " + fileName, ex);
            success = false;
        }

        return success;
    }

    /**
     * Filter files by name in storage bucket by prepared criteria
     *
     * @return List of file names stored in bucket filtered by criteria
     */
    public List<String> listFilesByFileNamePattern() {
        return listFilesByFileNamePattern(Pattern.compile(properties.getFileFormatRegExp()));
    }

    /**
     * Filter files by name in storage bucket by prepared criteria
     *
     * @param matchPattern criteria for file name filtering
     * @return List of file names stored in bucket filtered by criteria
     */
    private List<String> listFilesByFileNamePattern(Pattern matchPattern) {
        List<String> results = new ArrayList<>();

        Page<Blob> blobs = storage.list(properties.getBucketName(), BlobListOption.currentDirectory());
        for (Blob blob : blobs.iterateAll()) {
            if (!blob.isDirectory() && matchPattern.matcher(blob.getName()).matches()) {
                results.add(blob.getName());
            }
        }
        return results;
    }

    public boolean moveObjectToOtherBucket(String sourceBucket, String targetBucket, String fileName) {
        Blob blob = storage.get(sourceBucket, fileName);
        blob.copyTo(targetBucket, fileName);
        return blob.delete();
    }

    /**
     * Idea is to generate file name for today and upload only todays files
     *
     * @return LD_SERIALNUMBER_TODAY_TOMORROW.csv
     */
    private String generateTodaysFileName() {
        return new StringBuilder("LD_A[0-9]+_").append(createDatePart()).toString();
    }

    /**
     * Idea is to generate todays-tomorrows part of file name
     *
     * @return date part of todays file name
     */
    private String createDatePart() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMdd");
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime tomorrow = LocalDateTime.now().plusDays(1);
        return dtf.format(now) + "_" + dtf.format(tomorrow) + ".csv";
    }
}

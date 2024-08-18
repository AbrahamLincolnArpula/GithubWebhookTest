package com.github.githubWebhookDataS3.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class S3Service {

    private final S3Client s3Client;
    private static final Logger logger = LoggerFactory.getLogger(S3Service.class);

    @Autowired
    public S3Service(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public void saveEventData(String eventType, String eventData) throws Exception {
        String bucketName = "githubbucketgrafana";
        String date = LocalDate.now().toString(); // Get current date in YYYY-MM-DD format
        String key = "github-events/" + eventType + "/" + date + ".json";

        // Retrieve existing content if the file exists, otherwise start new data
        String updatedData;
        if (doesObjectExist(bucketName, key)) {
            String existingData = getObjectContent(bucketName, key);
            updatedData = existingData + "," + eventData;  // Append new event data with a comma
        } else {
            updatedData = eventData;  // Start with the new event data
        }

        // Put the updated content back into S3
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromBytes(updatedData.getBytes(StandardCharsets.UTF_8)));
    }

    public String retrieveEventData(String eventType) throws Exception {
        String bucketName = "githubbucketgrafana";
        String prefix = "github-events/" + eventType + "/";

        // List all objects under the event type folder
        ListObjectsV2Request listObjectsRequest = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .prefix(prefix)
                .build();
        
        ListObjectsV2Response listObjectsResponse = s3Client.listObjectsV2(listObjectsRequest);

        // Retrieve content from all files asynchronously
        List<CompletableFuture<String>> futures = listObjectsResponse.contents().stream()
                .map(s3Object -> CompletableFuture.supplyAsync(() -> getObjectContent(bucketName, s3Object.key())))
                .collect(Collectors.toList());

        // Join all contents from different files into one, separated by commas
        String aggregatedData = futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.joining(","));

        // Enclose the aggregated data in an array and return it with the eventType as the key
        return "{\"" + eventType + "\":[" + aggregatedData + "]}";
    }

    private String getObjectContent(String bucketName, String key) {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();
            return s3Client.getObjectAsBytes(getObjectRequest).asUtf8String();
        } catch (NoSuchKeyException e) {
            logger.error("Object not found: " + key, e);
            return ""; // Return empty string if object is not found
        } catch (S3Exception e) {
            logger.error("Error retrieving object from S3: " + key, e);
            throw e;
        }
    }

    private boolean doesObjectExist(String bucketName, String key) {
        try {
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();
            s3Client.headObject(headObjectRequest);
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        }
    }
}

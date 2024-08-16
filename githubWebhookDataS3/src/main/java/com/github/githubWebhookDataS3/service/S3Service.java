package com.github.githubWebhookDataS3.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class S3Service {

	
    private final S3Client s3Client;

    @Autowired
    public S3Service(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public void saveEventData(String eventType, String eventData) throws Exception {
        String bucketName = "githubbucketgrafana";
        String key = "github-events/" + eventType + "/" + System.currentTimeMillis() + ".json";

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromBytes(eventData.getBytes(StandardCharsets.UTF_8)));
    }

    public String retrieveEventData(String eventType) throws Exception {
        String bucketName = "githubbucketgrafana";
        String prefix = "github-events/" + eventType + "/";
        
        ListObjectsV2Request listObjectsRequest = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .prefix(prefix)
                .build();
        
        ListObjectsV2Response listObjectsResponse = s3Client.listObjectsV2(listObjectsRequest);
        
        List<CompletableFuture<String>> futures = listObjectsResponse.contents().stream()
                .map(s3Object -> CompletableFuture.supplyAsync(() -> getObjectContent(bucketName, s3Object.key())))
                .collect(Collectors.toList());

        String aggregatedData = futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.joining(","));
        
        return "{\"" + eventType + "\":[" + aggregatedData + "]}";
    }

    private String getObjectContent(String bucketName, String key) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();
        return s3Client.getObjectAsBytes(getObjectRequest).asUtf8String();
    }
}

package com.github.githubWebhookDataS3.service;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.concurrent.CompletableFuture;

@Service
public class GitHubEventService {

    private final S3Service s3Service;
    private final ObjectMapper objectMapper;

    @Autowired
    public GitHubEventService(S3Service s3Service, ObjectMapper objectMapper) {
        this.s3Service = s3Service;
        this.objectMapper = objectMapper;
    }

    @Async
    @CacheEvict(value = "githubEvents", key = "#eventType")
    public CompletableFuture<Void> processGitHubEvent(String eventType, String eventData) throws Exception {
        if (!StringUtils.hasText(eventType) || !StringUtils.hasText(eventData)) {
            throw new IllegalArgumentException("Invalid event type or data.");
        }
        s3Service.saveEventData(eventType, eventData);
        return CompletableFuture.completedFuture(null);
    }

    @Cacheable(value = "githubEvents", key = "#eventType")
    public String getEventData(String eventType) throws Exception {
        return s3Service.retrieveEventData(eventType);
    }
}

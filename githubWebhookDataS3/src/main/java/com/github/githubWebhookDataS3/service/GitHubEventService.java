package com.github.githubWebhookDataS3.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.concurrent.CompletableFuture;

@Service
public class GitHubEventService {

    private static final Logger logger = LoggerFactory.getLogger(GitHubEventService.class);

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
		/*
		 * logger.info("Processing event of type: {}", eventType);
		 * logger.info("Processing eventData: {}", eventData);
		 */

        if (!StringUtils.hasText(eventType)) {
            logger.error("Invalid event type: {}", eventType);
            throw new IllegalArgumentException("Invalid event type.");
        }

        if (!StringUtils.hasText(eventData)) {
            logger.error("Invalid event data: {}", eventData);
            throw new IllegalArgumentException("Invalid event data.");
        }

        // Save event data to S3
        s3Service.saveEventData(eventType, eventData);

        logger.info("Successfully processed event of type: {}", eventType);
        return CompletableFuture.completedFuture(null);
    }

    @Cacheable(value = "githubEvents", key = "#eventType")
    public String getEventData(String eventType) throws Exception {
        logger.info("Retrieving event data for type: {}", eventType);
        return s3Service.retrieveEventData(eventType);
    }
}

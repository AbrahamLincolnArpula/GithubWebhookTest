package com.github.githubWebhookDataS3.controller;

import com.github.githubWebhookDataS3.service.GitHubEventService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/webhook")
public class GitHubWebhookController {

    private final GitHubEventService gitHubEventService;
    private static final Logger logger = LoggerFactory.getLogger(GitHubEventService.class);

    @Autowired
    public GitHubWebhookController(GitHubEventService gitHubEventService) {
        this.gitHubEventService = gitHubEventService;
    }

    @PostMapping(value = "/github", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> handleGitHubWebhook(@RequestHeader("X-GitHub-Event") String eventType,
                                                      @RequestBody String payload) {
		/*
		 * logger.info("controller Received event: {}", eventType);
		 * logger.info("controller Payload: {}", payload);
		 */
        try {
            //String eventType = headers.get("X-GitHub-Event");
            gitHubEventService.processGitHubEvent(eventType, payload);
            return ResponseEntity.ok("Event processed successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing event: " + e.getMessage());
        }
    }

    @GetMapping(value="/data/{eventType}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getGitHubEventData(@PathVariable String eventType) {
        try {
            String data = gitHubEventService.getEventData(eventType);
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving event data: " + e.getMessage());
        }
    }
}

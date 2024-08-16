package com.github.githubWebhookDataS3.controller;

import com.github.githubWebhookDataS3.service.GitHubEventService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/webhook")
public class GitHubWebhookController {

    private final GitHubEventService gitHubEventService;

    @Autowired
    public GitHubWebhookController(GitHubEventService gitHubEventService) {
        this.gitHubEventService = gitHubEventService;
    }

    @PostMapping("/github")
    public ResponseEntity<String> handleGitHubWebhook(@RequestHeader Map<String, String> headers,
                                                      @RequestBody String payload) {
        try {
            String eventType = headers.get("X-GitHub-Event");
            gitHubEventService.processGitHubEvent(eventType, payload);
            return ResponseEntity.ok("Event processed successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing event: " + e.getMessage());
        }
    }

    @GetMapping("/data/{eventType}")
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

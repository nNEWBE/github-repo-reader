package com.example.github_repo_reader.service;

import com.example.github_repo_reader.config.GitHubProperties;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class GitHubApiService {

    private final RestTemplate restTemplate;
    private final GitHubProperties gitHubProperties;

    private static final int MAX_RETRIES = 2;
    private static final long RATE_LIMIT_WAIT_MS = 1000; 
    private static final long RATE_LIMIT_EXCEEDED_WAIT_MS = 60000; 

    public JsonNode fetchRepoInfo(String repoName) {
        String url = String.format("%s/repos/%s/%s",
                gitHubProperties.getApiBaseUrl(),
                gitHubProperties.getOwner(),
                repoName);
        return makeGetRequestWithRetry(url);
    }

    public JsonNode fetchContributors(String repoName) {
        String url = String.format("%s/repos/%s/%s/contributors",
                gitHubProperties.getApiBaseUrl(),
                gitHubProperties.getOwner(),
                repoName);
        return makeGetRequestWithRetry(url);
    }

    public JsonNode fetchContents(String path) {
        String url = String.format("%s/repos/%s/%s/contents/%s",
                gitHubProperties.getApiBaseUrl(),
                gitHubProperties.getOwner(),
                gitHubProperties.getRepo(),
                path);
        return makeGetRequestWithRetry(url);
    }

    public JsonNode fetchRootContents() {
        return fetchContents("");
    }

    public String fetchReadmeContent(String path) {
        try {
            String readmePath = path.isEmpty() ? "README.md" : path + "/README.md";
            String url = String.format("%s/repos/%s/%s/contents/%s",
                    gitHubProperties.getApiBaseUrl(),
                    gitHubProperties.getOwner(),
                    gitHubProperties.getRepo(),
                    readmePath);
            JsonNode node = makeGetRequestWithRetry(url);
            if (node != null && node.has("content")) {
                String base64Content = node.get("content").asText().replaceAll("\\s", "");
                return new String(Base64.getDecoder().decode(base64Content));
            }
        } catch (Exception e) {
            log.warn("Failed to fetch README from path: {}. Error: {}", path, e.getMessage());
        }
        return null;
    }

    public String fetchReadmeSha(String path) {
        try {
            String readmePath = path.isEmpty() ? "README.md" : path + "/README.md";
            String url = String.format("%s/repos/%s/%s/contents/%s",
                    gitHubProperties.getApiBaseUrl(),
                    gitHubProperties.getOwner(),
                    gitHubProperties.getRepo(),
                    readmePath);
            JsonNode node = makeGetRequestWithRetry(url);
            if (node != null && node.has("sha")) {
                return node.get("sha").asText();
            }
        } catch (Exception e) {
            log.warn("Failed to fetch SHA from path: {}. Error: {}", path, e.getMessage());
        }
        return null;
    }

    private JsonNode makeGetRequestWithRetry(String url) {
        for (int attempt = 0; attempt <= MAX_RETRIES; attempt++) {
            try {

                if (attempt > 0) {
                    Thread.sleep(RATE_LIMIT_EXCEEDED_WAIT_MS);
                } else {
                    Thread.sleep(RATE_LIMIT_WAIT_MS);
                }

                HttpHeaders headers = new HttpHeaders();
                headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
                headers.set("User-Agent", "github-repo-reader-app");

                HttpEntity<Void> entity = new HttpEntity<>(headers);
                ResponseEntity<JsonNode> response = restTemplate.exchange(
                        url, HttpMethod.GET, entity, JsonNode.class);

                if (response.getStatusCode().is2xxSuccessful()) {
                    logRateLimit(response.getHeaders());
                    return response.getBody();
                }
            } catch (HttpClientErrorException e) {
                if (e.getStatusCode() == HttpStatus.FORBIDDEN || e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                    log.warn("Rate limit hit for URL: {}. Attempt {}/{}. Waiting...",
                            url, attempt + 1, MAX_RETRIES + 1);
                    try {
                        Thread.sleep(RATE_LIMIT_EXCEEDED_WAIT_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return null;
                    }
                } else {
                    log.error("GitHub API error for URL: {}. Status: {}. Error: {}",
                            url, e.getStatusCode(), e.getMessage());
                    return null;
                }
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                return null;
            } catch (Exception e) {
                log.error("GitHub API request failed for URL: {}. Error: {}", url, e.getMessage());
                return null;
            }
        }
        log.error("All retry attempts exhausted for URL: {}", url);
        return null;
    }

    private void logRateLimit(HttpHeaders headers) {
        String remaining = headers.getFirst("X-RateLimit-Remaining");
        String limit = headers.getFirst("X-RateLimit-Limit");
        if (remaining != null && limit != null) {
            int remainingCount = Integer.parseInt(remaining);
            if (remainingCount < 10) {
                log.warn("GitHub API rate limit low: {}/{} remaining", remaining, limit);
            }
        }
    }
}

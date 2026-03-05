package com.example.github_repo_reader.service;

import com.example.github_repo_reader.config.GitHubProperties;
import com.example.github_repo_reader.entity.Contributor;
import com.example.github_repo_reader.entity.Repository;
import com.example.github_repo_reader.entity.SubTopic;
import com.example.github_repo_reader.entity.Topic;
import com.example.github_repo_reader.repository.RepoRepository;
import com.example.github_repo_reader.repository.SubTopicRepository;
import com.example.github_repo_reader.repository.TopicRepository;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class GitHubSyncService {

    private final GitHubApiService gitHubApiService;
    private final GitHubProperties gitHubProperties;
    private final RepoRepository repoRepository;
    private final TopicRepository topicRepository;
    private final SubTopicRepository subTopicRepository;

    private static final LinkedHashMap<String, String> TOPIC_MAPPING = new LinkedHashMap<>();

    static {
        TOPIC_MAPPING.put("datatype", "Data type");
        TOPIC_MAPPING.put("operator", "Operator");
        TOPIC_MAPPING.put("classesandobject", "Class & Object");
        TOPIC_MAPPING.put("theobjectclass", "The Object class");
        TOPIC_MAPPING.put("wrapperclass", "wrapperclass");
        TOPIC_MAPPING.put("exceptionhandling", "Exception Handling");
        TOPIC_MAPPING.put("assertion", "Assertion");
        TOPIC_MAPPING.put("string", "String");
        TOPIC_MAPPING.put("datetime", "Date-Time");
        TOPIC_MAPPING.put("formatter", "Basic formatting");
        TOPIC_MAPPING.put("regex", "Regex");
        TOPIC_MAPPING.put("array", "Array");
        TOPIC_MAPPING.put("inheritance", "Inheritance");
        TOPIC_MAPPING.put("interfaces", "Interface");
        TOPIC_MAPPING.put("enum", "Enum");
        TOPIC_MAPPING.put("java17", "What's New in java17");
        TOPIC_MAPPING.put("qna", "Common Questions");
    }

    @jakarta.annotation.PostConstruct
    public void onStartup() {
        Thread syncThread = new Thread(() -> {
            log.info("Starting initial GitHub data sync...");
            syncAllData();
            log.info("Initial GitHub data sync completed.");
        }, "github-sync-init");
        syncThread.setDaemon(true);
        syncThread.start();
    }

    @Scheduled(cron = "${app.github.sync-cron}")
    public void scheduledSync() {
        log.info("Starting scheduled GitHub data sync...");
        syncAllData();
        log.info("Scheduled GitHub data sync completed.");
    }

    public void syncAllData() {
        try {
            syncContributions();
            syncTopicsAndContent();
        } catch (Exception e) {
            log.error("Error during GitHub data sync: {}", e.getMessage(), e);
        }
    }

    @Transactional
    public void syncContributions() {
        List<String> relatedRepos = gitHubProperties.getRelatedRepos();
        if (relatedRepos == null || relatedRepos.isEmpty()) {
            relatedRepos = Collections.singletonList(gitHubProperties.getRepo());
        }

        for (String repoName : relatedRepos) {
            try {
                syncRepoContributors(repoName.trim());
            } catch (Exception e) {
                log.error("Failed to sync contributors for repo: {}. Error: {}", repoName, e.getMessage());
            }
        }
    }

    private void syncRepoContributors(String repoName) {
        JsonNode repoInfo = gitHubApiService.fetchRepoInfo(repoName);
        if (repoInfo == null) {
            log.warn("Could not fetch repo info for: {}", repoName);
            return;
        }

        JsonNode contributorsNode = gitHubApiService.fetchContributors(repoName);
        if (contributorsNode == null || !contributorsNode.isArray()) {
            log.warn("Could not fetch contributors for: {}", repoName);
            return;
        }

        Repository repo = repoRepository.findByName(repoName)
                .orElseGet(() -> Repository.builder()
                        .name(repoName)
                        .contributors(new ArrayList<>())
                        .build());

        repo.setDescription(repoInfo.has("description") && !repoInfo.get("description").isNull()
                ? repoInfo.get("description").asText()
                : null);
        repo.setUrl(repoInfo.has("html_url") ? repoInfo.get("html_url").asText() : "");
        repo.setLastSyncedAt(LocalDateTime.now());

        repo.getContributors().clear();

        for (JsonNode contributorNode : contributorsNode) {
            Contributor contributor = Contributor.builder()
                    .userName(contributorNode.get("login").asText())
                    .contributionCount(contributorNode.get("contributions").asInt())
                    .profileUrl(contributorNode.get("avatar_url").asText())
                    .repository(repo)
                    .build();
            repo.getContributors().add(contributor);
        }

        repoRepository.save(repo);
        log.info("Synced contributors for repo: {} ({} contributors)",
                repoName, repo.getContributors().size());
    }

    @Transactional
    public void syncTopicsAndContent() {
        int order = 0;
        for (Map.Entry<String, String> entry : TOPIC_MAPPING.entrySet()) {
            String folderName = entry.getKey();
            String topicName = entry.getValue();

            try {
                syncTopic(folderName, topicName, order);
            } catch (Exception e) {
                log.error("Failed to sync topic: {} ({}). Error: {}",
                        topicName, folderName, e.getMessage());
            }
            order++;
        }
    }

    private void syncTopic(String folderName, String topicName, int displayOrder) {
        Topic topic = topicRepository.findByFolderName(folderName)
                .orElseGet(() -> Topic.builder()
                        .folderName(folderName)
                        .subTopics(new ArrayList<>())
                        .build());

        topic.setTopicName(topicName);
        topic.setDisplayOrder(displayOrder);

        JsonNode contents = gitHubApiService.fetchContents(folderName);
        if (contents == null) {
            log.warn("Could not fetch contents for topic folder: {}", folderName);
            topicRepository.save(topic);
            return;
        }

        List<String> subTopicPaths = new ArrayList<>();

        if (contents.isArray()) {
            boolean hasPartDirs = false;
            for (JsonNode item : contents) {
                String name = item.get("name").asText();
                String type = item.get("type").asText();
                if ("dir".equals(type) && name.startsWith("part")) {
                    hasPartDirs = true;
                    subTopicPaths.add(folderName + "/" + name);
                }
            }

            if (!hasPartDirs) {
                subTopicPaths.add(folderName);
            }
        }

        Map<String, SubTopic> existingByPath = new HashMap<>();
        for (SubTopic st : topic.getSubTopics()) {
            existingByPath.put(st.getRepoPath(), st);
        }

        Set<String> currentPaths = new HashSet<>(subTopicPaths);
        for (String path : subTopicPaths) {
            String subTopicName = deriveSubTopicName(path, topicName);

            SubTopic subTopic = existingByPath.get(path);
            if (subTopic == null) {
                subTopic = SubTopic.builder()
                        .repoPath(path)
                        .subTopicName(subTopicName)
                        .topic(topic)
                        .build();
                topic.getSubTopics().add(subTopic);
            } else {
                subTopic.setSubTopicName(subTopicName);
            }

            fetchAndUpdateContent(subTopic, path, topicName);
        }

        topic.getSubTopics().removeIf(st -> !currentPaths.contains(st.getRepoPath()));

        topicRepository.save(topic);
        log.info("Synced topic: {} with {} sub-topics", topicName, topic.getSubTopics().size());
    }

    private void fetchAndUpdateContent(SubTopic subTopic, String path, String topicName) {
        String readmePath = path + "/README.md";
        if (!path.contains("/")) {

            readmePath = path + "/README.md";
        }

        JsonNode readmeNode = gitHubApiService.fetchContents(readmePath);
        if (readmeNode == null || !readmeNode.has("sha")) {
            log.warn("Could not fetch README for: {}", readmePath);
            return;
        }

        String newSha = readmeNode.get("sha").asText();

        if (!newSha.equals(subTopic.getLastSha())) {
            if (readmeNode.has("content")) {
                try {
                    String base64Content = readmeNode.get("content").asText().replaceAll("\\s", "");
                    String content = new String(Base64.getDecoder().decode(base64Content));
                    subTopic.setContent(content);
                    subTopic.setLastSha(newSha);
                    log.info("Updated content for: {}/{}", topicName, subTopic.getSubTopicName());
                } catch (Exception e) {
                    log.error("Failed to decode README content for: {}. Error: {}", readmePath, e.getMessage());
                }
            }
        } else {
            log.debug("Content unchanged for: {}/{}", topicName, subTopic.getSubTopicName());
        }
    }

    private String deriveSubTopicName(String path, String topicName) {
        if (path.contains("/")) {
            String[] parts = path.split("/");
            return parts[parts.length - 1];
        }
        return topicName;
    }
}

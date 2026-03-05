package com.example.github_repo_reader.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "app.github")
@Getter
@Setter
public class GitHubProperties {

    private String owner;
    private String repo;
    private String apiBaseUrl;
    private String syncCron;
    private List<String> relatedRepos;
}

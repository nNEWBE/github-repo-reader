package com.example.github_repo_reader;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class GithubRepoReaderApplication {

	public static void main(String[] args) {
		SpringApplication.run(GithubRepoReaderApplication.class, args);
	}

}

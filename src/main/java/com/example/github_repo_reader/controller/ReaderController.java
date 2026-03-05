package com.example.github_repo_reader.controller;

import com.example.github_repo_reader.dto.*;
import com.example.github_repo_reader.service.ReaderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ReaderController {

    private final ReaderService readerService;

    @GetMapping("/read_contributions")
    public ResponseEntity<ApiResponse<List<RepoContributionDto>>> readContributions() {
        List<RepoContributionDto> contributions = readerService.readContributions();
        return ResponseEntity.ok(ApiResponse.success(contributions, "Read successful"));
    }

    @GetMapping("/read_indices")
    public ResponseEntity<ApiResponse<List<TopicIndexDto>>> readIndices() {
        List<TopicIndexDto> indices = readerService.readIndices();
        return ResponseEntity.ok(ApiResponse.success(indices, "Read successful"));
    }

    @GetMapping("/read_blog")
    public ResponseEntity<ApiResponse<BlogDto>> readBlog(
            @RequestParam("topic_name") String topicName,
            @RequestParam("sub_topic_name") String subTopicName) {

        return readerService.readBlog(topicName, subTopicName)
                .map(blog -> ResponseEntity.ok(ApiResponse.success(blog, "Read successful")))
                .orElseGet(() -> ResponseEntity.ok(
                        ApiResponse.error("Blog not found for topic: " + topicName
                                + ", sub_topic: " + subTopicName)));
    }
}

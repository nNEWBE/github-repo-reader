package com.example.github_repo_reader.service;

import com.example.github_repo_reader.dto.*;
import com.example.github_repo_reader.entity.Repository;
import com.example.github_repo_reader.entity.SubTopic;
import com.example.github_repo_reader.entity.Topic;
import com.example.github_repo_reader.repository.RepoRepository;
import com.example.github_repo_reader.repository.SubTopicRepository;
import com.example.github_repo_reader.repository.TopicRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReaderService {

    private final RepoRepository repoRepository;
    private final TopicRepository topicRepository;
    private final SubTopicRepository subTopicRepository;

    @Transactional(readOnly = true)
    public List<RepoContributionDto> readContributions() {
        List<Repository> repos = repoRepository.findAllWithContributors();

        return repos.stream().map(repo -> RepoContributionDto.builder()
                .name(repo.getName())
                .description(repo.getDescription())
                .url(repo.getUrl())
                .contribution(repo.getContributors().stream()
                        .map(c -> ContributorDto.builder()
                                .userName(c.getUserName())
                                .contributionCount(c.getContributionCount())
                                .profileUrl(c.getProfileUrl())
                                .build())
                        .collect(Collectors.toList()))
                .build()
        ).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TopicIndexDto> readIndices() {
        List<Topic> topics = topicRepository.findAllWithSubTopicsOrdered();

        return topics.stream().map(topic -> TopicIndexDto.builder()
                .topicName(topic.getTopicName())
                .noOfSubTopics(topic.getSubTopics().size())
                .subTopicList(topic.getSubTopics().stream()
                        .map(st -> SubTopicDto.builder()
                                .subTopicName(st.getSubTopicName())
                                .build())
                        .collect(Collectors.toList()))
                .build()
        ).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<BlogDto> readBlog(String topicName, String subTopicName) {
        Optional<SubTopic> subTopic = subTopicRepository
                .findByTopicNameAndSubTopicName(topicName, subTopicName);

        return subTopic.map(st -> BlogDto.builder()
                .topicName(st.getTopic().getTopicName())
                .subTopicName(st.getSubTopicName())
                .content(st.getContent())
                .build());
    }
}

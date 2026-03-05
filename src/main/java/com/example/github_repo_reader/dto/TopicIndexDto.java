package com.example.github_repo_reader.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TopicIndexDto {

    @JsonProperty("topic_name")
    private String topicName;

    @JsonProperty("no_of_sub_topics")
    private int noOfSubTopics;

    @JsonProperty("subTopicList")
    private List<SubTopicDto> subTopicList;
}

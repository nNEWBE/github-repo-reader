package com.example.github_repo_reader.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlogDto {

    @JsonProperty("topic_name")
    private String topicName;

    @JsonProperty("sub_topic_name")
    private String subTopicName;

    private String content;
}

package com.example.github_repo_reader.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubTopicDto {

    @JsonProperty("sub_topic_name")
    private String subTopicName;
}

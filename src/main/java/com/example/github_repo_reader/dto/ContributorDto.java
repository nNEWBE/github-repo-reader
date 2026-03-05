package com.example.github_repo_reader.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContributorDto {

    @JsonProperty("user_name")
    private String userName;

    @JsonProperty("contribution_count")
    private int contributionCount;

    @JsonProperty("profile_url")
    private String profileUrl;
}

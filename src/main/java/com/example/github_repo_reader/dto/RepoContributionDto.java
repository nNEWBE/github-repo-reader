package com.example.github_repo_reader.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RepoContributionDto {

    private String name;
    private String description;
    private String url;
    private List<ContributorDto> contribution;
}

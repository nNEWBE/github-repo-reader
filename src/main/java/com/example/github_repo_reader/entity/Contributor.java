package com.example.github_repo_reader.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "contributors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Contributor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String userName;

    @Column(nullable = false)
    private int contributionCount;

    @Column(nullable = false, length = 1024)
    private String profileUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repository_id", nullable = false)
    private Repository repository;
}

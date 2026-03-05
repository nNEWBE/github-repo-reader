package com.example.github_repo_reader.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "repositories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Repository {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(length = 1024)
    private String description;

    @Column(nullable = false)
    private String url;

    @OneToMany(mappedBy = "repository", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Contributor> contributors = new ArrayList<>();

    @Column(nullable = false)
    private LocalDateTime lastSyncedAt;
}

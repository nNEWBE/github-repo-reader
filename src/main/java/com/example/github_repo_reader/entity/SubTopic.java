package com.example.github_repo_reader.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "sub_topics",
        uniqueConstraints = @UniqueConstraint(columnNames = {"topic_id", "subTopicName"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubTopic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String subTopicName;

    @Column(nullable = false)
    private String repoPath;

    @Column(columnDefinition = "CLOB")
    private String content;

    @Column(length = 64)
    private String lastSha;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id", nullable = false)
    private Topic topic;
}

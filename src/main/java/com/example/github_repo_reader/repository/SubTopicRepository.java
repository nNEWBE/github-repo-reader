package com.example.github_repo_reader.repository;

import com.example.github_repo_reader.entity.SubTopic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SubTopicRepository extends JpaRepository<SubTopic, Long> {

    @Query("SELECT st FROM SubTopic st JOIN st.topic t WHERE t.topicName = :topicName AND st.subTopicName = :subTopicName")
    Optional<SubTopic> findByTopicNameAndSubTopicName(
            @Param("topicName") String topicName,
            @Param("subTopicName") String subTopicName
    );

    Optional<SubTopic> findByRepoPath(String repoPath);
}

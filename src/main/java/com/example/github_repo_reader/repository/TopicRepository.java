package com.example.github_repo_reader.repository;

import com.example.github_repo_reader.entity.Topic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TopicRepository extends JpaRepository<Topic, Long> {

    Optional<Topic> findByFolderName(String folderName);

    @Query("SELECT t FROM Topic t LEFT JOIN FETCH t.subTopics ORDER BY t.displayOrder")
    List<Topic> findAllWithSubTopicsOrdered();
}

package com.example.github_repo_reader.repository;

import com.example.github_repo_reader.entity.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface RepoRepository extends JpaRepository<Repository, Long> {

    Optional<Repository> findByName(String name);

    @Query("SELECT r FROM Repository r LEFT JOIN FETCH r.contributors ORDER BY r.name")
    List<Repository> findAllWithContributors();
}

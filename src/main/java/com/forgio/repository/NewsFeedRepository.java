package com.forgio.repository;

import com.forgio.entity.NewsFeed;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface NewsFeedRepository extends JpaRepository<NewsFeed, UUID> {
    List<NewsFeed> findByFactory_FactoryIdOrderByCreatedAtDesc(UUID factoryId);
}
package com.backend.repository;

import com.backend.entity.SocialPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SocialPostRepository extends JpaRepository<SocialPost, Long> {
    List<SocialPost> findByBusiness_IdOrderByCreatedAtDesc(Long businessId);
}

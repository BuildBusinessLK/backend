package com.backend.repository;

import com.backend.entity.ConnectedSocialAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConnectedSocialAccountRepository extends JpaRepository<ConnectedSocialAccount, Long> {
    Optional<ConnectedSocialAccount> findByBusiness_IdAndPlatform(Long businessId, String platform);
    void deleteByBusiness_IdAndPlatform(Long businessId, String platform);
}

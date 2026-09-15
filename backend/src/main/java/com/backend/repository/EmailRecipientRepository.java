package com.backend.repository;

import com.backend.domain.Sector;
import com.backend.entity.EmailRecipient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmailRecipientRepository extends JpaRepository<EmailRecipient, Long> {
    List<EmailRecipient> findByGroup_Id(Long groupId);
    List<EmailRecipient> findByGroup_GroupKey(String groupKey);
    List<EmailRecipient> findByGroup_GroupKeyIn(List<String> groupKeys);
    List<EmailRecipient> findBySector(Sector sector);
    long countByGroup_Id(Long groupId);
    long countByGroup_GroupKey(String groupKey);

    @Query("SELECT r FROM EmailRecipient r WHERE " +
           "(:groupId IS NULL OR r.group.id = :groupId) AND " +
           "(:search IS NULL OR LOWER(r.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(r.companyName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(r.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(r.district) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<EmailRecipient> searchRecipients(@Param("groupId") Long groupId, @Param("search") String search, Pageable pageable);
}

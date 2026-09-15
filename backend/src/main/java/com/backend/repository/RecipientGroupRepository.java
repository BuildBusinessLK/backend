package com.backend.repository;

import com.backend.domain.RecipientGroupType;
import com.backend.domain.Sector;
import com.backend.entity.RecipientGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecipientGroupRepository extends JpaRepository<RecipientGroup, Long> {
    Optional<RecipientGroup> findByGroupKey(String groupKey);
    List<RecipientGroup> findByType(RecipientGroupType type);
    List<RecipientGroup> findBySector(Sector sector);
    boolean existsByGroupKey(String groupKey);
}

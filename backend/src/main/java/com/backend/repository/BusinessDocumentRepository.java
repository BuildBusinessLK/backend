package com.backend.repository;

import com.backend.entity.BusinessDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BusinessDocumentRepository extends JpaRepository<BusinessDocument, Long> {

    List<BusinessDocument> findByUser_IdOrderByCreatedAtDesc(Long userId);

    Optional<BusinessDocument> findByIdAndUser_Id(Long id, Long userId);

    long countByUser_Id(Long userId);
}

package com.backend.service;

import com.backend.domain.DocumentCategory;
import com.backend.dto.document.BusinessDocumentDto;
import com.backend.entity.Business;
import com.backend.entity.BusinessDocument;
import com.backend.entity.User;
import com.backend.repository.BusinessDocumentRepository;
import com.backend.repository.BusinessRepository;
import com.backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
public class BusinessDocumentService {

    private static final Logger log = LoggerFactory.getLogger(BusinessDocumentService.class);
    private static final String UPLOAD_DIR = "uploads/business-documents";

    private final BusinessDocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final BusinessRepository businessRepository;

    public BusinessDocumentService(
            BusinessDocumentRepository documentRepository,
            UserRepository userRepository,
            BusinessRepository businessRepository) {
        this.documentRepository = documentRepository;
        this.userRepository = userRepository;
        this.businessRepository = businessRepository;
    }

    @Transactional(readOnly = true)
    public List<BusinessDocumentDto> listUserDocuments(Long userId) {
        return documentRepository.findByUser_IdOrderByCreatedAtDesc(userId).stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public BusinessDocumentDto uploadDocument(
            Long userId,
            MultipartFile file,
            DocumentCategory category,
            String description,
            Long businessId) throws IOException {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File must not be empty");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        Business business = null;
        if (businessId != null) {
            business = businessRepository.findById(businessId).orElse(null);
        } else {
            List<Business> list = businessRepository.findByOwner_Id(userId);
            if (!list.isEmpty()) {
                business = list.get(0);
            }
        }

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename() != null ? file.getOriginalFilename() : "document");
        String extension = "";
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex > 0) {
            extension = originalFilename.substring(dotIndex).toLowerCase();
        }

        Path userUploadPath = Paths.get(UPLOAD_DIR, String.valueOf(userId)).toAbsolutePath().normalize();
        Files.createDirectories(userUploadPath);

        String uniqueFileName = UUID.randomUUID() + "_" + originalFilename.replaceAll("[^a-zA-Z0-9._-]", "_");
        Path targetPath = userUploadPath.resolve(uniqueFileName).normalize();

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
        }

        String extractedText = extractPreviewText(targetPath, extension);

        BusinessDocument doc = new BusinessDocument();
        doc.setUser(user);
        doc.setBusiness(business);
        doc.setFileName(originalFilename);
        doc.setStoredPath(targetPath.toString());
        doc.setFileType(file.getContentType());
        doc.setFileSize(file.getSize());
        doc.setCategory(category != null ? category : DocumentCategory.GENERAL_NOTES);
        doc.setDescription(description != null ? description.trim() : "");
        doc.setExtractedText(extractedText);
        doc.setStatus("READY_FOR_AI");

        BusinessDocument saved = documentRepository.save(doc);
        return toDto(saved);
    }

    @Transactional(readOnly = true)
    public Resource loadFileAsResource(Long userId, Long documentId) throws MalformedURLException {
        BusinessDocument doc = documentRepository.findByIdAndUser_Id(documentId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found"));

        Path filePath = Paths.get(doc.getStoredPath()).normalize();
        Resource resource = new UrlResource(filePath.toUri());

        if (resource.exists() && resource.isReadable()) {
            return resource;
        } else {
            throw new RuntimeException("Could not read file: " + doc.getFileName());
        }
    }

    @Transactional(readOnly = true)
    public BusinessDocument getDocument(Long userId, Long documentId) {
        return documentRepository.findByIdAndUser_Id(documentId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found"));
    }

    @Transactional
    public void deleteDocument(Long userId, Long documentId) {
        BusinessDocument doc = documentRepository.findByIdAndUser_Id(documentId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found"));

        try {
            Path filePath = Paths.get(doc.getStoredPath());
            Files.deleteIfExists(filePath);
        } catch (Exception e) {
            log.warn("Could not delete physical file: {}", doc.getStoredPath(), e);
        }

        documentRepository.delete(doc);
    }

    private String extractPreviewText(Path filePath, String extension) {
        if (".txt".equals(extension) || ".csv".equals(extension) || ".log".equals(extension) || ".json".equals(extension)) {
            try {
                String content = Files.readString(filePath, StandardCharsets.UTF_8);
                if (content.length() > 3000) {
                    return content.substring(0, 3000) + "... [truncated]";
                }
                return content;
            } catch (Exception e) {
                log.info("Could not extract text preview from text file: {}", e.getMessage());
            }
        }
        return null;
    }

    private BusinessDocumentDto toDto(BusinessDocument doc) {
        BusinessDocumentDto dto = new BusinessDocumentDto();
        dto.setId(doc.getId());
        dto.setBusinessId(doc.getBusiness() != null ? doc.getBusiness().getId() : null);
        dto.setFileName(doc.getFileName());
        dto.setFileType(doc.getFileType());
        dto.setFileSize(doc.getFileSize());
        dto.setCategory(doc.getCategory());
        dto.setDescription(doc.getDescription());
        dto.setStatus(doc.getStatus());
        dto.setHasExtractedText(doc.getExtractedText() != null && !doc.getExtractedText().isBlank());
        dto.setCreatedAt(doc.getCreatedAt());
        return dto;
    }
}

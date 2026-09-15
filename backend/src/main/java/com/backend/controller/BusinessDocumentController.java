package com.backend.controller;

import com.backend.domain.DocumentCategory;
import com.backend.dto.document.BusinessDocumentDto;
import com.backend.entity.BusinessDocument;
import com.backend.security.CustomUserDetails;
import com.backend.service.BusinessDocumentService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/business-documents")
@CrossOrigin(origins = "*")
public class BusinessDocumentController {

    private final BusinessDocumentService documentService;

    public BusinessDocumentController(BusinessDocumentService documentService) {
        this.documentService = documentService;
    }

    @GetMapping
    public ResponseEntity<List<BusinessDocumentDto>> list(@AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(documentService.listUserDocuments(principal.getId()));
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> upload(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "category", required = false) String categoryStr,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "businessId", required = false) Long businessId) {

        try {
            DocumentCategory category = DocumentCategory.GENERAL_NOTES;
            if (categoryStr != null && !categoryStr.isBlank()) {
                try {
                    category = DocumentCategory.valueOf(categoryStr.trim().toUpperCase());
                } catch (IllegalArgumentException ignored) {
                }
            }

            BusinessDocumentDto dto = documentService.uploadDocument(
                    principal.getId(), file, category, description, businessId);
            return ResponseEntity.ok(dto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(Map.of("message", "Failed to store file: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable Long id) {

        try {
            Resource resource = documentService.loadFileAsResource(principal.getId(), id);
            BusinessDocument doc = documentService.getDocument(principal.getId(), id);

            String contentType = doc.getFileType();
            if (contentType == null || contentType.isBlank()) {
                contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + doc.getFileName() + "\"")
                    .body(resource);
        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable Long id) {
        try {
            documentService.deleteDocument(principal.getId(), id);
            return ResponseEntity.ok(Map.of("message", "Document deleted successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}

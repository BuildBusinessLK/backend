package com.backend.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.sql.DataSource;
import java.lang.management.ManagementFactory;
import java.sql.Connection;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class HealthController {

    private final DataSource dataSource;
    private final RestTemplate restTemplate;
    private final String aiServiceHealthUrl;

    public HealthController(
            DataSource dataSource,
            RestTemplate restTemplate,
            @Value("${ai.service.health-url:${AI_SERVICE_URL:http://localhost:8000/chat}}") String aiServiceUrl) {
        this.dataSource = dataSource;
        this.restTemplate = restTemplate;
        // If aiServiceUrl ends with /chat, replace with /health
        String base = aiServiceUrl.replaceAll("/chat$", "").replaceAll("/+$", "");
        this.aiServiceHealthUrl = base.endsWith("/health") ? base : base + "/health";
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> report = new LinkedHashMap<>();
        boolean allHealthy = true;

        // 1. System Info & Uptime
        long uptimeMs = ManagementFactory.getRuntimeMXBean().getUptime();
        report.put("service", "BuildBusinessLK Backend API");
        report.put("uptimeSeconds", uptimeMs / 1000);

        // 2. Database Check
        Map<String, Object> dbHealth = new LinkedHashMap<>();
        long dbStart = System.currentTimeMillis();
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            boolean valid = conn.isValid(2);
            long dbLatency = System.currentTimeMillis() - dbStart;
            dbHealth.put("status", valid ? "UP" : "DOWN");
            dbHealth.put("databaseProduct", conn.getMetaData().getDatabaseProductName());
            dbHealth.put("latencyMs", dbLatency);
        } catch (Exception ex) {
            allHealthy = false;
            dbHealth.put("status", "DOWN");
            dbHealth.put("error", ex.getMessage());
        }
        report.put("database", dbHealth);

        // 3. AI Microservice Check
        Map<String, Object> aiHealth = new LinkedHashMap<>();
        long aiStart = System.currentTimeMillis();
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> aiRes = restTemplate.getForObject(aiServiceHealthUrl, Map.class);
            long aiLatency = System.currentTimeMillis() - aiStart;
            aiHealth.put("status", "UP");
            aiHealth.put("url", aiServiceHealthUrl);
            aiHealth.put("latencyMs", aiLatency);
            if (aiRes != null) {
                aiHealth.putAll(aiRes);
            }
        } catch (Exception ex) {
            // AI service might be offline or sleeping on free tier
            aiHealth.put("status", "UNREACHABLE");
            aiHealth.put("url", aiServiceHealthUrl);
            aiHealth.put("notice", "AI service may be sleeping or restarting. RAG fallbacks active.");
            aiHealth.put("error", ex.getMessage());
        }
        report.put("aiService", aiHealth);

        // Overall status
        report.put("status", allHealthy ? "UP" : "DEGRADED");
        return ResponseEntity.ok(report);
    }
}

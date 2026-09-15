package com.backend.service;

import com.backend.domain.RecipientGroupType;
import com.backend.domain.Sector;
import com.backend.entity.EmailRecipient;
import com.backend.entity.RecipientGroup;
import com.backend.repository.EmailRecipientRepository;
import com.backend.repository.RecipientGroupRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Service
public class EdbRecipientImporterService {

    private static final Logger log = LoggerFactory.getLogger(EdbRecipientImporterService.class);

    private final RecipientGroupRepository recipientGroupRepository;
    private final EmailRecipientRepository emailRecipientRepository;

    public EdbRecipientImporterService(
            RecipientGroupRepository recipientGroupRepository,
            EmailRecipientRepository emailRecipientRepository) {
        this.recipientGroupRepository = recipientGroupRepository;
        this.emailRecipientRepository = emailRecipientRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        try {
            long recipientCount = emailRecipientRepository.count();
            if (recipientCount == 0) {
                log.info("No EDB exporters detected in database. Running initial EDB directory ingestion...");
                int imported = importEdbDirectory();
                log.info("Initial EDB directory ingestion completed: {} exporters imported.", imported);
            } else {
                log.info("EDB recipients already present in database (count: {}). Skipping auto-import.", recipientCount);
            }
        } catch (Exception ex) {
            log.error("Failed to automatically ingest EDB directory on startup: {}", ex.getMessage(), ex);
        }
    }

    @Transactional
    public int importEdbDirectory() {
        // 1. Ensure system groups exist
        RecipientGroup coconutGroup = getOrCreateGroup(
                "edb_coconut",
                "EDB Coconut Exporters & Traders",
                RecipientGroupType.EDB_COCONUT_EXPORTERS,
                Sector.COCONUT,
                "173 official EDB registered exporters and traders of coconut kernel, oil, milk, and activated carbon"
        );

        RecipientGroup kithulGroup = getOrCreateGroup(
                "edb_kithul",
                "EDB Kithul & Jaggery Exporters",
                RecipientGroupType.EDB_KITHUL_EXPORTERS,
                Sector.KITHUL,
                "44 official EDB registered exporters and traders of authentic Sri Lankan Kithul treacle and jaggery"
        );

        RecipientGroup palmyrahGroup = getOrCreateGroup(
                "edb_palmyrah",
                "EDB Palmyrah Exporters & Distillers",
                RecipientGroupType.EDB_PALMYRAH_EXPORTERS,
                Sector.PALMYRAH,
                "Official registered commercial exporters of northern Sri Lankan Palmyrah products and spirits"
        );

        // 2. Read file
        List<String> lines = readDirectoryLines();
        if (lines.isEmpty()) {
            log.warn("EDB exporters directory text file is empty or not found.");
            return 0;
        }

        // 3. Parse entries
        List<ParsedExporter> exporters = parseDirectory(lines);
        log.info("Parsed {} exporter profiles from EDB directory text file.", exporters.size());

        // Clear existing system exporters if re-importing
        List<RecipientGroup> targetGroups = List.of(coconutGroup, kithulGroup, palmyrahGroup);
        for (RecipientGroup group : targetGroups) {
            List<EmailRecipient> existing = emailRecipientRepository.findByGroup_Id(group.getId());
            if (!existing.isEmpty()) {
                emailRecipientRepository.deleteAll(existing);
            }
        }

        List<EmailRecipient> recipientsToSave = new ArrayList<>();
        int coconutCount = 0;
        int kithulCount = 0;
        int palmyrahCount = 0;

        for (ParsedExporter exp : exporters) {
            RecipientGroup targetGroup;
            Sector targetSector;

            if (exp.sector.equalsIgnoreCase("Kithul")) {
                targetGroup = kithulGroup;
                targetSector = Sector.KITHUL;
                kithulCount++;
            } else if (exp.sector.equalsIgnoreCase("Palmyrah")) {
                targetGroup = palmyrahGroup;
                targetSector = Sector.PALMYRAH;
                palmyrahCount++;
            } else {
                targetGroup = coconutGroup;
                targetSector = Sector.COCONUT;
                coconutCount++;
            }

            EmailRecipient recipient = new EmailRecipient();
            recipient.setGroup(targetGroup);
            recipient.setSector(targetSector);
            recipient.setCompanyName(exp.companyName);
            recipient.setName(exp.companyName + " (Export Dept)");
            recipient.setAddress(exp.address);
            recipient.setCity(exp.city);
            recipient.setDistrict(exp.district);
            recipient.setPhone(exp.phone.isBlank() ? "+94 11 230 0700" : exp.phone);
            
            // Generate safe mock email: e.g. export+haycarb.plc@buildbusinesslk.mock
            String slug = exp.companyName.toLowerCase(Locale.ROOT)
                    .replaceAll("[^a-z0-9]+", ".")
                    .replaceAll("^\\.+|\\.+$", "");
            if (slug.length() > 30) {
                slug = slug.substring(0, 30);
            }
            recipient.setEmail("export+" + slug + "@buildbusinesslk.mock");
            recipient.setRecipientType("B2B_EXPORTER");

            recipientsToSave.add(recipient);
        }

        emailRecipientRepository.saveAll(recipientsToSave);

        // Update group counts
        coconutGroup.setRecipientCount(coconutCount);
        kithulGroup.setRecipientCount(kithulCount);
        palmyrahGroup.setRecipientCount(palmyrahCount);

        recipientGroupRepository.saveAll(List.of(coconutGroup, kithulGroup, palmyrahGroup));

        log.info("EDB Import Complete: Coconut={}, Kithul={}, Palmyrah={}. Total={}",
                coconutCount, kithulCount, palmyrahCount, recipientsToSave.size());

        return recipientsToSave.size();
    }

    private RecipientGroup getOrCreateGroup(String key, String name, RecipientGroupType type, Sector sector, String description) {
        return recipientGroupRepository.findByGroupKey(key)
                .map(group -> {
                    group.setName(name);
                    group.setType(type);
                    group.setSector(sector);
                    group.setDescription(description);
                    group.setSystem(true);
                    return recipientGroupRepository.save(group);
                })
                .orElseGet(() -> recipientGroupRepository.save(
                        new RecipientGroup(key, name, type, sector, description, true)
                ));
    }

    private List<String> readDirectoryLines() {
        // Try Classpath first
        try {
            Resource resource = new ClassPathResource("data/edb_registered_exporters_directory.txt");
            if (resource.exists()) {
                try (InputStream is = resource.getInputStream();
                     BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                    return reader.lines().toList();
                }
            }
        } catch (Exception ex) {
            log.debug("Could not read from classpath: {}", ex.getMessage());
        }

        // Fallback to relative file paths
        List<String> candidatePaths = List.of(
                "data/edb_registered_exporters_directory.txt",
                "backend/backend/src/main/resources/data/edb_registered_exporters_directory.txt",
                "ai-service/data/edb_registered_exporters_directory.txt",
                "../ai-service/data/edb_registered_exporters_directory.txt",
                "../../ai-service/data/edb_registered_exporters_directory.txt"
        );

        for (String p : candidatePaths) {
            Path path = Paths.get(p);
            if (Files.exists(path)) {
                try {
                    return Files.readAllLines(path, StandardCharsets.UTF_8);
                } catch (Exception e) {
                    log.warn("Error reading from {}: {}", p, e.getMessage());
                }
            }
        }

        return List.of();
    }

    private List<ParsedExporter> parseDirectory(List<String> lines) {
        List<ParsedExporter> result = new ArrayList<>();
        String currentSector = "Coconut";
        String currentCompany = null;
        List<String> currentAddressLines = new ArrayList<>();
        String currentPhone = "";

        for (String rawLine : lines) {
            String line = rawLine.trim();
            if (line.isEmpty() || line.equals("---")) {
                if (currentCompany != null) {
                    result.add(createParsedExporter(currentCompany, currentSector, currentAddressLines, currentPhone));
                    currentCompany = null;
                    currentAddressLines.clear();
                    currentPhone = "";
                }
                continue;
            }

            if (line.startsWith("===")) {
                if (line.contains("KITHUL")) {
                    currentSector = "Kithul";
                } else if (line.contains("PALMYRAH")) {
                    currentSector = "Palmyrah";
                } else if (line.contains("COCONUT")) {
                    currentSector = "Coconut";
                }
                continue;
            }

            if (line.startsWith("Company:")) {
                if (currentCompany != null) {
                    result.add(createParsedExporter(currentCompany, currentSector, currentAddressLines, currentPhone));
                    currentCompany = null;
                    currentAddressLines.clear();
                    currentPhone = "";
                }
                // e.g. "Company: HAYCARB PLC (Sector: Coconut)"
                String comp = line.substring("Company:".length()).trim();
                int idx = comp.indexOf("(Sector:");
                if (idx > 0) {
                    comp = comp.substring(0, idx).trim();
                }
                currentCompany = comp;
                continue;
            }

            if (line.startsWith("Tel:")) {
                String tel = line.substring("Tel:".length()).replaceAll("^[:\\s]+", "").trim();
                if (!tel.isBlank()) {
                    currentPhone = tel;
                }
                continue;
            }

            if (line.startsWith("Fax:") || line.startsWith("eMail:") || line.startsWith("Web:")) {
                continue;
            }

            if (currentCompany != null) {
                currentAddressLines.add(line);
            }
        }

        if (currentCompany != null) {
            result.add(createParsedExporter(currentCompany, currentSector, currentAddressLines, currentPhone));
        }

        return result;
    }

    private ParsedExporter createParsedExporter(String company, String sector, List<String> addressLines, String phone) {
        String fullAddress = String.join(", ", addressLines);
        String detectedDistrict = detectDistrict(fullAddress);
        String detectedCity = detectCity(fullAddress, detectedDistrict);

        return new ParsedExporter(company, sector, fullAddress, detectedDistrict, detectedCity, phone);
    }

    private String detectDistrict(String address) {
        String lower = address.toLowerCase(Locale.ROOT);
        if (lower.contains("colombo") || lower.contains("nugegoda") || lower.contains("dehiwala") || lower.contains("maharagama") || lower.contains("moratuwa") || lower.contains("kotte")) {
            return "Colombo";
        }
        if (lower.contains("gampaha") || lower.contains("negombo") || lower.contains("kelaniya") || lower.contains("kadawatha") || lower.contains("ja-ela") || lower.contains("wattala") || lower.contains("divulapitiya") || lower.contains("katunayake")) {
            return "Gampaha";
        }
        if (lower.contains("kalutara") || lower.contains("panadura") || lower.contains("horana") || lower.contains("wadduwa") || lower.contains("beruwala")) {
            return "Kalutara";
        }
        if (lower.contains("kurunegala") || lower.contains("kuliyapitiya") || lower.contains("narammala") || lower.contains("giriulla") || lower.contains("pannala")) {
            return "Kurunegala";
        }
        if (lower.contains("puttalam") || lower.contains("chilaw") || lower.contains("marawila") || lower.contains("wennappuwa") || lower.contains("dankotuwa") || lower.contains("lunuwila")) {
            return "Puttalam";
        }
        if (lower.contains("kandy") || lower.contains("peradeniya") || lower.contains("gampola") || lower.contains("katugastota")) {
            return "Kandy";
        }
        if (lower.contains("matale") || lower.contains("dambulla")) {
            return "Matale";
        }
        if (lower.contains("galle") || lower.contains("karapitiya") || lower.contains("hikkaduwa") || lower.contains("ambalangoda")) {
            return "Galle";
        }
        if (lower.contains("matara") || lower.contains("mirissa") || lower.contains("weligama")) {
            return "Matara";
        }
        if (lower.contains("hambantota") || lower.contains("tangalle")) {
            return "Hambantota";
        }
        if (lower.contains("jaffna") || lower.contains("chunnakam") || lower.contains("nallur") || lower.contains("point pedro")) {
            return "Jaffna";
        }
        if (lower.contains("ratnapura") || lower.contains("balangoda") || lower.contains("pelmadulla")) {
            return "Ratnapura";
        }
        if (lower.contains("kegalle") || lower.contains("mawanella")) {
            return "Kegalle";
        }
        if (lower.contains("badulla") || lower.contains("bandarawela") || lower.contains("ella")) {
            return "Badulla";
        }
        if (lower.contains("anuradhapura")) {
            return "Anuradhapura";
        }
        return "Colombo"; // Default Sri Lankan commercial center
    }

    private String detectCity(String address, String fallbackDistrict) {
        String lower = address.toLowerCase(Locale.ROOT);
        String[] candidateCities = {
            "Colombo", "Negombo", "Divulapitiya", "Kelaniya", "Kadawatha", "Katunayake",
            "Kurunegala", "Kuliyapitiya", "Chilaw", "Wennappuwa", "Dankotuwa", "Lunuwila",
            "Panadura", "Horana", "Kalutara", "Kandy", "Peradeniya", "Galle", "Matara",
            "Jaffna", "Ratnapura", "Balangoda", "Gampaha"
        };
        for (String city : candidateCities) {
            if (lower.contains(city.toLowerCase(Locale.ROOT))) {
                return city;
            }
        }
        return fallbackDistrict;
    }

    private record ParsedExporter(
            String companyName,
            String sector,
            String address,
            String district,
            String city,
            String phone
    ) {}
}

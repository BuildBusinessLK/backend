package com.backend.config;

import com.backend.entity.KnowledgeSource;
import com.backend.repository.KnowledgeSourceRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class KnowledgeSourceDataInitializer implements ApplicationRunner {

    private final KnowledgeSourceRepository knowledgeSourceRepository;

    public KnowledgeSourceDataInitializer(KnowledgeSourceRepository knowledgeSourceRepository) {
        this.knowledgeSourceRepository = knowledgeSourceRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (knowledgeSourceRepository.count() > 0) {
            return;
        }
        List<KnowledgeSource> defaults = List.of(
                seed("COCONUT", "coconut.txt"),
                seed("KITHUL", "kithul.txt"),
                seed("PALMYRAH", "palmyrah.txt")
        );
        knowledgeSourceRepository.saveAll(defaults);
    }

    private static KnowledgeSource seed(String sector, String fileName) {
        KnowledgeSource k = new KnowledgeSource();
        k.setSector(sector);
        k.setFileName(fileName);
        k.setSourceType("TXT");
        return k;
    }
}

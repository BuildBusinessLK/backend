package com.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class BackendApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }

    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        // Connect timeout: 10 s
        factory.setConnectTimeout(10_000);
        // Read timeout: 120 s — llama3 8B on local hardware can take 30–60 s to respond
        factory.setReadTimeout(120_000);
        return new RestTemplate(factory);
    }
}

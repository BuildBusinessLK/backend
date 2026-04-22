package com.backend.controller;

import com.backend.dto.AiRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/ask")
@CrossOrigin(origins = "*")
public class AiController {

    @PostMapping
    public ResponseEntity<String> askAI(@RequestBody AiRequest request) {

        String answer = "Processed: " + request.getQuestion();

        return ResponseEntity.ok(answer);
    }

}

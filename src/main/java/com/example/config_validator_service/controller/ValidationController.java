package com.example.config_validator_service.controller;

import com.example.config_validator_service.model.ConfigRequest;
import com.example.config_validator_service.model.SchemaDefinition;
import com.example.config_validator_service.model.ValidationResult;
import com.example.config_validator_service.service.ValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class ValidationController {

    private final ValidationService validationService;

    @Autowired
    public ValidationController(ValidationService validationService) {
        this.validationService = validationService;
    }

    @PostMapping("/validate-config")
    public ResponseEntity<ValidationResult> validateConfig(@RequestBody ConfigRequest request) {
        ValidationResult result = validationService.validate(request);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/schema")
    public ResponseEntity<SchemaDefinition> getSchema() {
        return ResponseEntity.ok(validationService.getSchema());
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Service is up and running");
    }
}
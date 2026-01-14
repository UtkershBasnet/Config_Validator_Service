package com.example.config_validator_service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SchemaDefinition {
    private Map<String, FieldDefinition> fields;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FieldDefinition {
        private String type;
        private String description;
        private String constraints;
    }
}

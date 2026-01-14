package com.example.config_validator_service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConfigRequest {
    private String environment;
    private Boolean debug;
    private Integer maxConnections;
    private String adminPassword;
}
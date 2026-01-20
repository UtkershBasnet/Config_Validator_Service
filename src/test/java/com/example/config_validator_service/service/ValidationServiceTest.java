package com.example.config_validator_service.service;

import com.example.config_validator_service.model.ConfigRequest;
import com.example.config_validator_service.model.SchemaDefinition;
import com.example.config_validator_service.model.ValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ValidationServiceTest {

    private ValidationService validationService;

    @BeforeEach
    void setUp() {
        validationService = new ValidationService();
    }

    @Test
    void validate_shouldPass_whenConfigIsValid_forDev() {
        ConfigRequest request = new ConfigRequest("dev", true, 50, "SecureP@ssw0rd");
        ValidationResult result = validationService.validate(request);

        assertEquals("PASS", result.getStatus());
        assertTrue(result.getErrors().isEmpty());
    }

    @Test
    void validate_shouldPass_whenConfigIsValid_forTest() {
        ConfigRequest request = new ConfigRequest("test", true, 400, "SecureP@ssw0rd");
        ValidationResult result = validationService.validate(request);

        assertEquals("PASS", result.getStatus());
        assertTrue(result.getErrors().isEmpty());
    }

    @Test
    void validate_shouldPass_whenConfigIsValid_forProd() {
        ConfigRequest request = new ConfigRequest("prod", false, 1500, "SecureP@ssw0rd");
        ValidationResult result = validationService.validate(request);

        assertEquals("PASS", result.getStatus());
        assertTrue(result.getErrors().isEmpty());
    }

    @Test
    void validate_shouldFail_whenEnvironmentIsInvalid() {
        ConfigRequest request = new ConfigRequest("invalid_env", true, 100, "SecureP@ssw0rd");
        ValidationResult result = validationService.validate(request);

        assertEquals("FAIL", result.getStatus());
        assertTrue(result.getErrors().contains("Field 'environment' must be one of: dev, test, prod."));
    }

    @Test
    void validate_shouldFail_whenEnvironmentIsNull() {
        ConfigRequest request = new ConfigRequest(null, true, 100, "SecureP@ssw0rd");
        ValidationResult result = validationService.validate(request);

        assertEquals("FAIL", result.getStatus());
        assertTrue(result.getErrors().contains("Field 'environment' is required."));
    }

    @Test
    void validate_shouldFail_whenDebugIsEnabledInProd() {
        ConfigRequest request = new ConfigRequest("prod", true, 100, "SecureP@ssw0rd");
        ValidationResult result = validationService.validate(request);

        assertEquals("FAIL", result.getStatus());
        assertTrue(result.getErrors().contains("Debug mode must not be enabled in production."));
    }

    @Test
    void validate_shouldPass_whenDebugIsDisabledInProd() {
        ConfigRequest request = new ConfigRequest("prod", false, 100, "SecureP@ssw0rd");
        ValidationResult result = validationService.validate(request);

        assertEquals("PASS", result.getStatus());
    }

    // ---------- maxConnections env-based tests ----------

    @Test
    void validate_shouldFail_whenMaxConnectionsTooHighForDev() {
        ConfigRequest request = new ConfigRequest("dev", true, 200, "SecureP@ssw0rd");
        ValidationResult result = validationService.validate(request);

        assertEquals("FAIL", result.getStatus());
        assertTrue(result.getErrors().stream()
                .anyMatch(e -> e.contains("between 1 and 100 for environment 'dev'")));
    }

    @Test
    void validate_shouldFail_whenMaxConnectionsTooHighForTest() {
        ConfigRequest request = new ConfigRequest("test", true, 600, "SecureP@ssw0rd");
        ValidationResult result = validationService.validate(request);

        assertEquals("FAIL", result.getStatus());
        assertTrue(result.getErrors().stream()
                .anyMatch(e -> e.contains("between 1 and 500 for environment 'test'")));
    }

    @Test
    void validate_shouldFail_whenMaxConnectionsTooHighForProd() {
        ConfigRequest request = new ConfigRequest("prod", false, 2500, "SecureP@ssw0rd");
        ValidationResult result = validationService.validate(request);

        assertEquals("FAIL", result.getStatus());
        assertTrue(result.getErrors().stream()
                .anyMatch(e -> e.contains("between 1 and 2000 for environment 'prod'")));
    }

    @Test
    void validate_shouldFail_whenMaxConnectionsIsZero() {
        ConfigRequest request = new ConfigRequest("dev", true, 0, "SecureP@ssw0rd");
        ValidationResult result = validationService.validate(request);

        assertEquals("FAIL", result.getStatus());
    }

    // ---------- Password tests ----------

    @Test
    void validate_shouldFail_whenPasswordIsTooShort() {
        ConfigRequest request = new ConfigRequest("dev", true, 50, "Short1!");
        ValidationResult result = validationService.validate(request);

        assertEquals("FAIL", result.getStatus());
        assertTrue(result.getErrors().stream().anyMatch(e -> e.contains("at least 8 characters")));
    }

    @Test
    void validate_shouldFail_whenPasswordLacksMixedCase() {
        ConfigRequest request = new ConfigRequest("dev", true, 50, "nocaps1!");
        ValidationResult result = validationService.validate(request);

        assertEquals("FAIL", result.getStatus());
        assertTrue(result.getErrors().stream().anyMatch(e -> e.contains("mixed case")));
    }

    @Test
    void validate_shouldFail_whenPasswordLacksNumbers() {
        ConfigRequest request = new ConfigRequest("dev", true, 50, "NoNumbers!");
        ValidationResult result = validationService.validate(request);

        assertEquals("FAIL", result.getStatus());
        assertTrue(result.getErrors().stream().anyMatch(e -> e.contains("numbers")));
    }

    @Test
    void validate_shouldFail_whenPasswordLacksSpecialChars() {
        ConfigRequest request = new ConfigRequest("dev", true, 50, "NoSpecialChars123");
        ValidationResult result = validationService.validate(request);

        assertEquals("FAIL", result.getStatus());
        assertTrue(result.getErrors().stream().anyMatch(e -> e.contains("special characters")));
    }

    @Test
    void getSchema_shouldReturnSchemaDefinition() {
        SchemaDefinition schema = validationService.getSchema();
        assertNotNull(schema);
        assertNotNull(schema.getFields());
        assertTrue(schema.getFields().containsKey("environment"));
        assertTrue(schema.getFields().containsKey("debug"));
        assertTrue(schema.getFields().containsKey("maxConnections"));
        assertTrue(schema.getFields().containsKey("adminPassword"));
    }
}

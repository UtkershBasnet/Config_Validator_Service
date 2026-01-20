package com.example.config_validator_service.service;

import com.example.config_validator_service.model.ConfigRequest;
import com.example.config_validator_service.model.SchemaDefinition;
import com.example.config_validator_service.model.ValidationResult;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Service
public class ValidationService {

    private static final String ENV_DEV = "dev";
    private static final String ENV_TEST = "test";
    private static final String ENV_PROD = "prod";

    public ValidationResult validate(ConfigRequest request) {
        List<String> errors = new ArrayList<>();

        // 1. Validate 'environment'
        if (request.getEnvironment() == null) {
            errors.add("Field 'environment' is required.");
        } else {
            String env = request.getEnvironment();
            if (!env.equals(ENV_DEV) && !env.equals(ENV_TEST) && !env.equals(ENV_PROD)) {
                errors.add("Field 'environment' must be one of: dev, test, prod.");
            }
        }

        // 2. Validate 'debug'
        if (request.getDebug() == null) {
            // Optional defaults could be handled, but req says "Required fields must be present" (conceptually).
            // Let's assume nullable per field unless specified. 
            // "Required fields must be present" -> implies strictness.
            errors.add("Field 'debug' is required.");
        } else {
             if (request.getEnvironment() != null && request.getEnvironment().equals(ENV_PROD) && request.getDebug()) {
                 errors.add("Debug mode must not be enabled in production.");
             }
        }

        // 3. Validate 'maxConnections' (environment-based)
        if (request.getMaxConnections() == null) {
            errors.add("Field 'maxConnections' is required.");
        } else {
            Integer maxConn = request.getMaxConnections();
            String env = request.getEnvironment();

            if (env == null) {
                errors.add("Field 'environment' must be set before validating maxConnections.");
            } else {
                int min = 1;
                int maxAllowed;

                switch (env) {
                    case ENV_DEV:
                        maxAllowed = 100;
                        break;
                    case ENV_TEST:
                        maxAllowed = 500;
                        break;
                    case ENV_PROD:
                        maxAllowed = 2000;
                        break;
                    default:
                        maxAllowed = 1000; // fallback safety
                }

                if (maxConn < min || maxConn > maxAllowed) {
                    errors.add(String.format(
                        "Field 'maxConnections' must be between %d and %d for environment '%s'.",
                        min, maxAllowed, env
                    ));
                }
            }
        }


        // 4. Validate 'adminPassword'
        if (request.getAdminPassword() == null) {
            errors.add("Field 'adminPassword' is required.");
        } else {
            if (!isValidPassword(request.getAdminPassword())) {
                errors.add("Field 'adminPassword' must be at least 8 characters long and contain mixed case, numbers, and special characters.");
            }
        }

        String status = errors.isEmpty() ? "PASS" : "FAIL";
        return new ValidationResult(status, errors);
    }

    private boolean isValidPassword(String password) {
        if (password.length() < 8) return false;
        // Mixed case
        if (!Pattern.compile("[a-z]").matcher(password).find()) return false;
        if (!Pattern.compile("[A-Z]").matcher(password).find()) return false;
        // Numbers
        if (!Pattern.compile("[0-9]").matcher(password).find()) return false;
        // Special chars
        if (!Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]").matcher(password).find()) return false;
        return true;
    }

    public SchemaDefinition getSchema() {
        Map<String, SchemaDefinition.FieldDefinition> fields = new HashMap<>();
        
        fields.put("environment", new SchemaDefinition.FieldDefinition(
                "String", 
                "Execution environment identifier", 
                "One of: dev, test, prod"
        ));
        
        fields.put("debug", new SchemaDefinition.FieldDefinition(
                "Boolean",
                "Debug mode flag",
                "Must be false if environment is prod"
        ));
        
        fields.put("maxConnections", new SchemaDefinition.FieldDefinition(
                "Integer",
                "Allowed connection limit",
                "1-100 in dev, 1-500 in test, 1-2000 in prod"
        ));
        
        fields.put("adminPassword", new SchemaDefinition.FieldDefinition(
                "String",
                "Sensitive credential field",
                "Min length 8, mixed case, numbers, special characters"
        ));

        return new SchemaDefinition(fields);
    }
}

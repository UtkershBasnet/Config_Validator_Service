package com.example.config_validator_service.exception;

import com.example.config_validator_service.model.ValidationResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Collections;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ValidationResult> handleMalformedJson(HttpMessageNotReadableException ex) {
        // Return a FAIL status with a descriptive error message
        ValidationResult result = new ValidationResult("FAIL", Collections.singletonList("Malformed JSON request or invalid data types: " + ex.getMessage()));
        // Note: The user might prefer 400 Bad Request, but the Validation Output Contract says "Enumerate all rule violations in a single response" and "Clearly indicate success or failure".
        // Returning 200 OK with FAIL status fits the "Validation Result" pattern, but technically it's a client error.
        // However, usually for "Validation Service" where the input is syntactically wrong, 400 is appropriate.
        // The prompt says "Validation failures must not crash the service".
        // Let's stick to 400 for structural errors but ensure the BODY follows the contract.
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ValidationResult> handleGenericException(Exception ex) {
        ValidationResult result = new ValidationResult("FAIL", Collections.singletonList("Internal server error: " + ex.getMessage()));
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
    }
}

package dev.jacklucas.notes_api.exception;

import org.springframework.validation.FieldError;

import java.time.Instant;
import java.util.List;

public record ExceptionResponse(
        int status,
        String error,
        String message,
        String path,
        Instant timestamp,
        List<FieldError> fieldErrors
) {
}
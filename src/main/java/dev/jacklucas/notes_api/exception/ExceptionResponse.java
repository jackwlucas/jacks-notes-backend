package dev.jacklucas.notes_api.exception;

import java.time.Instant;
import java.util.List;

public record ExceptionResponse(
        int status,
        String error,
        String message,
        String path,
        Instant timestamp,
        List<FieldErrorItem> fieldErrors
) {
    // Handles field-level errors.
    public static record FieldErrorItem(String name, String message) {
    }
}
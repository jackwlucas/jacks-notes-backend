package dev.jacklucas.notes_api.exception;

import dev.jacklucas.notes_api.note.NoteNotFound;
import dev.jacklucas.notes_api.tag.TagNotFound;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // (400) Handle field level errors.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ExceptionResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        // Set the status.
        var status = HttpStatus.BAD_REQUEST;

        // Iterate over the field errors and transform them into our leaner field error item.
        var fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map((f) -> new ExceptionResponse.FieldErrorItem(f.getField(), f.getDefaultMessage()))
                .toList();

        // Build the response body.
        var exceptionResponse = new ExceptionResponse(
                status.value(),
                status.getReasonPhrase(),
                "Validation failed.",
                request.getRequestURI(),
                Instant.now(),
                fieldErrors
        );

        // Log the error and return a response.
        log.warn(exceptionResponse.toString());
        return ResponseEntity.status(status).body(exceptionResponse);
    }

    // (400) Handle malformed JSON.
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ExceptionResponse> handleMalformedJson(
            HttpMessageNotReadableException ex,
            HttpServletRequest request
    ) {
        // Set the status and get field-level errors.
        var status = HttpStatus.BAD_REQUEST;

        // Build the response body.
        var exceptionResponse = new ExceptionResponse(
                status.value(),
                status.getReasonPhrase(),
                "Malformed JSON.",
                request.getRequestURI(),
                Instant.now(),
                null
        );

        // Log the error and return a response.
        log.warn(exceptionResponse.toString());
        return ResponseEntity.status(status).body(exceptionResponse);
    }

    // Handle 404.
    @ExceptionHandler({NoteNotFound.class, TagNotFound.class})
    public ResponseEntity<ExceptionResponse> handleNotFound(RuntimeException ex, HttpServletRequest request) {
        // Set the status.
        var status = HttpStatus.NOT_FOUND;

        // Build the response body.
        var exceptionResponse = new ExceptionResponse(
                status.value(),
                status.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI(),
                Instant.now(),
                null
        );

        // Log the error and return a response.
        log.warn(exceptionResponse.toString());
        return ResponseEntity.status(status).body(exceptionResponse);
    }
}

package dev.jacklucas.notes_api.note.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record PutNoteRequest(
        @NotBlank String title,
        @NotNull String content,
        List<String> tags,
        @NotNull Boolean archived
) {
}

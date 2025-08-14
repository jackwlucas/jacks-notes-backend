package dev.jacklucas.notes_api.note.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateNoteRequest(
        @NotBlank String userId,
        @NotBlank String title,
        @NotNull String content,
        List<String> tags
) {
}
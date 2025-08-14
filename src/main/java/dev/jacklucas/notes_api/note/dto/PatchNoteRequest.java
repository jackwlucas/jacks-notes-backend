package dev.jacklucas.notes_api.note.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Optional;

public record PatchNoteRequest(
        @NotBlank String userId,
        Optional<@NotBlank String> title,
        Optional<@NotNull String> content,
        Optional<List<String>> tags,
        Optional<@NotNull Boolean> archived
) {
}

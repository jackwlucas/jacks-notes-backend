package dev.jacklucas.notes_api.tag.dto;

import jakarta.validation.constraints.NotBlank;

public record PutTagRequest(
        @NotBlank String name
) {
}

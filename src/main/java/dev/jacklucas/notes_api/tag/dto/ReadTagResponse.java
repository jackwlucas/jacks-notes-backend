package dev.jacklucas.notes_api.tag.dto;

import dev.jacklucas.notes_api.tag.Tag;

public record ReadTagResponse(
        String id,
        String name,
        String createdAt
) {
    public static ReadTagResponse from(Tag t) {
        return new ReadTagResponse(
                t.getId().toString(),
                t.getName(),
                t.getCreatedAt().toString()
        );
    }
}

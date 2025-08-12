package dev.jacklucas.notes_api.note.dto;

import dev.jacklucas.notes_api.note.Note;
import dev.jacklucas.notes_api.tag.Tag;

import java.util.List;

public record ReadNoteResponse(
        String id,
        String title,
        String content,
        boolean archived,
        List<String> tags
) {
    public static ReadNoteResponse from(Note n) {
        var tagNames = n.getTags().stream().map(Tag::getName).toList();
        return new ReadNoteResponse(
                n.getId().toString(),
                n.getTitle(),
                n.getContent(),
                n.isArchived(),
                tagNames
        );
    }
}
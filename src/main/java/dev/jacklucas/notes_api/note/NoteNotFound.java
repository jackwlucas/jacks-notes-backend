package dev.jacklucas.notes_api.note;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class NoteNotFound extends RuntimeException {
    public NoteNotFound(UUID id) {
        super("Note (%s) not found.".formatted(id));
    }
}

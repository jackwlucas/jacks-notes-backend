package dev.jacklucas.notes_api.tag;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class TagNotFound extends RuntimeException {
    public TagNotFound(UUID id) {
        super("Tag (%s) not found.".formatted(id));
    }
}

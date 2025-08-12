package dev.jacklucas.notes_api.tag;

import dev.jacklucas.notes_api.tag.dto.CreateTagRequest;
import dev.jacklucas.notes_api.tag.dto.PutTagRequest;
import dev.jacklucas.notes_api.tag.dto.ReadTagResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.Response;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/tags")
public class TagController {

    private final TagRepository tagRepository;

    public TagController(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    @PostMapping
    public ResponseEntity<ReadTagResponse> createTag(@RequestBody @Valid CreateTagRequest request) {
        // Trim the tag name.
        var tagName = request.name().trim();

        var existingTag = tagRepository.findByName(tagName).orElse(null);

        // Case that the tax exists.
        if (existingTag != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ReadTagResponse.from(existingTag));
        }

        // Otherwise, build the tag and save it in the repo.
        var newTag = tagRepository.save(Tag.builder().name(tagName).build());

        return ResponseEntity.status(HttpStatus.CREATED).body(ReadTagResponse.from(newTag));
    }

    @GetMapping
    public Page<ReadTagResponse> listTags(Pageable pageable) {
        return tagRepository.findAll(pageable).map(ReadTagResponse::from);
    }

    @GetMapping("/{id}")
    public ReadTagResponse getTagById(@PathVariable UUID id) {
        var tag = tagRepository.findById(id).orElseThrow(() -> new TagNotFound(id));
        return ReadTagResponse.from(tag);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReadTagResponse> updateTag(@PathVariable UUID id, @RequestBody @Valid PutTagRequest request) {
        log.info("PUT /api/tags/{} -> '{}'", id, request.name());

        // Find the tag that we want to update.
        var existingTag = tagRepository.findById(id).orElseThrow(() -> new TagNotFound(id));
        var trimmedName = request.name().trim();

        // Case that the new name and existing name are equal.
        if (existingTag.getName().equals(trimmedName))
            return ResponseEntity.status(HttpStatus.OK).body(ReadTagResponse.from(existingTag));

        // Case that the new name already exists in the DB.
        if (tagRepository.findByName(trimmedName).isPresent())
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ReadTagResponse.from(existingTag));

        // Update the tag's name, save it, and return a good response.
        existingTag.setName(trimmedName);
        tagRepository.save(existingTag);
        return ResponseEntity.status(HttpStatus.OK).body(ReadTagResponse.from(existingTag));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTagById(@PathVariable UUID id) {
        log.info("DELETE /api/tags/{}", id);

        // Get the tag, delete it from the DB, and return a response.
        var tag = tagRepository.findById(id).orElseThrow(() -> new TagNotFound(id));
        tagRepository.delete(tag);
        return ResponseEntity.noContent().build();
    }
}

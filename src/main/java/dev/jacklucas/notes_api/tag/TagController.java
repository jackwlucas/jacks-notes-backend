package dev.jacklucas.notes_api.tag;

import dev.jacklucas.notes_api.tag.dto.CreateTagRequest;
import dev.jacklucas.notes_api.tag.dto.PutTagRequest;
import dev.jacklucas.notes_api.tag.dto.ReadTagResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
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
    public ResponseEntity<ReadTagResponse> createTag(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody @Valid CreateTagRequest request
    ) {
        // Trim the tag name.
        String userId = jwt.getSubject();
        var tagName = request.name().trim();

        var existingTag = tagRepository.findByNameAndUserId(tagName, userId).orElse(null);

        // Case that the tax exists.
        if (existingTag != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ReadTagResponse.from(existingTag));
        }

        // Otherwise, build the tag and save it in the repo.
        var savedTag = tagRepository.save(Tag.builder().userId(userId).name(tagName).build());
        return ResponseEntity.status(HttpStatus.CREATED).body(ReadTagResponse.from(savedTag));
    }

    @GetMapping
    public Page<ReadTagResponse> listTags(
            @AuthenticationPrincipal Jwt jwt,
            Pageable pageable
    ) {
        String userId = jwt.getSubject();
        return tagRepository.findByUserId(userId, pageable).map(ReadTagResponse::from);
    }

    @GetMapping("/{id}")
    public ReadTagResponse getTagById(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id
    ) {
        final String userId = jwt.getSubject();

        var tag = tagRepository.findById(id)
                .filter(t -> t.getUserId().equals(userId))
                .orElseThrow(() -> new TagNotFound(id));

        return ReadTagResponse.from(tag);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReadTagResponse> updateTag(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id,
            @RequestBody @Valid PutTagRequest request
    ) {
        log.info("PUT /api/tags/{} -> '{}'", id, request.name());

        final String userId = jwt.getSubject();

        // Find the tag that we want to update.
        var existingTag = tagRepository.findById(id)
                .filter(t -> t.getUserId().equals(userId))
                .orElseThrow(() -> new TagNotFound(id));

        var trimmedName = request.name().trim();

        // Case that the new name and existing name are equal.
        if (existingTag.getName().equals(trimmedName))
            return ResponseEntity.status(HttpStatus.OK).body(ReadTagResponse.from(existingTag));

        // Case that the new name already exists in the DB.
        if (tagRepository.findByNameAndUserId(trimmedName, userId).isPresent())
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ReadTagResponse.from(existingTag));

        // Update the tag's name, save it, and return a good response.
        existingTag.setName(trimmedName);
        tagRepository.save(existingTag);
        return ResponseEntity.status(HttpStatus.OK).body(ReadTagResponse.from(existingTag));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTagById(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id
    ) {
        log.info("DELETE /api/tags/{}", id);

        final String userId = jwt.getSubject();

        var tag = tagRepository.findById(id)
                .filter(t -> t.getUserId().equals(userId))
                .orElseThrow(() -> new TagNotFound(id));

        tagRepository.delete(tag);
        return ResponseEntity.noContent().build();
    }
}

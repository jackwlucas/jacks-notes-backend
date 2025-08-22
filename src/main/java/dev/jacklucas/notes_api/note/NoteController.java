package dev.jacklucas.notes_api.note;

import dev.jacklucas.notes_api.note.dto.CreateNoteRequest;
import dev.jacklucas.notes_api.note.dto.PatchNoteRequest;
import dev.jacklucas.notes_api.note.dto.PutNoteRequest;
import dev.jacklucas.notes_api.note.dto.ReadNoteResponse;
import dev.jacklucas.notes_api.tag.Tag;
import dev.jacklucas.notes_api.tag.TagRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/notes")
public class NoteController {

    private final NoteRepository noteRepository;
    private final TagRepository tagRepository;

    public NoteController(NoteRepository noteRepository, TagRepository tagRepository) {
        this.noteRepository = noteRepository;
        this.tagRepository = tagRepository;
    }

    // Route handles creating new notes.
    @PostMapping
    public ResponseEntity<ReadNoteResponse> createNote(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody @Valid CreateNoteRequest request
    ) {
        // Get user.
        final String userId = jwt.getSubject();

        // Build the note and resolve tags.
        var note = Note.builder()
                .userId(userId)
                .title(request.title())
                .content(request.content())
                .archived(false)
                .build();

        note.setTags(resolveTags(request.tags(), userId));

        // Return the note.
        var saved = noteRepository.save(note);
        return ResponseEntity.status(HttpStatus.CREATED).body(ReadNoteResponse.from(saved));
    }

    // Route handles getting paginated list of notes.
    @GetMapping
    public Page<ReadNoteResponse> listNotes(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) String tag,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        final String userId = jwt.getSubject();

        var param = (tag == null) ? null : tag.trim();

        if (param != null && !param.isEmpty()) {
            return noteRepository.findByUserIdAndTags_Name(userId, param, pageable).map(ReadNoteResponse::from);
        }

        return noteRepository.findByUserId(userId, pageable).map(ReadNoteResponse::from);
    }

    // Route handles getting note by ID.
    @GetMapping("/{id}")
    public ReadNoteResponse getNoteById(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id
    ) {
        final String ownerId = jwt.getSubject();

        var note = noteRepository.findById(id)
                .filter(n -> n.getUserId().equals(ownerId))
                .orElseThrow(() -> new NoteNotFound(id));

        return ReadNoteResponse.from(note);
    }

    // Route handles updating a Note by ID.
    @Transactional
    @PutMapping("/{id}")
    public ResponseEntity<ReadNoteResponse> updateNote(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id,
            @RequestBody @Valid PutNoteRequest request
    ) {
        log.info("PUT /api/notes/{}", id);

        final String userId = jwt.getSubject();

        // Get the note that we want to update.
        var note = noteRepository.findById(id)
                .filter(n -> n.getUserId().equals(userId))
                .orElseThrow(() -> new NoteNotFound(id));

        // Update the values.
        note.setTitle(request.title().trim());
        note.setContent(request.content().trim());
        note.setArchived(request.archived());
        note.setTags(resolveTags(request.tags(), userId));

        // Handle the tags.
        var resolvedTags = resolveTags(request.tags(), userId);
        note.setTags(resolvedTags);

        // Save the note and return it.
        var saved = noteRepository.save(note);
        return ResponseEntity.status(HttpStatus.OK).body(ReadNoteResponse.from(saved));
    }

    @Transactional
    @PatchMapping("/{id}")
    public ResponseEntity<ReadNoteResponse> patchNote(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id,
            @RequestBody @Valid PatchNoteRequest request) {
        log.info("PATCH /api/notes/{}", id);

        final String userId = jwt.getSubject();

        // Get the note that we want to patch.
        var note = noteRepository.findById(id)
                .filter(n -> n.getUserId().equals(userId))
                .orElseThrow(() -> new NoteNotFound(id));

        // Update the values.
        request.title().ifPresent(note::setTitle);
        request.content().ifPresent(note::setContent);
        request.archived().ifPresent(note::setArchived);
        request.tags().ifPresent(tags -> note.setTags(resolveTags(tags, userId)));

        // Save the note and return a good response.
        var savedNote = noteRepository.save(note);
        return ResponseEntity.status(HttpStatus.OK).body(ReadNoteResponse.from(savedNote));
    }

    // Route handles deleting a note by ID.
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNoteById(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id
    ) {
        log.info("DELETE /api/notes/{}", id);

        final String userId = jwt.getSubject();

        var note = noteRepository.findById(id)
                .filter(n -> n.getUserId().equals(userId))
                .orElseThrow(() -> new NoteNotFound(id));

        noteRepository.delete(note);
        return ResponseEntity.noContent().build();
    }

    /* HELPERS */

    /*
     * Takes a list of tag names and returns the corresponding Tag entities.
     * Creates new Tag records if they don't already exist.
     */
    private Set<Tag> resolveTags(List<String> tags, String userId) {
        // Return empty set if no tags were provided.
        if (tags == null || tags.isEmpty()) {
            return Collections.emptySet();
        }

        // Remove null/blank values and trim whitespace.
        Set<String> normalizedTags = new HashSet<>();
        for (String t : tags) {
            if (!t.isBlank()) {
                String trimmed = t.trim();
                if (!trimmed.isEmpty()) {
                    normalizedTags.add(trimmed);
                }
            }
        }

        // Find or create each tag.
        Set<Tag> resolvedTags = new HashSet<>();
        for (String name : normalizedTags) {
            // Add the existing tag or build, save, and return a new tag.
            resolvedTags.add(tagRepository.findByNameAndUserId(name, userId)
                    .orElseGet(() -> {
                        var newTag = Tag.builder().name(name).userId(userId).build();
                        return tagRepository.save(newTag);
                    }));
        }

        return resolvedTags;
    }

}

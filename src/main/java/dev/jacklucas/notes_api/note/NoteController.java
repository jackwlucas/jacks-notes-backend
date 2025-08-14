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

    private String getUserId(HttpServletRequest request) {
        
    }

    // Route handles creating new notes.
    @PostMapping
    public ResponseEntity<ReadNoteResponse> createNote(@RequestBody @Valid CreateNoteRequest request) {
        // Build the note.
        var note = Note.builder()
                .ownerId(request.userId())
                .title(request.title())
                .content(request.content())
                .archived(false)
                .build();

        // Handle any tags using the helper method
        var resolvedTags = resolveTags(request.tags(), request.userId());
        note.setTags(resolvedTags);

        // Return the note.
        var saved = noteRepository.save(note);
        return ResponseEntity.status(HttpStatus.CREATED).body(ReadNoteResponse.from(saved));
    }

    // Route handles getting paginated list of notes.
    @GetMapping
    public Page<ReadNoteResponse> listNotes(
            @RequestParam(required = false) String tag,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        // Check if the tag param is null and if it is not, then trim it.
        var param = (tag == null) ? null : tag.trim();

        // If the tag exists, get notes with it.
        if (param != null && !param.isEmpty()) {
            return noteRepository.findByTags_Name(param, pageable).map(ReadNoteResponse::from);
        }

        // Otherwise, just list notes.
        return noteRepository.findAll(pageable).map(ReadNoteResponse::from);
    }

    // Route handles getting note by ID.
    @GetMapping("/{id}")
    public ReadNoteResponse getNoteById(@PathVariable UUID id) {
        var note = noteRepository.findById(id).orElseThrow(() -> new NoteNotFound(id));
        return ReadNoteResponse.from(note);
    }

    // Route handles updating a Note by ID.
    @Transactional
    @PutMapping("/{id}")
    public ResponseEntity<ReadNoteResponse> updateNote(@PathVariable UUID id, @RequestBody @Valid PutNoteRequest request) {
        log.info("PUT /api/notes/{}", id);

        // Get the note that we want to update.
        var note = noteRepository.findById(id).orElseThrow(() -> new NoteNotFound(id));

        // Get/trim the title and content. Get the archived boolean.
        var title = request.title().trim();
        var content = request.content().trim();
        var archived = request.archived();

        // Set the title, content, and archived values.
        note.setTitle(title);
        note.setContent(content);
        note.setArchived(archived);

        // Handle the tags.
        var resolvedTags = resolveTags(request.tags(), request.userId());
        note.setTags(resolvedTags);

        // Save the note and return a CREATED response.
        var savedNote = noteRepository.save(note);
        return ResponseEntity.status(HttpStatus.OK).body(ReadNoteResponse.from(savedNote));
    }

    @Transactional
    @PatchMapping("/{id}")
    public ResponseEntity<ReadNoteResponse> patchNote(@PathVariable UUID id, @RequestBody @Valid PatchNoteRequest request) {
        log.info("PATCH /api/notes/{}", id);

        // Get the note that we want to patch.
        var note = noteRepository.findById(id).orElseThrow(() -> new NoteNotFound(id));

        request.title().ifPresent(note::setTitle);

        request.content().ifPresent(note::setContent);

        request.archived().ifPresent(note::setArchived);

        request.tags().ifPresent((tags) -> {
            var newTags = resolveTags(tags, request.userId());
            note.setTags(newTags);
        });

        // Save the note and return a good response.
        var savedNote = noteRepository.save(note);
        return ResponseEntity.status(HttpStatus.OK).body(ReadNoteResponse.from(savedNote));
    }

    // Route handles deleting a note by ID.
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNoteById(@PathVariable UUID id) {
        log.info("DELETE /api/notes/{}", id);

        var note = noteRepository.findById(id).orElseThrow(() -> new NoteNotFound(id));

        // If the note exists, delete it and return a no-content response.
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
            resolvedTags.add(tagRepository.findByNameAndOwnerId(name, userId)
                    .orElseGet(() -> {
                        var newTag = Tag.builder().name(name).build();
                        return tagRepository.save(newTag);
                    }));
        }

        return resolvedTags;
    }

}

package dev.jacklucas.notes_api.note;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.jacklucas.notes_api.tag.Tag;
import dev.jacklucas.notes_api.tag.TagRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NoteController.class)
class NoteControllerTest {

    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper om;

    @MockitoBean
    NoteRepository noteRepository;

    @MockitoBean
    TagRepository tagRepository;

    private static final String USER = "user-123";

    private SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor withJwt() {
        return jwt().jwt(j -> j.subject(USER));
    }

    private Note note(UUID id, String title, String content, boolean archived, Set<Tag> tags) {
        return Note.builder()
                .id(id)
                .userId(USER)
                .title(title)
                .content(content)
                .archived(archived)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .tags(tags == null ? new HashSet<>() : tags)
                .build();
    }

    private Tag tag(String name) {
        return Tag.builder().id(UUID.randomUUID()).userId(USER).name(name).createdAt(Instant.now()).build();
    }

    @Test
    @DisplayName("GET /api/notes/{id} returns note when owned")
    void getNoteById_found() throws Exception {
        var id = UUID.randomUUID();
        var n = note(id, "Test Note", "Test Content", false, Set.of());
        when(noteRepository.findById(id)).thenReturn(Optional.of(n));

        mvc.perform(get("/api/notes/{id}", id).with(withJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Note"))
                .andExpect(jsonPath("$.content").value("Test Content"));

        verify(noteRepository).findById(id);
    }

    @Test
    @DisplayName("GET /api/notes returns paged list for user")
    void listNotes() throws Exception {
        var n = note(UUID.randomUUID(), "A", "B", false, Set.of());
        when(noteRepository.findByUserId(eq(USER), any()))
                .thenReturn(new PageImpl<>(List.of(n), PageRequest.of(0, 20), 1));

        mvc.perform(get("/api/notes").with(withJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));
    }

    @Test
    @DisplayName("POST /api/notes creates a note and resolves tags")
    void createNote() throws Exception {
        var body = Map.of(
                "title", "Created",
                "content", "Content",
                "tags", List.of("work", "ideas")
        );

        when(tagRepository.findByNameAndUserId("work", USER)).thenReturn(Optional.empty());
        when(tagRepository.findByNameAndUserId("ideas", USER)).thenReturn(Optional.empty());
        when(tagRepository.save(any(Tag.class))).thenAnswer(inv -> {
            var t = (Tag) inv.getArgument(0);
            if (t.getId() == null) t.setId(UUID.randomUUID());
            t.setCreatedAt(Instant.now());
            return t;
        });

        var saved = note(UUID.randomUUID(), "Created", "Content", false, Set.of(tag("work"), tag("ideas")));
        when(noteRepository.save(any(Note.class))).thenReturn(saved);

        mvc.perform(post("/api/notes").with(withJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Created"));

        var captor = ArgumentCaptor.forClass(Note.class);
        verify(noteRepository).save(captor.capture());
        assertThat(captor.getValue().getUserId()).isEqualTo(USER);
    }

    @Test
    @DisplayName("PUT /api/notes/{id} updates full note including tags")
    void updateNote_put() throws Exception {
        var id = UUID.randomUUID();
        var existing = note(id, "Old", "Old", false, Set.of());
        when(noteRepository.findById(id)).thenReturn(Optional.of(existing));

        when(tagRepository.findByNameAndUserId("x", USER)).thenReturn(Optional.empty());
        when(tagRepository.save(any(Tag.class))).thenAnswer(inv -> {
            var t = (Tag) inv.getArgument(0);
            if (t.getId() == null) t.setId(UUID.randomUUID());
            return t;
        });

        var updated = note(id, "New Title", "New Content", true, Set.of(tag("x")));
        when(noteRepository.save(any(Note.class))).thenReturn(updated);

        var body = Map.of(
                "title", "New Title",
                "content", "New Content",
                "archived", true,
                "tags", List.of("x")
        );

        mvc.perform(put("/api/notes/{id}", id).with(withJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("New Title"))
                .andExpect(jsonPath("$.archived").value(true));
    }

    @Test
    @DisplayName("PATCH /api/notes/{id} partially updates note")
    void patchNote() throws Exception {
        var id = UUID.randomUUID();
        var existing = note(id, "Before", "Body", false, Set.of());
        when(noteRepository.findById(id)).thenReturn(Optional.of(existing));

        var saved = note(id, "Before", "Body", true, Set.of());
        when(noteRepository.save(any(Note.class))).thenReturn(saved);

        // Include required fields to satisfy current validation
        var body = Map.of(
                "title", "Before",   // unchanged
                "content", "Body",   // unchanged
                "archived", true     // the actual change
        );

        mvc.perform(patch("/api/notes/{id}", id).with(withJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.archived").value(true));

        verify(noteRepository).findById(id);
        verify(noteRepository).save(any(Note.class));
    }

    @Test
    @DisplayName("DELETE /api/notes/{id} deletes when owned")
    void deleteNote() throws Exception {
        var id = UUID.randomUUID();
        var existing = note(id, "T", "C", false, Set.of());
        when(noteRepository.findById(id)).thenReturn(Optional.of(existing));

        mvc.perform(delete("/api/notes/{id}", id).with(withJwt()))
                .andExpect(status().isNoContent());

        verify(noteRepository).delete(existing);
    }


    @Test
    @DisplayName("GET /api/notes/{id} -> 404 when missing or not owned")
    void getNoteById_notFound() throws Exception {
        var id = UUID.randomUUID();
        when(noteRepository.findById(id)).thenReturn(Optional.empty());

        mvc.perform(get("/api/notes/{id}", id).with(withJwt()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/notes/{id} -> 404 when missing")
    void putNote_notFound() throws Exception {
        var id = UUID.randomUUID();
        when(noteRepository.findById(id)).thenReturn(Optional.empty());

        var body = Map.of(
                "title", "X",
                "content", "Y",
                "archived", false,
                "tags", List.of()
        );

        mvc.perform(put("/api/notes/{id}", id).with(withJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(body)))
                .andExpect(status().isNotFound());

        verify(noteRepository).findById(id);
        verify(noteRepository, never()).save(any());
    }

    @Test
    @DisplayName("PATCH /api/notes/{id} -> 404 when missing")
    void patchNote_notFound() throws Exception {
        var id = UUID.randomUUID();
        when(noteRepository.findById(id)).thenReturn(Optional.empty());

        // Include required fields so validation passes and we reach the 404 path
        var body = Map.of(
                "title", "keep",
                "content", "keep",
                "archived", true
        );

        mvc.perform(patch("/api/notes/{id}", id).with(withJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(body)))
                .andExpect(status().isNotFound());

        verify(noteRepository).findById(id);
        verify(noteRepository, never()).save(any());
    }

    @Test
    @DisplayName("DELETE /api/notes/{id} -> 404 when missing")
    void deleteNote_notFound() throws Exception {
        var id = UUID.randomUUID();
        when(noteRepository.findById(id)).thenReturn(Optional.empty());

        mvc.perform(delete("/api/notes/{id}", id).with(withJwt()))
                .andExpect(status().isNotFound());

        verify(noteRepository).findById(id);
        verify(noteRepository, never()).delete(any());
    }
}

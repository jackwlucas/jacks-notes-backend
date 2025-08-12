package dev.jacklucas.notes_api.note;

import dev.jacklucas.notes_api.note.dto.CreateNoteRequest;
import dev.jacklucas.notes_api.tag.Tag;
import dev.jacklucas.notes_api.tag.TagRepository;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NoteController.class)
public class NoteControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private NoteRepository noteRepository;

    @MockitoBean
    private TagRepository tagRepository;

    private Note note(String title, String content) {
        Instant now = Instant.now();

        return Note.builder()
                .id(UUID.randomUUID())
                .title(title)
                .content(content)
                .archived(false)
                .createdAt(now)
                .updatedAt(now)
                .tags(new HashSet<>())
                .build();
    }

    private Tag tag(String name) {
        return Tag.builder()
                .id(UUID.randomUUID())
                .name(name)
                .createdAt(Instant.now())
                .build();
    }

    /*
        POST /api/notes
    */
    @Test
    void createNote_WithValidData_Returns201() throws Exception {
        String requestBody =
                """
                            {
                                "title": "Test Note",
                                "content": "Test Content"
                                "tags": null
                            }
                        """;

        // Create the note request and the note it should create.
        Note note = note("Test Title", "Test Content");

        when(noteRepository.save(note)).thenReturn(note);

        mvc.perform(post("/api/notes").contentType(MediaType.APPLICATION_JSON).content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Test Title"))
                .andExpect(jsonPath("$.content").value("Test Content"));

        verify(noteRepository).save(note);
    }

    /*
        GET /api/notes/{ID}
    */
    @Test
    void getNoteById_WhenFound_ReturnsNote() throws Exception {
        Note note = note("Test Note", "Test Content");

        when(noteRepository.findById(note.getId())).thenReturn(Optional.of(note));

        mvc.perform(get("/api/notes/%s".formatted(note.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Note"))
                .andExpect(jsonPath("$.content").value("Test Content"));

        verify(noteRepository).findById(note.getId());
    }

    @Test
    void getNoteById_WhenNotFound_Returns404() throws Exception {
        UUID id = UUID.randomUUID();

        when(noteRepository.findById(id)).thenReturn(Optional.empty());

        mvc.perform(get("/api/notes/%s".formatted(id)))
                .andExpect(status().isNotFound());

        verify(noteRepository).findById(id);
    }

    @Test
    void getNoteById_WithInvalidId_Returns400() throws Exception {
        mvc.perform(get("/api/notes/%s".formatted("invalid-uuid")))
                .andExpect(status().isBadRequest());
    }
}

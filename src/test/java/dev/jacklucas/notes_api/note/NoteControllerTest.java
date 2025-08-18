//package dev.jacklucas.notes_api.note;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import dev.jacklucas.notes_api.note.dto.CreateNoteRequest;
//import dev.jacklucas.notes_api.tag.Tag;
//import dev.jacklucas.notes_api.tag.TagRepository;
//import jakarta.validation.constraints.NotBlank;
//import jakarta.validation.constraints.NotNull;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.http.MediaType;
//import org.springframework.test.context.bean.override.mockito.MockitoBean;
//import org.springframework.test.web.servlet.MockMvc;
//
//import java.time.Instant;
//import java.util.*;
//
//import static org.hamcrest.Matchers.containsInAnyOrder;
//import static org.hamcrest.Matchers.hasSize;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.anyString;
//import static org.mockito.Mockito.*;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//@WebMvcTest(NoteController.class)
//public class NoteControllerTest {
//
//    @Autowired
//    private MockMvc mvc;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    @MockitoBean
//    private NoteRepository noteRepository;
//
//    @MockitoBean
//    private TagRepository tagRepository;
//
//    private Note note(String title, String content) {
//        Instant now = Instant.now();
//
//        return Note.builder()
//                .id(UUID.randomUUID())
//                .title(title)
//                .content(content)
//                .archived(false)
//                .createdAt(now)
//                .updatedAt(now)
//                .tags(new HashSet<>())
//                .build();
//    }
//
//    private Tag tag(String name) {
//        return Tag.builder()
//                .id(UUID.randomUUID())
//                .name(name)
//                .createdAt(Instant.now())
//                .build();
//    }
//
//    /*
//        POST /api/notes
//    */
//    @Test
//    void createNote_WithValidData_Returns201() throws Exception {
//        // Create the note request and the note it should create.
//        CreateNoteRequest request = new CreateNoteRequest("Test Title", "Test Content", Collections.emptyList());
//        Note note = note("Test Title", "Test Content");
//
//        when(noteRepository.save(any(Note.class))).thenReturn(note);
//
//        mvc.perform(post("/api/notes")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isCreated())
//                .andExpect(jsonPath("$.title").value("Test Title"))
//                .andExpect(jsonPath("$.content").value("Test Content"));
//
//        verify(noteRepository).save(any(Note.class));
//    }
//
//    @Test
//    void createNote_WithNullTags_Returns201() throws Exception {
//        CreateNoteRequest request = new CreateNoteRequest("Test Title", "Test Content", null);
//        Note savedNote = note("Test Title", "Test Content");
//
//        when(noteRepository.save(any(Note.class))).thenReturn(savedNote);
//
//        mvc.perform(post("/api/notes")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isCreated())
//                .andExpect(jsonPath("$.title").value("Test Title"))
//                .andExpect(jsonPath("$.tags").isEmpty());
//
//        verify(noteRepository).save(any(Note.class));
//    }
//
//    @Test
//    void createNote_WithoutTitle_Returns400() throws Exception {
//        CreateNoteRequest request = new CreateNoteRequest("", "Test Content", Collections.emptyList());
//
//        mvc.perform(post("/api/notes")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isBadRequest());
//    }
//
//    @Test
//    void createNote_WithNullContent_Returns400() throws Exception {
//        CreateNoteRequest request = new CreateNoteRequest("Test Title", null, Collections.emptyList());
//
//        mvc.perform(post("/api/notes")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isBadRequest());
//    }
//
//    @Test
//    void createNote_WithNewTags_Returns201() throws Exception {
//        CreateNoteRequest request = new CreateNoteRequest("Test Title", "Test Content", List.of("tag1", "tag2"));
//        Note savedNote = note("Test Title", "Test Content");
//        Tag tag1 = tag("tag1");
//        Tag tag2 = tag("tag2");
//
//        savedNote.setTags(Set.of(tag1, tag2));
//
//        // When repository creates note, it returns note.
//        when(noteRepository.save(any(Note.class))).thenReturn(savedNote);
//
//        // When repository searches for tags, it returns nothing.
//        when(tagRepository.findByName(anyString())).thenReturn(Optional.empty());
//
//        // When repository creates tags, it returns tags.
//        when(tagRepository.save(any(Tag.class))).thenReturn(tag1, tag2);
//
//        mvc.perform(post("/api/notes")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isCreated())
//                .andExpect(jsonPath("$.tags", hasSize(2)))
//                .andExpect(jsonPath("$.tags", containsInAnyOrder("tag1", "tag2")));
//
//        // If save was called three times, we can assume that findByName returned empty three times.
//        verify(tagRepository, times(2)).save(any(Tag.class));
//        verify(noteRepository).save(any(Note.class));
//    }
//
//    @Test
//    void createNote_WithExistingTags_Returns201() throws Exception {
//        CreateNoteRequest request = new CreateNoteRequest("Test Title", "Test Content", List.of("tag1", "tag2"));
//        Note savedNote = note("Test Title", "Test Content");
//        Tag tag1 = tag("tag1");
//        Tag tag2 = tag("tag2");
//
//        savedNote.setTags(Set.of(tag1, tag2));
//
//        // When repository creates note, it returns note.
//        when(noteRepository.save(any(Note.class))).thenReturn(savedNote);
//
//        // When repository searches for tags, it returns tags.
//        when(tagRepository.findByName("tag1")).thenReturn(Optional.of(tag1));
//        when(tagRepository.findByName("tag2")).thenReturn(Optional.of(tag2));
//
//        mvc.perform(post("/api/notes")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isCreated())
//                .andExpect(jsonPath("$.tags", hasSize(2)))
//                .andExpect(jsonPath("$.tags", containsInAnyOrder("tag1", "tag2")));
//
//        // If save was called three times, we can assume that findByName returned empty three times.
//        verify(tagRepository, never()).save(any(Tag.class));
//        verify(noteRepository).save(any(Note.class));
//    }
//
//    @Test
//    void createNote_WithNewAndExistingTags_Returns201() throws Exception {
//        CreateNoteRequest request = new CreateNoteRequest("Test Title", "Test Content", List.of("tag1", "tag2"));
//        Note savedNote = note("Test Title", "Test Content");
//        Tag tag1 = tag("tag1");
//        Tag tag2 = tag("tag2");
//
//        savedNote.setTags(Set.of(tag1, tag2));
//
//        // When repository creates note, it returns note.
//        when(noteRepository.save(any(Note.class))).thenReturn(savedNote);
//
//        // When repository searches for tags, it returns tags.
//        when(tagRepository.findByName("tag1")).thenReturn(Optional.empty());
//        when(tagRepository.findByName("tag2")).thenReturn(Optional.of(tag2));
//
//        mvc.perform(post("/api/notes")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isCreated())
//                .andExpect(jsonPath("$.tags", hasSize(2)))
//                .andExpect(jsonPath("$.tags", containsInAnyOrder("tag1", "tag2")));
//
//        // If save was called three times, we can assume that findByName returned empty three times.
//        verify(tagRepository, times(1)).save(any(Tag.class));
//        verify(noteRepository).save(any(Note.class));
//    }
//
//    @Test
//    void createNote_WithValidAndInvalidTags_Returns201() throws Exception {
//        CreateNoteRequest request = new CreateNoteRequest("Test Title", "Test Content", List.of("tag1", ""));
//        Note savedNote = note("Test Title", "Test Content");
//        Tag tag1 = tag("tag1");
//
//        savedNote.setTags(Set.of(tag1));
//
//        // When repository creates note, it returns note.
//        when(noteRepository.save(any(Note.class))).thenReturn(savedNote);
//
//        // When repository searches for tags, it returns tags.
//        when(tagRepository.findByName("tag1")).thenReturn(Optional.empty());
//
//        mvc.perform(post("/api/notes")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isCreated())
//                .andExpect(jsonPath("$.tags", hasSize(1)))
//                .andExpect(jsonPath("$.tags", containsInAnyOrder("tag1")));
//
//        // If save was called three times, we can assume that findByName returned empty three times.
//        verify(tagRepository, times(1)).save(any(Tag.class));
//        when(tagRepository.save(any(Tag.class))).thenReturn(tag1);
//    }
//
//    @Test
//    void createNote_WithDuplicateTags_Returns201() throws Exception {
//        CreateNoteRequest request = new CreateNoteRequest("Test Title", "Test Content", List.of("tag1", "tag1"));
//        Note savedNote = note("Test Title", "Test Content");
//        Tag tag1 = tag("tag1");
//
//        savedNote.setTags(Set.of(tag1));
//
//        // When repository creates note, it returns note.
//        when(noteRepository.save(any(Note.class))).thenReturn(savedNote);
//
//        // When repository searches for tags, it returns nothing.
//        when(tagRepository.findByName("tag1")).thenReturn(Optional.empty());
//
//        mvc.perform(post("/api/notes")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isCreated())
//                .andExpect(jsonPath("$.tags", hasSize(1)))
//                .andExpect(jsonPath("$.tags", containsInAnyOrder("tag1")));
//
//        // If save was called three times, we can assume that findByName returned empty three times.
//        verify(tagRepository, times(1)).save(any(Tag.class));
//        when(tagRepository.save(any(Tag.class))).thenReturn(tag1);
//    }
//
//    /*
//        GET /api/notes/{ID}
//    */
//    @Test
//    void getNoteById_WhenFound_ReturnsNote() throws Exception {
//        Note note = note("Test Note", "Test Content");
//
//        when(noteRepository.findById(note.getId())).thenReturn(Optional.of(note));
//
//        mvc.perform(get("/api/notes/%s".formatted(note.getId())))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.title").value("Test Note"))
//                .andExpect(jsonPath("$.content").value("Test Content"));
//
//        verify(noteRepository).findById(note.getId());
//    }
//
//    @Test
//    void getNoteById_WhenNotFound_Returns404() throws Exception {
//        UUID id = UUID.randomUUID();
//
//        when(noteRepository.findById(id)).thenReturn(Optional.empty());
//
//        mvc.perform(get("/api/notes/%s".formatted(id)))
//                .andExpect(status().isNotFound());
//
//        verify(noteRepository).findById(id);
//    }
//
//    @Test
//    void getNoteById_WithInvalidId_Returns400() throws Exception {
//        mvc.perform(get("/api/notes/%s".formatted("invalid-uuid")))
//                .andExpect(status().isBadRequest());
//    }
//}

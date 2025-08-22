package dev.jacklucas.notes_api.tag;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TagController.class)
class TagControllerTest {

    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper om;

    @MockitoBean
    TagRepository tagRepository;

    private static final String USER = "user-123";

    private SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor withJwt() {
        return jwt().jwt(j -> j.subject(USER));
    }

    private Tag tag(String name) {
        return Tag.builder().id(UUID.randomUUID()).userId(USER).name(name).createdAt(Instant.now()).build();
    }

    @Test
    @DisplayName("POST /api/tags creates new tag or 409 if exists")
    void createTag() throws Exception {
        when(tagRepository.findByNameAndUserId("Work", USER)).thenReturn(Optional.empty());
        when(tagRepository.save(any(Tag.class))).thenAnswer(inv -> {
            var t = (Tag) inv.getArgument(0);
            if (t.getId() == null) t.setId(UUID.randomUUID());
            t.setCreatedAt(Instant.now());
            return t;
        });

        mvc.perform(post("/api/tags").with(withJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(Map.of("name", " Work "))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Work"));

        // conflict path
        var existing = tag("Work");
        when(tagRepository.findByNameAndUserId("Work", USER)).thenReturn(Optional.of(existing));

        mvc.perform(post("/api/tags").with(withJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(Map.of("name", "Work"))))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("GET /api/tags returns paged list for user")
    void listTags() throws Exception {
        when(tagRepository.findByUserId(eq(USER), any()))
                .thenReturn(new PageImpl<>(List.of(tag("a")), PageRequest.of(0, 20), 1));

        mvc.perform(get("/api/tags").with(withJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));
    }

    @Test
    @DisplayName("GET /api/tags/{id} returns tag when owned")
    void getTagById() throws Exception {
        var t = tag("Ideas");
        when(tagRepository.findById(t.getId())).thenReturn(Optional.of(t));

        mvc.perform(get("/api/tags/{id}", t.getId()).with(withJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Ideas"));
    }

    @Test
    @DisplayName("PUT /api/tags/{id} handles no-op, conflict, and success")
    void updateTag() throws Exception {
        var id = UUID.randomUUID();
        var existing = Tag.builder().id(id).userId(USER).name("alpha").createdAt(Instant.now()).build();

        when(tagRepository.findById(id)).thenReturn(Optional.of(existing));

        // no-op (same name)
        mvc.perform(put("/api/tags/{id}", id).with(withJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(Map.of("name", "alpha"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("alpha"));

        // conflict (duplicate)
        when(tagRepository.findByNameAndUserId("bravo", USER)).thenReturn(Optional.of(tag("bravo")));
        mvc.perform(put("/api/tags/{id}", id).with(withJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(Map.of("name", "bravo"))))
                .andExpect(status().isConflict());

        // success rename
        when(tagRepository.findByNameAndUserId("charlie", USER)).thenReturn(Optional.empty());
        mvc.perform(put("/api/tags/{id}", id).with(withJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(Map.of("name", "charlie"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("charlie"));
    }

    @Test
    @DisplayName("DELETE /api/tags/{id} deletes when owned")
    void deleteTag() throws Exception {
        var t = tag("Old");
        when(tagRepository.findById(t.getId())).thenReturn(Optional.of(t));

        mvc.perform(delete("/api/tags/{id}", t.getId()).with(withJwt()))
                .andExpect(status().isNoContent());

        verify(tagRepository).delete(t);
    }
}

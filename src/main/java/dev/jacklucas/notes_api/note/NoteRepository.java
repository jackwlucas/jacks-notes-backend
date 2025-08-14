package dev.jacklucas.notes_api.note;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface NoteRepository extends JpaRepository<Note, UUID> {
    Page<Note> findByUserId(String userId, Pageable pageable);

    Page<Note> findByUserIdAndTags_Name(String userId, String name, Pageable pageable);
}

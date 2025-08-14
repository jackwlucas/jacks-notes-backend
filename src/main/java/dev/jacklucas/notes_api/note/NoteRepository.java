package dev.jacklucas.notes_api.note;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface NoteRepository extends JpaRepository<Note, UUID> {
    Page<Note> findByTags_Name(String name, Pageable pageable);

    Page<Note> findByOwnerId(String ownerId, java.awt.print.Pageable pageable);

    Page<Note> findByOwnerIdAndTags_Name(String ownerId, String name, java.awt.print.Pageable pageable);
}

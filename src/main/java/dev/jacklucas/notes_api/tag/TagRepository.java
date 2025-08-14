package dev.jacklucas.notes_api.tag;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TagRepository extends JpaRepository<Tag, UUID> {
    Optional<Tag> findByName(String name);

    Optional<Tag> findByNameIgnoreCase(String name);

    Optional<Tag> findByNameAndOwnerId(String name, String ownerId);

    Optional<Tag> findByNameIgnoreCaseAndOwnerId(String name, String ownerId);
}

package dev.jacklucas.notes_api.tag;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface TagRepository extends JpaRepository<Tag, UUID> {
    Page<Tag> findByUserId(String userId, Pageable pageable);

    Optional<Tag> findByNameAndUserId(String name, String userId);

    Optional<Tag> findByNameIgnoreCaseAndUserId(String name, String userId);

}

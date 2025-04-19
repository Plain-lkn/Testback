package org.example.plain.domain.file.repository;

import org.example.plain.domain.file.entity.ImageFileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImageFileRepository extends JpaRepository<ImageFileEntity, String> {
    // Find image by filename
    ImageFileEntity findByFilename(String filename);
} 
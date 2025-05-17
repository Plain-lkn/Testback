package org.example.plain.domain.file.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "image_file")
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ImageFileEntity extends FileEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    public static ImageFileEntity makeImageFileEntity(String filename, String filePath) {
        return ImageFileEntity.builder()
                .filename(filename)
                .filePath(filePath)
                .build();
    }
}

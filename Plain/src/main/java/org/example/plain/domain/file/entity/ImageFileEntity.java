package org.example.plain.domain.file.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "file_image")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImageFileEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private String filename;
    private String filePath;

    public static ImageFileEntity makeImageFileEntity(String filename, String filePath) {
        return ImageFileEntity.builder()
                .filename(filename)
                .filePath(filePath)
                .build();
    }
}

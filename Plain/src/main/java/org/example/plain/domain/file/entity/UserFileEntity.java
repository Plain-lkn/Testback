package org.example.plain.domain.file.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.plain.domain.file.entity.id.UserFileEntityKey;
import org.example.plain.domain.user.entity.User;

@Entity
@Table(name = "user_image")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserFileEntity {

    @EmbeddedId
    private UserFileEntityKey fileId;

    @MapsId("userId")
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @MapsId("fileId")
    @ManyToOne
    @JoinColumn(name = "file_id")
    private ImageFileEntity imageFileEntity;
    
    public static UserFileEntity createUserProfileImage(User user, ImageFileEntity imageFile) {
        UserFileEntityKey key = new UserFileEntityKey(imageFile.getId(), user.getId());
        
        return UserFileEntity.builder()
                .fileId(key)
                .user(user)
                .imageFileEntity(imageFile)
                .build();
    }
}

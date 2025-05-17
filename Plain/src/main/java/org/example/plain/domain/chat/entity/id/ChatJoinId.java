package org.example.plain.domain.chat.entity.id;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ChatJoinId implements Serializable {
    private String chatId;
    private String userId;
} 
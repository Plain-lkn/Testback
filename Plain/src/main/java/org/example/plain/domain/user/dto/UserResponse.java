package org.example.plain.domain.user.dto;

import org.example.plain.domain.user.entity.User;

public record UserResponse(
        String id,
        String username,
        String email,
        String profileImageUrl
) {
    // 생성자 오버로딩 - 프로필 이미지 없는 경우
    public UserResponse(String id, String username, String email) {
        this(id, username, email, null);
    }

    public static UserResponse chaingeUsertoUserResponse(User user){
        return new UserResponse(user.getId(), user.getUsername(), user.getEmail());
    }
}

package org.example.plain.domain.user.interfaces;

import org.example.plain.domain.user.dto.UserRequest;
import org.example.plain.domain.user.dto.UserResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public interface UserService {
    public boolean createUser(UserRequest userRequest);
    boolean updateUser(UserRequest userRequest);
    public boolean deleteUser(String id);
    public UserResponse getUser(String id);
    public boolean checkUserIdIsExist(String id);
    public UserResponse getUserByEmail(String email);
    boolean updateProfileImage(MultipartFile file, String userId);
}

package org.example.plain.domain.user.interfaces;

import org.example.plain.domain.user.dto.UserRequest;
import org.example.plain.domain.user.dto.UserResponse;
import org.springframework.stereotype.Service;

@Service
public interface UserService {
    public boolean createUser(UserRequest userRequest);
    boolean updateUser(UserRequest userRequest);
    public boolean deleteUser(String id);
    public UserResponse getUser(String id);
    public boolean checkUserIdIsExist(String id);
    public UserResponse getUserByEmail(String email);
}

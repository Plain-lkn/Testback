package org.example.plain.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.example.plain.common.enums.Role;
import org.example.plain.domain.user.dto.UserRequest;
import org.example.plain.domain.user.dto.UserResponse;
import org.example.plain.domain.user.entity.User;
import org.example.plain.domain.user.interfaces.UserService;
import org.example.plain.domain.user.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;


    @Override
    @Transactional
    public boolean createUser(UserRequest userRequest) {
        User user = new User(userRequest);
        user.setPassword(bCryptPasswordEncoder.encode(userRequest.getPassword()));
        user.setRole(Role.NORMAL);
        Objects.requireNonNull(user);
        userRepository.save(user);
        return true;
    }

    @Override
    public boolean updateUser(UserRequest userRequest) {
        userRepository.findById(userRequest.getId()).ifPresent(userEntity -> {
                userEntity.setUsername(userRequest.getUsername());
                if (userRequest.getPassword() != null) {
                    userEntity.setPassword(bCryptPasswordEncoder.encode(userRequest.getPassword()));
                }
                userEntity.setEmail(userRequest.getEmail());
                userRepository.save(userEntity);
        });
        return true;
    }

    @Override
    public boolean deleteUser(String id) {
        userRepository.findById(id).ifPresent(userRepository::delete);
        return true;
    }

    @Override
    public UserResponse getUser(String id) {
        return UserResponse.chaingeUsertoUserResponse(userRepository.findById(id).orElseThrow());
    }

    @Override
    public UserResponse getUserByEmail(String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return null;
        }
        return UserResponse.chaingeUsertoUserResponse(user);
    }


}

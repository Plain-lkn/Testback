package org.example.plain.domain.user.controller;

import lombok.RequiredArgsConstructor;
import org.example.plain.domain.user.dto.UserRequest;
import org.example.plain.domain.user.interfaces.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class registerController {

    private final UserService userService;

    @PostMapping("users/sign_up")
    public ResponseEntity singUp(UserRequest userRequest) {
        userService.createUser(userRequest);
        return ResponseEntity.noContent().build();
    }
}

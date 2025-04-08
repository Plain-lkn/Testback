package org.example.plain.domain.user.controller;

import org.example.plain.common.ResponseField;
import org.example.plain.common.ResponseMaker;
import org.example.plain.domain.user.dto.CustomUserDetails;
import org.example.plain.domain.user.dto.UserRequest;
import org.example.plain.domain.user.dto.UserResponse;
import org.example.plain.domain.user.interfaces.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

// logout 기능 개발.
@RestController
@RequestMapping("/account")
public class AccountController {

    @Autowired
    UserService userService;

    @PostMapping("/create")
    public ResponseEntity createAccount(@RequestBody UserRequest userRequest) throws Exception {
        userService.createUser(userRequest);
        return ResponseMaker.noContent();
    }

    @PatchMapping("/update")
    public ResponseEntity updateAccount(@RequestBody UserRequest userRequest, Authentication authentication) throws Exception {
        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
        if (!userRequest.getId().equals(customUserDetails.getUser().getId())) {
            throw new HttpClientErrorException(HttpStatus.FORBIDDEN);
        }
        userService.updateUser(userRequest);
        return ResponseMaker.noContent();
    }


    @DeleteMapping
    public ResponseEntity deleteAccount(@RequestBody String uId, Model model) throws Exception {
        userService.deleteUser(uId);
        return ResponseMaker.noContent();
    }

    @GetMapping("/info")
    public ResponseEntity<ResponseField<UserResponse>> getUserInfo(String userId){
        UserResponse userResponse = userService.getUser(userId);
        return new ResponseMaker<UserResponse>().ok(userResponse);
    }

}

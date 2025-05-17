package org.example.plain.domain.userManage;

import lombok.RequiredArgsConstructor;
import org.example.plain.common.ResponseField;
import org.example.plain.domain.classLecture.dto.ClassResponse;
import org.example.plain.domain.lecture.dto.LectureResponse;
import org.example.plain.domain.user.dto.UserResponse;
import org.example.plain.domain.userManage.interfaces.UserManageService;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class UserMenageController {
    private final UserManageService userManageService;

    @ResponseBody
    @GetMapping("/user_info")
    public ResponseField<UserResponse> userInfo(String userId){
        return new ResponseField<UserResponse>().returnOkResponseField(userManageService.userSingleInfo(userId));
    }

    @ResponseBody
    @GetMapping("/my_lectures")
    public ResponseField<List<LectureResponse>> classList(String userId){
        return new ResponseField<List<LectureResponse>>().returnOkResponseField(userManageService.getMyLectures(userId));
    }

    @ResponseBody
    @GetMapping("/my_classes")
    public ResponseField<List<ClassResponse>> getMyClass(String userId){
        return new ResponseField<List<ClassResponse>>().returnOkResponseField(userManageService.getMyClasses(userId));
    }

}

package org.example.plain.domain.userManage.interfaces;

import org.example.plain.domain.classLecture.dto.ClassResponse;
import org.example.plain.domain.lecture.dto.LectureResponse;
import org.example.plain.domain.user.dto.UserRequest;
import org.example.plain.domain.user.dto.UserResponse;

import java.util.List;

public interface UserManageService {
    public UserResponse userSingleInfo(String userId);
    public List<ClassResponse> getMyClasses(String userId);
    public List<LectureResponse> getMyLectures(String userId);
    public void readAlarm(boolean read);
}

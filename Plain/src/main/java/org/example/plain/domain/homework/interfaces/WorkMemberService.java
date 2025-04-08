package org.example.plain.domain.homework.interfaces;

import org.example.plain.domain.homework.dto.WorkMember;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;


public interface WorkMemberService {
    public void addHomeworkMember(String workId, String memberId, String userId);
    public void removeHomeworkMember(String workId, String memberId, String userId);
    public WorkMember getSingleMembers(String workId, String memberId);
    public List<WorkMember> homeworkMembers(String workId);
}

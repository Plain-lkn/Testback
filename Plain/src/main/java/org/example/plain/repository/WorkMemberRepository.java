package org.example.plain.repository;

import org.example.plain.domain.classMember.entity.ClassMember;
import org.example.plain.domain.homework.entity.WorkEntity;
import org.example.plain.domain.homework.entity.WorkMemberEntity;
import org.example.plain.domain.homework.entity.WorkMemberId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.util.List;
import java.util.Optional;

public interface WorkMemberRepository extends JpaRepository<WorkMemberEntity, WorkMemberId> {
    List<WorkMemberEntity> findByUser(ClassMember groupUser);

    List<WorkMemberEntity> findByWork(WorkEntity workId);

    @Query("SELECT w FROM WorkMemberEntity w WHERE w.work.workId = :workId AND w.user.id = :userId")
    Optional<WorkMemberEntity> findByWorkIdAndUserId(@Param("workId") String workId, @Param("userId") String userId);
}

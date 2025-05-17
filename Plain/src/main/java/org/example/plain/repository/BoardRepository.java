package org.example.plain.repository;

import org.example.plain.domain.board.dto.Board;
import org.example.plain.domain.board.entity.BoardEntity;
import org.example.plain.domain.homework.dto.Work;
import org.example.plain.domain.homework.entity.WorkEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BoardRepository extends JpaRepository<BoardEntity, String> {
    @Query("SELECT w FROM WorkEntity w WHERE w.workId = :id")
    Optional<WorkEntity> findByWorkId(String id);

    Optional<List<WorkEntity>> findByGroupId(String  groupId);

    @Modifying
    @Query("SELECT w FROM BoardEntity w WHERE w.classId = :classId AND w.title like %:keyword%")
    Optional<List<BoardEntity>> findBoardByClassIdAndKeyword(String classId, String keyword);

    @Modifying
    @Query("SELECT w FROM BoardEntity w WHERE w.classId = :classId AND w.title like %:keyword%")
    Optional<List<WorkEntity>> findByClassIdAndKeyword(String classId, String keyword);


    List<BoardEntity> findAllByGroupId(String groupId);

    Optional<BoardEntity> findByBoardId(String id);

}

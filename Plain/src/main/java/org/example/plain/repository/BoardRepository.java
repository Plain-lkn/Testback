package org.example.plain.repository;

import org.example.plain.domain.board.dto.Board;
import org.example.plain.domain.board.entity.BoardEntity;
import org.example.plain.domain.homework.dto.Work;
import org.example.plain.domain.homework.entity.WorkEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BoardRepository extends JpaRepository<BoardEntity, String> {
    @Query("SELECT w FROM WorkEntity w WHERE w.workId = :id")
    Optional<WorkEntity> findByWorkId(String id);

    Optional<List<WorkEntity>> findByGroupId(String  groupId);

    List<BoardEntity> findAllByGroupId(String groupId);

    Optional<BoardEntity> findByBoardId(String id);

}

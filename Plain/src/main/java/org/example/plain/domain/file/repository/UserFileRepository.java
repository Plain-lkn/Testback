package org.example.plain.domain.file.repository;

import org.example.plain.domain.file.entity.UserFileEntity;
import org.example.plain.domain.file.entity.id.UserFileEntityKey;
import org.example.plain.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserFileRepository extends JpaRepository<UserFileEntity, UserFileEntityKey> {
    Optional<UserFileEntity> findByUser(User user);
    
    @Query("SELECT u FROM UserFileEntity u WHERE u.user.id = :userId")
    Optional<UserFileEntity> findByUserId(@Param("userId") String userId);
    
    void deleteByUser(User user);
}

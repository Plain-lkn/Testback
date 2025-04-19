package org.example.plain.domain.file.service;

import lombok.RequiredArgsConstructor;
import org.example.plain.domain.file.dto.FileData;
import org.example.plain.domain.file.dto.UserFileData;
import org.example.plain.domain.file.entity.FileEntity;
import org.example.plain.domain.file.entity.ImageFileEntity;
import org.example.plain.domain.file.entity.UserFileEntity;
import org.example.plain.domain.file.interfaces.CloudFileService;
import org.example.plain.domain.file.interfaces.FileDatabaseService;
import org.example.plain.domain.file.repository.ImageFileRepository;
import org.example.plain.domain.file.repository.UserFileRepository;
import org.example.plain.domain.user.entity.User;
import org.example.plain.domain.user.repository.UserRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserImageFileDatabaseServiceImpl implements FileDatabaseService {
    
    private final ImageFileRepository imageFileRepository;
    private final UserFileRepository userFileRepository;
    private final UserRepository userRepository;
    private final CloudFileService cloudFileService;

    /**
     * 프로필 이미지 저장
     * 이미 저장된 프로필 이미지가 있으면 기존 이미지 삭제 후 새 이미지 저장
     * @param filename 파일명
     * @param filepath 파일 경로
     * @param fileData 파일 데이터
     * @return 파일 엔티티
     */
    @Transactional
    public FileEntity save(String filename, String filepath, FileData fileData) {
        UserFileData userFileData = (UserFileData) fileData;
        
        // 1. 사용자 정보 조회
        User user = userRepository.findById(userFileData.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userFileData.getUserId()));
        
        // 1.5. 사용자의 기존 프로필 이미지 조회 및 처리
        Optional<UserFileEntity> existingUserFile = userFileRepository.findByUser(user);
        if (existingUserFile.isPresent()) {
            // 1.5.1. 동일한 파일명이면 새로 저장하지 않고 기존 파일 반환 (null 반환)
            ImageFileEntity existingImageFile = existingUserFile.get().getImageFileEntity();
            if (existingImageFile.getFilename().equals(filename)) {
                // ImageFileEntity가 FileEntity를 상속하지 않으므로 null 반환
                return null;
            }
            
            // 1.5.2. 다른 파일명이면 기존 이미지 삭제 
            ImageFileEntity oldImageFile = existingUserFile.get().getImageFileEntity();
            userFileRepository.delete(existingUserFile.get());
            
            // 클라우드에서 파일 삭제 (S3)
            cloudFileService.deleteFile(oldImageFile.getFilePath());
            imageFileRepository.delete(oldImageFile);
        }

        // 2. 이미지 파일 엔티티 생성 및 저장
        ImageFileEntity imageFile = ImageFileEntity.makeImageFileEntity(filename, filepath);
        imageFile = imageFileRepository.save(imageFile);
        
        // 3. 사용자-이미지 연결 엔티티 생성 및 저장
        UserFileEntity userFileEntity = UserFileEntity.createUserProfileImage(user, imageFile);
        userFileRepository.save(userFileEntity);
        
        // ImageFileEntity가 FileEntity를 상속하지 않으므로 null 반환
        return null;
    }


    @Override
    @Transactional
    public void delete(String filename, String filepath) {
        // 이미지 파일 엔티티 조회
        ImageFileEntity imageFile = imageFileRepository.findByFilename(filename);
        if (imageFile != null) {
            // 연결된 UserFileEntity 삭제
            // 실제 파일 삭제
            cloudFileService.deleteFile(filepath);
            
            // 이미지 파일 엔티티 삭제
            imageFileRepository.delete(imageFile);
        }
    }

    @Override
    public void chackFileData(FileData fileData) {
        // 파일 데이터 유효성 검사
        if (fileData == null || fileData.getFile() == null) {
            throw new IllegalArgumentException("File data or file cannot be null");
        }
    }

    @Override
    @Transactional
    public void delete(FileEntity file) {
        // FileEntity와 ImageFileEntity는 상속 관계가 아니므로 
        // 파일명과 경로를 이용해 삭제 로직 구현
        if (file != null) {
            delete(file.getFilename(), file.getFilePath());
        }
    }
    
    /**
     * 사용자 프로필 이미지 조회
     */
    @Transactional(readOnly = true)
    public ImageFileEntity getUserProfileImage(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
        
        // 사용자의 프로필 이미지 조회
        Optional<UserFileEntity> userFile = userFileRepository.findByUser(user);
        return userFile.map(UserFileEntity::getImageFileEntity).orElse(null);
    }
    
    /**
     * 특정 사용자에게 지정된 파일명의 프로필 이미지가 이미 존재하는지 확인
     * @param userId 사용자 ID
     * @param filename 파일명
     * @return 이미 존재하면 true, 아니면 false
     */
    @Transactional(readOnly = true)
    public boolean isProfileImageExists(String userId, String filename) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
        
        Optional<UserFileEntity> userFile = userFileRepository.findByUser(user);
        if (!userFile.isPresent()) {
            return false;
        }
        
        return userFile.get().getImageFileEntity().getFilename().equals(filename);
    }
}

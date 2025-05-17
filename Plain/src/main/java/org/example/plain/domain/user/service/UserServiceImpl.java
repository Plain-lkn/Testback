package org.example.plain.domain.user.service;

import org.example.plain.common.enums.Role;
import org.example.plain.domain.file.dto.FileData;
import org.example.plain.domain.file.dto.FileInfo;
import org.example.plain.domain.file.dto.UserFileData;
import org.example.plain.domain.file.entity.ImageFileEntity;
import org.example.plain.domain.file.entity.UserFileEntity;
import org.example.plain.domain.file.interfaces.CloudFileService;
import org.example.plain.domain.file.interfaces.FileDatabaseService;
import org.example.plain.domain.file.repository.UserFileRepository;
import org.example.plain.domain.user.dto.UserRequest;
import org.example.plain.domain.user.dto.UserResponse;
import org.example.plain.domain.user.entity.EmailVerification;
import org.example.plain.domain.user.entity.User;
import org.example.plain.domain.user.interfaces.UserService;
import org.example.plain.domain.user.repository.EmailVerificationRepository;
import org.example.plain.domain.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.multipart.MultipartFile;

import java.util.NoSuchElementException;
import java.util.Objects;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final CloudFileService cloudFileService;

    private final FileDatabaseService userImageService;
    private final UserFileRepository userFileRepository;
    private final EmailVerificationRepository emailVerificationRepository;

    public UserServiceImpl(UserRepository userRepository,
                           BCryptPasswordEncoder bCryptPasswordEncoder,
                           CloudFileService cloudFileService,
                           @Qualifier("userImageFileDatabaseServiceImpl") FileDatabaseService userImageService,
                           UserFileRepository userFileRepository, EmailVerificationRepository emailVerificationRepository) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.cloudFileService = cloudFileService;
        this.userImageService = userImageService;
        this.userFileRepository = userFileRepository;
        this.emailVerificationRepository = emailVerificationRepository;
    }


    @Override
    @Transactional
    public boolean createUser(UserRequest userRequest) {
        // 아이디 중복 검사
        checkUserIdIsExist(userRequest.getId());
//        EmailVerification verification = emailVerificationRepository.findById(userRequest.getEmail()).orElseThrow(()->new NoSuchElementException("이메일 인증을 하지 않았습니다."));
//        if (!verification.getVerified()){
//            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "이메일 인증을 하지 않았습니다.");
//        }
        
        // 이메일 중복 검사
        if (userRepository.findByEmail(userRequest.getEmail()).isPresent()) {
            throw new HttpClientErrorException(HttpStatus.CONFLICT, "이미 존재하는 이메일입니다.");
        }

        User user = new User(userRequest);
        user.setPassword(bCryptPasswordEncoder.encode(userRequest.getPassword()));
        user.setRole(Role.NORMAL);
        Objects.requireNonNull(user);
        userRepository.save(user);
        //emailVerificationRepository.delete(verification);
        return true;
    }

    @Override
    @Transactional
    public boolean updateProfileImage(MultipartFile file, String userId) {
        // 1. 사용자 정보 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new HttpClientErrorException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));

        try {
            // 2. 파일 정보 설정
            FileData fileData = FileData.builder()
                    .file(file)
                    .filename(file.getOriginalFilename())
                    .build();
            
            // 3. 클라우드에 파일 업로드
            FileInfo fileInfo = cloudFileService.uploadSingleFile(fileData, userId, "profile");

            UserFileData userFileData = UserFileData.builder()
                    .userId(userId)
                    .build();

            // 4. 데이터베이스에 파일 정보 저장
            userImageService.save(
                    fileInfo.getFilename(), 
                    fileInfo.getFileUrl(), 
                    userFileData
            );
            
            return true;
        } catch (Exception e) {
            throw new RuntimeException("프로필 이미지 업로드 실패: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean updateUser(UserRequest userRequest) {
        try {
            userRepository.findById(userRequest.getId()).ifPresent(userEntity -> {
                userEntity.setUsername(userRequest.getUsername());
                if (userRequest.getPassword() != null) {
                    userEntity.setPassword(bCryptPasswordEncoder.encode(userRequest.getPassword()));
                }
                userEntity.setEmail(userRequest.getEmail());
                userRepository.save(userEntity);
            });

            return true;
        }catch (Exception e) {
            throw e;
        }
    }

    @Override
    public boolean deleteUser(String id) {
        try {
            userRepository.findById(id).ifPresent(userRepository::delete);
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public UserResponse getUser(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new HttpClientErrorException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));
        
        // 기본 사용자 정보 조회
        UserResponse response = UserResponse.chaingeUsertoUserResponse(user);
        
        // 프로필 이미지 정보 조회
        ImageFileEntity profileImage = userFileRepository.findByUserId(id).orElse(new UserFileEntity()).getImageFileEntity();
        if (profileImage != null) {
            // UserResponse가 record 클래스이므로 setter 대신 새 인스턴스 생성
            response = new UserResponse(
                    response.id(),
                    response.username(),
                    response.email(),
                    profileImage.getFilePath()
            );
        }
        
        return response;
    }

    @Override
    public boolean checkUserIdIsExist(String id) {
        if (userRepository.findById(id).isPresent()) {
            throw new HttpClientErrorException(HttpStatus.CONFLICT, "이미 존재하는 아이디입니다.");
        }
        return true;
    }

    @Override
    public UserResponse getUserByEmail(String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return null;
        }
        return UserResponse.chaingeUsertoUserResponse(user);
    }
}

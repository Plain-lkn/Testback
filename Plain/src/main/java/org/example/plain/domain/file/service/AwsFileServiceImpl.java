package org.example.plain.domain.file.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import org.example.plain.domain.file.dto.FileData;
import org.example.plain.domain.file.dto.FileInfo;
import org.example.plain.domain.file.dto.FileServiceGenericInfo;
import org.example.plain.domain.file.entity.FileEntity;
import org.example.plain.domain.file.interfaces.CloudFileService;
import org.example.plain.domain.file.interfaces.FileDatabaseService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class AwsFileServiceImpl implements CloudFileService {

    @Value(value = "${file.path}")
    private String filePath;

    @Value(value = "${cloud.aws.s3.bucket}")
    private String bucket;

    private final AmazonS3 amazonS3;

    /**
     * 단일 파일 업로드
     * 이미 S3에 동일한 파일명이 존재하면 업로드하지 않음
     */
    @Override
    public FileInfo uploadSingleFile(FileData fileData, String... id) {
        MultipartFile file = fileData.getFile();
        
        if (file == null) {
            throw new IllegalArgumentException("File cannot be null");
        }

        String filename = makeFilename(file.getOriginalFilename(), id);
        
        // S3에 이미 동일한 키로 파일이 존재하는지 확인
        if (isFileExists(filename)) {
            // 이미 존재하는 파일이면 URL만 반환
            String fileUrl = String.valueOf(amazonS3.getUrl(bucket, filename));
            return new FileInfo(filename, fileUrl);
        }

        try (InputStream inputStream = file.getInputStream()) {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            metadata.setContentType(file.getContentType());

            amazonS3.putObject(new PutObjectRequest(bucket, filename, inputStream, metadata));
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file", e);
        }

        String fileUrl = String.valueOf(amazonS3.getUrl(bucket, filename));

        return new FileInfo(filename, fileUrl);
    }

    /**
     * 다중 파일 업로드
     * 이미 S3에 동일한 파일명이 존재하면 업로드하지 않음
     */
    @Override
    public List<FileInfo> uploadFiles(FileData fileData, List<MultipartFile> files, String... id) {
        List<FileInfo> fileInfos = new ArrayList<>();

        for (MultipartFile file : files) {
            if (file == null) {
                throw new IllegalArgumentException("File cannot be null");
            }

            String filename = makeFilename(file.getOriginalFilename(), id);
            
            // S3에 이미 동일한 키로 파일이 존재하는지 확인
            if (isFileExists(filename)) {
                // 이미 존재하는 파일이면 URL만 반환
                String fileUrl = String.valueOf(amazonS3.getUrl(bucket, filename));
                fileInfos.add(new FileInfo(filename, fileUrl));
                continue;
            }

            try (InputStream inputStream = file.getInputStream()) {
                ObjectMetadata metadata = new ObjectMetadata();
                metadata.setContentLength(file.getSize());
                metadata.setContentType(file.getContentType());

                amazonS3.putObject(new PutObjectRequest(bucket, filename, inputStream, metadata));
            } catch (IOException e) {
                throw new RuntimeException("Failed to upload file", e);
            }

            String fileUrl = String.valueOf(amazonS3.getUrl(bucket, filename));
            fileInfos.add(new FileInfo(filename, fileUrl));
        }
        
        return fileInfos;
    }

    /**
     * S3에 파일이 이미 존재하는지 확인
     * @param filename 확인할 파일명
     * @return 존재하면 true, 아니면 false
     */
    private boolean isFileExists(String filename) {
        try {
            return amazonS3.doesObjectExist(bucket, filename);
        } catch (Exception e) {
            // 오류 발생 시 파일이 없다고 간주
            return false;
        }
    }

    @Override
    public void deleteFile(FileEntity file) {
        String fileUrl = file.getFilePath();
        if (fileUrl == null || fileUrl.isEmpty()) {
            throw new IllegalArgumentException("File URL cannot be null or empty");
        }

        String expectedPrefix = "https://" + bucket + ".s3.";
        if (!fileUrl.startsWith(expectedPrefix)) {
            throw new IllegalArgumentException("Invalid S3 URL: URL must start with " + expectedPrefix);
        }

        String objectKey = extractObjectKeyFromUrl(fileUrl);
        amazonS3.deleteObject(new DeleteObjectRequest(bucket, objectKey));
    }

    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            throw new IllegalArgumentException("File URL cannot be null or empty");
        }

        String expectedPrefix = "https://" + bucket + ".s3.";
        if (!fileUrl.startsWith(expectedPrefix)) {
            throw new IllegalArgumentException("Invalid S3 URL: URL must start with " + expectedPrefix);
        }

        String objectKey = extractObjectKeyFromUrl(fileUrl);
        amazonS3.deleteObject(new DeleteObjectRequest(bucket, objectKey));
    }

    private String extractObjectKeyFromUrl(String fileUrl) {
        try {
            // URL에서 파일 경로 부분만 추출
            String path = fileUrl.substring(fileUrl.indexOf(bucket + "/") + bucket.length() + 1);
            if (path.isEmpty()) {
                throw new IllegalArgumentException("Invalid S3 URL: empty path");
            }
            return path;
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid S3 URL: " + fileUrl, e);
        }
    }

    public String makeFilename(String originalFilename, String... id) {
        StringBuilder filename = new StringBuilder();
        for (String word : id){
            filename.append(word).append("/");
        }
        return filename.append(originalFilename).toString();
    }

    public String makeFilename (String originalFilename, String userId){
        Integer count = 1;
        int split = originalFilename.lastIndexOf(".");
        String name = originalFilename.substring(0, split);
        String extending = originalFilename.substring(split);
        String addSide = "";

        while(new File(userId+name+addSide+extending).exists()){
            addSide = "(" + count + ")";
            count++;
        }
        return userId+name+addSide+extending;
    }

    public String makeFilename (String originalFilename){
        Integer count = 1;
        int split = originalFilename.lastIndexOf(".");
        String name = originalFilename.substring(0, split);
        String extending = originalFilename.substring(split);
        String addSide = "";

        while(new File(name+addSide+extending).exists()){
            addSide = "(" + count + ")";
            count++;
        }
        return name+addSide+extending;
    }
}

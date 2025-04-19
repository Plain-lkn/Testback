//package org.example.plain.domain.file.service;
//
//import lombok.RequiredArgsConstructor;
//import org.example.plain.domain.file.entity.FileEntity;
//import org.example.plain.domain.file.interfaces.FileService;
//import org.example.plain.domain.homework.repository.FileRepository;
//import org.example.plain.domain.user.entity.User;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Component;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.File;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.NoSuchElementException;
//import java.util.Objects;
//
//@Component
//@RequiredArgsConstructor
//public class SubmitFileServiceImpl implements FileService {
//
//    @Value(value = "${file.path}")
//    private String filepath;
//    private final FileRepository fileRepository;
//
//    public List<FileEntity> changeFile (List<MultipartFile> multifiles, User user){
//        List<FileEntity> files = new ArrayList<>();
//
//        multifiles.forEach(file -> {
//            FileEntity fileEntity = new FileEntity();
//            String filename = makeFilename(Objects.requireNonNull(file.getOriginalFilename()),user.getId());
//
//            saveFile(file,filename);
//
//            fileEntity.setFilename(filename);
//            fileEntity.setUser(user);
//            fileEntity.setFilePath(filepath+filename);
//
//            files.add(fileEntity);
//        });
//
//        return files;
//    }
//
//    public File getFile (String filename){
//        File file = new File(filepath+filename).exists() ? new File(filepath+filename):null;
//        return file;
//    }
//
//    @Override
//    public List<File> getFiles(String workId, String userId) {
//        return List.of();
//    }
//
//    public List<File> getFiles(List<FileEntity> fileEntities) {
//        List<File> files = new ArrayList<>();
//        for (FileEntity fileEntity:fileEntities){
//            File file1 = new File(fileEntity.getFilename());
//            files.add(file1);
//        }
//        return files;
//    }
//
//
//    public void deleteFile(Integer file_id) {
//        FileEntity entity = fileRepository.findById(file_id).orElseThrow(NullPointerException::new);
//
//        File file = new File(entity.getFilePath()+entity.getFilename());
//        if(file.exists()){
//            file.delete();
//            fileRepository.delete(entity);
//        }else {
//            throw new NoSuchElementException("파일이 존재하지 않습니다.");
//        }
//    }
//
//    @Override
//    public void deleteFile(String filename) {
//    }
//
//
//    public String makeFilename (String originalFilename, String userId){
//        Integer count = 1;
//        int split = originalFilename.lastIndexOf(".");
//        String name = originalFilename.substring(0, split);
//        String extending = originalFilename.substring(split);
//        String addSide = "";
//
//        while(new File(userId+name+addSide+extending).exists()){
//            addSide = "(" + count + ")";
//            count++;
//        }
//        return userId+name+addSide+extending;
//    }
//
//    public String makeFilename (String originalFilename){
//        Integer count = 1;
//        int split = originalFilename.lastIndexOf(".");
//        String name = originalFilename.substring(0, split);
//        String extending = originalFilename.substring(split);
//        String addSide = "";
//
//        while(new File(name+addSide+extending).exists()){
//            addSide = "(" + count + ")";
//            count++;
//        }
//        return name+addSide+extending;
//    }
//
//    // 파일을 서버나 S3에 저장하기 위한 것.
//    public File saveFile(MultipartFile file, String filename) {
//        filename = makeFilename(filename);
//
//        File file1 = new File(filepath+filename);
//        try {
//            file.transferTo(file1);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//        return file1;
//    }
//
//
//
//
//}

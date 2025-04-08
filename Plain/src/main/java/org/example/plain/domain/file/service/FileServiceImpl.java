package org.example.plain.domain.file.service;

import org.example.plain.domain.file.entity.FileEntity;
import org.example.plain.domain.file.interfaces.FileService;
import org.example.plain.domain.homework.entity.WorkMemberEntity;
import org.example.plain.domain.homework.entity.WorkMemberId;
import org.example.plain.repository.WorkMemberRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class FileServiceImpl implements FileService {

    private final WorkMemberRepository workMemberRepository;


    @Value("${file.path}")
    private String filepath;

    public FileServiceImpl(WorkMemberRepository workMemberRepository) {
        this.workMemberRepository = workMemberRepository;
    }

    @Override
    public File getFile (String filename){
        File file = new File(filepath+filename).exists() ? new File(filepath+filename):null;
        return file;
    }

    @Override
    public List<File> getFiles(String workId, String userId) {
        WorkMemberEntity workMember = workMemberRepository.findById(new WorkMemberId(workId, userId)).orElseThrow();
        return this.getFiles(workMember.getFileEntities());
    }


    @Override
    public void deleteFile(String filename) {
        File file = new File(filepath+filename);
        if(file.exists()){
            file.delete();
        }else {
            throw new NoSuchElementException("파일이 존재하지 않습니다.");
        }
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

    public File saveFile(MultipartFile file, String filename) {
        File file1 = new File(filepath+filename);
        try {
            file.transferTo(file1);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return file1;
    }

    public List<File> getFiles(List<FileEntity> fileEntities) {
        List<File> files = new ArrayList<>();
        for (FileEntity fileEntity:fileEntities){
            File file1 = new File(fileEntity.getFilename());
            files.add(file1);
        }
        return files;
    }
}

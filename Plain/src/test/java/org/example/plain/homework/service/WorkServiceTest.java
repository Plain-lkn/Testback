//package org.example.plain.homework.service;
//
//
//import org.example.plain.common.enums.ClassType;
//import org.example.plain.common.enums.Role;
//import org.example.plain.domain.board.entity.BoardEntity;
//import org.example.plain.domain.classLecture.dto.ClassAddRequest;
//import org.example.plain.domain.classLecture.entity.ClassLecture;
//import org.example.plain.domain.classMember.entity.ClassMember;
//import org.example.plain.domain.classMember.entity.ClassMemberId;
//import org.example.plain.domain.homework.dto.Work;
//import org.example.plain.domain.homework.dto.WorkSubmitField;
//import org.example.plain.domain.homework.entity.WorkEntity;
//import org.example.plain.domain.homework.entity.WorkMemberEntity;
//import org.example.plain.domain.homework.interfaces.WorkService;
//import org.example.plain.domain.file.repository.FileRepository;
//import org.example.plain.domain.homework.service.CloudWorkServiceImpl;
//import org.example.plain.domain.user.dto.CustomUserDetails;
//import org.example.plain.domain.user.entity.User;
//import org.example.plain.repository.BoardRepository;
//import org.example.plain.repository.GroupMemberRepository;
//import org.example.plain.repository.WorkMemberRepository;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.ArgumentCaptor;
//import org.mockito.Mockito;
//import org.springframework.mock.web.MockMultipartFile;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.Authentication;
//import org.springframework.test.context.TestPropertySource;
//import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
//import org.springframework.web.client.HttpClientErrorException;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.time.LocalDateTime;
//import java.time.Month;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Optional;
//
//import static org.assertj.core.api.Assertions.*;
//import static org.junit.jupiter.api.Assertions.assertThrows;
//
//@TestPropertySource(properties = {"file.path=src/main/resources/static/files/"})
//@SpringJUnitConfig
//public class WorkServiceTest {
//
//    private WorkService workService;
//    private WorkMemberRepository workMemberRepository;
//    private BoardRepository boardRepository;
//    private WorkSubmitFieldRepository workSubmitFieldRepository;
//    private GroupMemberRepository groupMemberRepository;
//    private List<WorkEntity> workEntities;
//    private List<Work> works;
//    private List<ClassLecture> classLectures;
//    private List<User> users;
//    private FileRepository fileRepository;
//
//    @BeforeEach
//    public void init(){
//        workMemberRepository = Mockito.mock(WorkMemberRepository.class);
//        boardRepository = Mockito.mock(BoardRepository.class);
//        workSubmitFieldRepository = Mockito.mock(WorkSubmitFieldRepository.class);
//        groupMemberRepository = Mockito.mock(GroupMemberRepository.class);
//        fileRepository = Mockito.mock(FileRepository.class);
//        workService = new CloudWorkServiceImpl(boardRepository, workSubmitFieldRepository, workMemberRepository, groupMemberRepository, fileRepository);
//
//
//        works =  new ArrayList<>();
//        works.add(new Work("rlaqhfem","flsals","parkdea","123","스프링 공부하기","당장 일해라", LocalDateTime.of(2025, Month.FEBRUARY,28,12,00)));
//        workEntities = new ArrayList<>();
//        workEntities.add(WorkEntity.workToWorkEntity(works.get(0)));
//
//        users = new ArrayList<>();
//        users.add(new User("kimsee", Role.NORMAL,"김말년","1111","kim@gmail.com"));
//        users.add(new User("parkdea", Role.LEADER_CLASS,"박대철","1111","park@gmail.com"));
//
//        classLectures = new ArrayList<>();
//        classLectures.add(new ClassAddRequest("class1","로동 랜드",100L,1212L, ClassType.LECTURE,"로동을 배우는 클래스").toEntity(users.get(1),null));
//
//        Mockito.when(groupMemberRepository.findById(new ClassMemberId("class1","parkdea"))).thenReturn(Optional.of(new ClassMember(classLectures.get(0), users.get(1))));
//    }
//
//    @Test
//    public void saveWork(){
//        CustomUserDetails customUserDetails = new CustomUserDetails(users.get(1));
//        Authentication authentication = new UsernamePasswordAuthenticationToken(customUserDetails,"1234",null);
//
//        Work work = new Work();
//
//        ArgumentCaptor<WorkEntity> argumentCaptor =  ArgumentCaptor.forClass(WorkEntity.class);
//        workService.insertWork(work,"class1",authentication);
//
//        Mockito.verify(boardRepository).save(Mockito.any(BoardEntity.class));
//        Mockito.verify(boardRepository).save(argumentCaptor.capture());
//
//        assertThat(argumentCaptor.getValue().getWorkId()).isNotNull();
//        assertThat(argumentCaptor.getValue().getUserId()).isEqualTo("parkdea");
//    }
//
//    @Test
//    public void updateWork(){
//        WorkEntity mock = WorkEntity.workToWorkEntity(works.get(0));
//        mock.setUser(users.get(1));
//        mock.setGroup(classLectures.get(0));
//
//        Mockito.when(boardRepository.findByWorkId(Mockito.any())).thenReturn(Optional.of(mock));
//
//        CustomUserDetails customUserDetails = new CustomUserDetails(users.get(1));
//        Authentication authentication = new UsernamePasswordAuthenticationToken(customUserDetails,"1234",null);
//
//        Work work = new Work();
//        workService.updateWork(work,works.get(0).getWorkId(),customUserDetails.getUser().getId());
//
//        Mockito.verify(boardRepository).save(Mockito.any(BoardEntity.class));
//    }
//
//    @Test
//    public void updateBlockWork(){
//        WorkEntity mock = WorkEntity.workToWorkEntity(works.get(0));
//        mock.setUser(users.get(1));
//        mock.setGroup(classLectures.get(0));
//
//        Mockito.when(boardRepository.findByWorkId(Mockito.any())).thenReturn(Optional.of(mock));
//
//        CustomUserDetails customUserDetails = new CustomUserDetails(users.get(0));
//        Authentication authentication = new UsernamePasswordAuthenticationToken(customUserDetails,"1234",null);
//
//        Work work = new Work();
//        assertThrows(HttpClientErrorException.class,() ->
//                        workService.updateWork(work,works.get(0).getWorkId(),customUserDetails.getUser().getId()
//        ));
//
//        Mockito.verify(boardRepository, Mockito.never()).save(Mockito.any(BoardEntity.class));
//    }
//
//    @Test
//    public void submitWork(){
//        WorkEntity mock = WorkEntity.workToWorkEntity(works.get(0));
//        mock.setUser(users.get(1));
//        mock.setGroup(classLectures.get(0));
//        Mockito.when(boardRepository.findByWorkId(Mockito.any())).thenReturn(Optional.of(mock));
//
//        WorkMemberEntity mockGroupMember = WorkMemberEntity.makeWorkMemberEntity(users.get(0),mock);
//        Mockito.when(workMemberRepository.findById(Mockito.any()))
//                .thenReturn(Optional.of(mockGroupMember));
//
//        ClassMember mockGroupMember2 = Mockito.mock(ClassMember.class);
//        Mockito.when(mockGroupMember2.getUser()).thenReturn(users.get(0)); // getUser() 호출 시 users.get(0) 반환
//        Mockito.when(groupMemberRepository.findById(Mockito.any()))
//                .thenReturn(Optional.of(mockGroupMember2));
//
//
//        List<MultipartFile> files = new ArrayList<>();
//        files.add(new MockMultipartFile("myfile","myfile.txt", (String) null, (byte[]) null));
//        files.add(new MockMultipartFile("myfile","myfile2.jpg", (String) null, (byte[]) null));
//        files.add(new MockMultipartFile("myfile","myfile2.jpg", (String) null, (byte[]) null));
//
//
//        WorkSubmitField workSubmitField = WorkSubmitField.builder()
//                .workId(mock.getWorkId())
//                .userId(users.get(0).getId())
//                .file(files)
//                .build();
//
//        workService.submitWork(workSubmitField);
//
//        Mockito.verify(workMemberRepository).save(Mockito.any(WorkMemberEntity.class));
//        Mockito.verify(workSubmitFieldRepository).save(Mockito.any(WorkSubmitFieldEntity.class));
//    }
//
//    @Test
//    public void checkSubmitWork(){
//        //WorkSubmitFieldEntity workSubmitFieldEntity =
//
//        //Mockito.when(workSubmitFieldRepository.findById(Mockito.any())).thenReturn(Optional.of(workSubmitFieldEntity));
//
//        workService.getWorkResults(workEntities.get(0).getWorkId(),users.get(0).getId());
//    }
//
//}

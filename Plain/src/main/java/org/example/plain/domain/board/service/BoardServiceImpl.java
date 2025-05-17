package org.example.plain.domain.board.service;

import org.example.plain.domain.board.dao.BoardDao;
import org.example.plain.domain.board.dto.Board;
import org.example.plain.domain.board.entity.BoardEntity;
import org.example.plain.domain.board.interfaces.BoardService;
import org.example.plain.domain.homework.dto.Work;
import org.example.plain.domain.homework.entity.WorkEntity;
import org.example.plain.repository.BoardRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class BoardServiceImpl implements BoardService {

    private final BoardRepository boardRepository;
    BoardDao boardDao;

    BoardServiceImpl(BoardDao boardDao, BoardRepository boardRepository) {
        this.boardDao = boardDao;
        this.boardRepository = boardRepository;
    }

    @Override
    public Board getBoard(String id) {
        BoardEntity board = boardRepository.findByBoardId(id).orElseThrow();
        if (board instanceof WorkEntity) {
            return Work.changeWorkEntity((WorkEntity) board);
        }else {
            return Board.changeBoard((board));
        }
    }

    public List<Board> selectKeyword(String classId,String keyword) {
        List<BoardEntity> board = boardRepository.findBoardByClassIdAndKeyword(classId, keyword).orElseThrow();
        List<Board> boards = board.stream().map(boardEntity -> Board.changeBoard(boardEntity)).toList();
        return boards;
    }

    @Override
    public void createBoard(Board board) {
        boardDao.save(transportBoardEntity(board));
    }

    @Override
    @Transactional
    public void updateBoard(Board board, String boardId, String userId) {
        BoardEntity originalBoard = boardDao.findById(boardId);
        if(board.getTitle() != null) {
            originalBoard.setTitle(board.getTitle());
        }
        if (board.getContent() != null) {
            originalBoard.setContent(board.getContent());
        }
        boardDao.update(originalBoard);
    }

    @Override
    public void deleteBoard(String id, String userId) {
        BoardEntity originalBoard = boardDao.findById(id);
        if(originalBoard != null) {
            boardDao.delete(originalBoard);
        }
    }

    @Override
    public List<Board> getGroupBoards(String groupId) {
        List<BoardEntity> groupBoard = boardDao.findAllByGroupId(groupId);
        List<Board> boards = new ArrayList<>();
        for (BoardEntity boardEntity : groupBoard) {
            boards.add(Board.changeBoard(boardEntity));
        }
        return boards;
    }

    private BoardEntity transportBoardEntity(Board board) {
        BoardEntity boardEntity = new BoardEntity();
        boardEntity.setTitle(board.getTitle());
        boardEntity.setContent(board.getContent());
        boardEntity.setClassId(board.getGroupId());
        return boardEntity;
    }


}

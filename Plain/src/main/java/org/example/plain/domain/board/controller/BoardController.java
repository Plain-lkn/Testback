package org.example.plain.domain.board.controller;

import org.example.plain.domain.board.interfaces.BoardService;
import org.example.plain.domain.board.service.BoardServiceImpl;
import org.example.plain.domain.board.dto.Board;
import org.example.plain.domain.user.dto.UserRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/board")
public class BoardController {

    private final BoardService boardService;

    public BoardController(BoardService boardService) {
        this.boardService = boardService;
    }

    @GetMapping("/{board_id}")
    public ResponseEntity<Board> getBoard(@PathVariable String board_id) {
        return ResponseEntity.ok(boardService.getBoard(board_id));
    }

    @GetMapping("/list")
    public ResponseEntity<List<Board>> getGroupBoardList(@RequestParam String group_id){
        return ResponseEntity.ok().body(boardService.getGroupBoards(group_id));
    }

    @PostMapping("/insert")
    public ResponseEntity<?> insertBoard(@RequestBody Board board) {
        boardService.createBoard(board);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/update")
    public ResponseEntity<?> updateBoard(@RequestBody Board board, @RequestParam String board_id, UserRequest userRequest) {
        boardService.updateBoard(board, board_id, userRequest.getId());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{board_id}")
    public ResponseEntity<?> deleteBoard(@PathVariable String board_id, UserRequest userRequest) {
        boardService.deleteBoard(board_id, userRequest.getId());
        return ResponseEntity.ok().build();
    }
}

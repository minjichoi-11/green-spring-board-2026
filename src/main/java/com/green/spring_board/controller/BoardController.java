package com.green.spring_board.controller;

import com.green.spring_board.dto.BoardResponse;
import com.green.spring_board.dto.BoardUpdateRequest;
import com.green.spring_board.exceptions.ResourceNotFoundException;
import com.green.spring_board.exceptions.UnauthenticatedException;
import com.green.spring_board.exceptions.UserRequestException;
import com.green.spring_board.dto.BoardCreateRequest;
import com.green.spring_board.service.BoardService;
import com.green.spring_board.entity.Board;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/board")
@AllArgsConstructor
public class BoardController {

    private final BoardService boardService;

    //* 전체 조회
    @GetMapping
    public ResponseEntity<List<BoardResponse>> getBoards() {
        return ResponseEntity.ok(
                boardService.getAllBoards()
        );
    }

    //* 상세 조회
    @GetMapping ("/{id}")
    public ResponseEntity<BoardResponse> getBoardsDetail(@PathVariable int id) {
        try {
            BoardResponse board = boardService.getBoard(id);
            if (board == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(board);
        } catch (ResourceNotFoundException e) {
            // 게시글을 못 찾았을 때 (404)
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            // 위에도 아니면, 무조건 Java 아니면 DB 에러로 서버 에러 (500)
            return ResponseEntity.internalServerError().build();
        }
    }

    //* 삽입
    @PostMapping
    public ResponseEntity<Void> createBoard(
            @Valid @RequestBody BoardCreateRequest boardCreateRequest,
            HttpServletRequest httpServletRequest

    ) {
            // 1. 이 사람의 세션을 가져옴
            HttpSession session = httpServletRequest.getSession(false);

            if (session == null || session.getAttribute("userId") == null) {
                throw new UnauthenticatedException("로그인이 필요합니다.");
            }

            int userId = (int) session.getAttribute("userId");
            int newBoardId = boardService.createBoard(boardCreateRequest, userId);
            URI location = URI.create("/api/board/" + newBoardId);
            return ResponseEntity.created(location).build();

    }

    //* 수정
    @PatchMapping("/{id}")
    public ResponseEntity<Void> updateBoard(
            @PathVariable int id,
            @Valid @RequestBody BoardUpdateRequest boardUpdateRequest,
            HttpServletRequest httpServletRequest

    ) {
        HttpSession session = httpServletRequest.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            throw new UnauthenticatedException("로그인이 필요합니다.");
        }
        boardService.updateBoard(id, boardUpdateRequest);
        return ResponseEntity.ok().build(); // 성공 시 반환

    }


    //* 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBoard(
            @PathVariable int id,
            HttpServletRequest httpServletRequest
    ) {
        HttpSession session = httpServletRequest.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            throw new UnauthenticatedException("로그인이 필요합니다.");
        }

        boardService.deleteBoard(id);
        return ResponseEntity.noContent().build();
    }
}

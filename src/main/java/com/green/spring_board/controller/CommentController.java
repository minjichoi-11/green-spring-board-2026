package com.green.spring_board.controller;

import com.green.spring_board.dto.ApiResponse;
import com.green.spring_board.dto.CommentRequest;
import com.green.spring_board.dto.CommentResponse;
import com.green.spring_board.entity.Comment;
import com.green.spring_board.exceptions.UnauthenticatedException;
import com.green.spring_board.service.CommentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping ("/api")
@AllArgsConstructor

public class CommentController {
    private final CommentService commentService;

    //* 댓글 작성
    @PostMapping("/board/{id}/comment") //* 예를 들어, 1번 게시글의 댓글을 작성(postmapping)
    public ResponseEntity<ApiResponse<Void>> createComment(
            @PathVariable int id,
            @RequestBody CommentRequest commentRequest,
            HttpServletRequest httpServletRequest
    ) {

        HttpSession session = httpServletRequest.getSession(false);

        if(session == null || session.getAttribute("userId") == null) {
            throw new UnauthenticatedException("로그인이 필요합니다.");
        }
        int userId = (int) session.getAttribute("userId");

        commentService.createComment(commentRequest, userId, id);

        return ResponseEntity.ok(ApiResponse.ok());

    }

    //* 댓글 조회
    @GetMapping("/board/{id}/comment")
    public ResponseEntity<ApiResponse<List<CommentResponse>>> readComment(
            @PathVariable int id
            // 비회원도 댓글 조회가 가능하도록 세션 검사는 하지 않음
    ){
        return ResponseEntity.ok(
                ApiResponse.ok(commentService.readComments(id))
        );
    }

    //* 댓글 수정
    @PatchMapping("comment/{id}")
    public ResponseEntity<ApiResponse<Void>> updateComment(
            @RequestBody CommentRequest commentCreateRequest,
            @PathVariable int id,
            HttpServletRequest httpServletRequest
    ){
        HttpSession session = httpServletRequest.getSession(false);

        if(session == null || session.getAttribute("userId") == null) {
            throw new UnauthenticatedException("로그인이 필요합니다.");
        }
        int userId = (int) session.getAttribute("userId");

        commentService.updateComment(commentCreateRequest, id, userId);
        return ResponseEntity.ok(ApiResponse.ok());
    }


    //* 댓글 삭제
    @DeleteMapping("comment/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @PathVariable int id,
            HttpServletRequest httpServletRequest
    ){
        HttpSession session = httpServletRequest.getSession(false);

        if(session == null || session.getAttribute("userId") == null) {
            throw new UnauthenticatedException("로그인이 필요합니다.");
        }
        int userId = (int) session.getAttribute("userId");

        commentService.deleteComment(id, userId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(ApiResponse.ok());
    }

}

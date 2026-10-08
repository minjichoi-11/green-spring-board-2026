package com.green.spring_board.controller;

import com.green.spring_board.dto.ApiResponse;
import com.green.spring_board.dto.CommentRequest;
import com.green.spring_board.entity.Comment;
import com.green.spring_board.exceptions.UnauthenticatedException;
import com.green.spring_board.service.CommentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping ("/api")
@AllArgsConstructor

public class CommentController {
    private final CommentService commentService;

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
}

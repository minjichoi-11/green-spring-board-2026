package com.green.spring_board.controller;

import com.green.spring_board.dto.*;
import com.green.spring_board.exceptions.UnauthenticatedException;
import com.green.spring_board.service.BoardService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
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
    public ResponseEntity<ApiResponse<Page<BoardResponse>>> getBoards(

            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "latest") String order
    ){

        int userId = -1;

        return ResponseEntity.ok(
                ApiResponse.ok(boardService.getAllBoards(page, size, order))
        );
    }

    //* 상세 조회
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BoardResponse>> getBoardDetail(
            @PathVariable int id

    ){
        int userId = -1; // 비회원일 때 기본값 설정

        BoardResponse board = boardService.getBoard(userId);
        return ResponseEntity.ok(
                ApiResponse.ok(board)
        );
    }

    //* 내 글 조회
    @GetMapping("/my-boards")
    public ResponseEntity<ApiResponse<List<BoardResponse>>> getMyBoards(
    ){

        List<BoardResponse> response = boardService.getMyBoards();

        return ResponseEntity.ok(
                ApiResponse.ok(response)
        );
    }

    //* 삽입
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createBoard(
            @Valid @RequestBody BoardCreateRequest boardCreateRequest
    ) {
        int newBoardId = boardService.createBoard(boardCreateRequest);
        URI location = URI.create("/api/board/" + newBoardId);
        return ResponseEntity.created(location).body(ApiResponse.ok());
    }

    //* 수정
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> updateBoard(
            @PathVariable int id,
            @Valid @RequestBody BoardUpdateRequest boardUpdateRequest

    ) {
        boardService.updateBoard(id, boardUpdateRequest);
        return ResponseEntity.ok(ApiResponse.ok());
    }


    //* 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBoard(
            @PathVariable int id
    ) {

        //* 둘 중 어느 방법을 쓸지는 속한 팀, 조직 컨벤션 따르기

        //* 삭제 성공 시 응답 방법 1.
        //* 200 + ApiResponse<Void>

        //* 삭제 성공 시 응답 방법 2.
        //* 204(No Content) + No Body

        boardService.deleteBoard(id);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    //* 좋아요
    @PostMapping("/like/{id}")
    public ResponseEntity<ApiResponse<Void>> likeBoard(
            @PathVariable int id
            ) {

        boardService.pressLike(id);
        return ResponseEntity.ok(ApiResponse.ok());

        //* 다시 눌렀을 때 취소
        //* 좋아요 수
        //* 상세 눌렀을 때 어느 유저들이 게시글에 좋아요를 눌렀는지
        //* 내가 이 게시글 좋아요눌렀는지

    }

    @GetMapping("/like/{id}")
    public ResponseEntity<ApiResponse<LikeDetailResponse>> viewletDetails(
            @PathVariable int id
    ) {

        // 이 게시글에 좋아요 누른 유저들의 유저명
        LikeDetailResponse response = boardService.getLikeDetail(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

}

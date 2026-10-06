package com.green.spring_board.controller;

import com.green.spring_board.dto.*;
import com.green.spring_board.entity.User;
import com.green.spring_board.exceptions.ResourceConflictException;
import com.green.spring_board.exceptions.ResourceNotFoundException;
import com.green.spring_board.exceptions.UnauthenticatedException;
import com.green.spring_board.exceptions.UserRequestException;
import com.green.spring_board.repository.UserRepository;
import com.green.spring_board.service.BoardService;
import com.green.spring_board.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/user")
@AllArgsConstructor

public class UserController {
    private final UserService userService;
    private final UserRepository userRepository;
    private final BoardService boardService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Void>> signup(
            @Valid @RequestBody SignupRequest signupRequest,
            HttpServletRequest httpServletRequest
    ) {
        userService.signup(signupRequest); // 실패하면 서비스에서 예외를 던짐.
        return ResponseEntity.ok(ApiResponse.ok());
        }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Integer>> login(
            @Valid @RequestBody LoginRequest loginRequest,
            HttpServletRequest httpServletRequest
    ){
        //* DTO Vaild
            int userId = userService.login(loginRequest);

            HttpSession session = httpServletRequest.getSession();
            httpServletRequest.changeSessionId();
            session.setAttribute("userId", userId);

            return ResponseEntity.ok(ApiResponse.ok(userId));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<MyInfoResponse>> getCurrentUser(
            HttpServletRequest httpServletRequest
    ){
        // 1. 이 사람의 세션을 가져옴
        HttpSession session = httpServletRequest.getSession(false);

        if(session == null || session.getAttribute("userId") == null) {
            return ResponseEntity.status(401).build();
        }

        // 2. 세션에서 유저 아이디 뽑아옴
        int userId = (int) session.getAttribute("userId");
        MyInfoResponse response = userService.getUserInfo(userId);

        ResponseEntity.ok(ApiResponse.ok(response));

        return ResponseEntity.ok().build();
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            HttpServletRequest request
    ){
        HttpSession session = request.getSession(false);

        if(session == null || session.getAttribute("userId") == null) {
            return ResponseEntity.status(401).build();
        }

        session.invalidate();
        return ResponseEntity.ok().build();
    }

    @PatchMapping
    public ResponseEntity<ApiResponse<Void>> updateUserInfo(
            HttpServletRequest request,
            @Valid @RequestBody UserUpdateRequest userUpdateRequest
    ){
        HttpSession session = request.getSession(false);
        if(session == null || session.getAttribute("userId") == null) {
            return ResponseEntity.status(401).build();
        }
        int userId = (int) session.getAttribute("userId");
        userService.updateUserInfo(userId, userUpdateRequest);
        return ResponseEntity.ok().build();
    }

    // 유저 탈퇴 기능
    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            HttpServletRequest request
    ){
        HttpSession session = request.getSession(false);
        if(session == null || session.getAttribute("userId") == null) {
            return ResponseEntity.status(401).build();
        }
        int userId = (int) session.getAttribute("userId");

        // 1. DB 삭제
        userService.deleteUser(userId);
        // 2. 세션 비활성화
        session.invalidate();

        return ResponseEntity.noContent().build();
    }
}


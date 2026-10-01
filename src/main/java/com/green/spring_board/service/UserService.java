package com.green.spring_board.service;

import com.green.spring_board.dto.SignupRequest;
import com.green.spring_board.entity.User;
import com.green.spring_board.exceptions.ResourceConflictException;
import com.green.spring_board.exceptions.UserRequestException;
import com.green.spring_board.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor

public class UserService {
    //* 암호화 객체 생성
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public void signup(SignupRequest signupRequest) {
        //* 이메일과 비밀번호가 공백이 아닌지 확인
        if (signupRequest.getEmail().isBlank() || signupRequest.getPassword().isBlank()) {
            throw new UserRequestException("Email or password cannot be blank");
        }

        //* 이메일이 사용 중인지 확인
        if (userRepository.existsByEmail(signupRequest.getEmail()))
            throw new ResourceConflictException("Email already exists");

        //* 비밀번호 해싱(가입 시 암호화하여 엔티티에 세팅)
        String hashedPassword = passwordEncoder.encode(signupRequest.getPassword());

        //* DB save
        User user = new User();
        user.setEmail(signupRequest.getEmail());
        user.setPassword(hashedPassword); //* 암호화된 비밀번호 저장
        user.setNickname(signupRequest.getNickname());

        userRepository.save(user);
    }
}

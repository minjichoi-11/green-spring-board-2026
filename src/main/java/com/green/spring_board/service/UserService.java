package com.green.spring_board.service;

import com.green.spring_board.dto.LoginRequest;
import com.green.spring_board.dto.MyInfoResponse;
import com.green.spring_board.dto.SignupRequest;
import com.green.spring_board.dto.UserUpdateRequest;
import com.green.spring_board.entity.User;
import com.green.spring_board.exceptions.*;
import com.green.spring_board.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

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

    public int login(LoginRequest loginRequest) {
        //* 1. 이메일이 존재하는지 확인
        Optional<User> userOptional = userRepository.findByEmail(loginRequest.getEmail());

        if (userOptional.isEmpty()) {
            throw new ResourceNotFoundException("User not found");
        }
        User user = userOptional.get(); //? 이 이메일의 사용자 정보

        //* 2. 비밀번호가 올바른지 확인
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new UnauthenticatedException("Wrong password");
        }

        //* 3. 로그인 성공
        return user.getId();
    }

    public MyInfoResponse getUserInfo(int userId) {
        //* 유저 아이디로 DB 조회. 변수 이름: userOptional
        Optional<User> userOptional = userRepository.findById(userId);
        // DB에 가서 Optional을 하나 가져옴
        // 여기에는 유정 정보가 들어 있을 수도, 주소를 잘못 찾았다면 빈 상자일 수도 있다.
        // 그래서 아직은 유저 안의 정보를 바로 쓸 수 없다.

        //* 존재하지 않는 유저라면 예외 발생
        if (userOptional.isEmpty()) {
            throw new ResourceNotFoundException("User not found");
        }

        //? 실제 유저 객체를 꺼냄
        User user = userOptional.get();

        //* 3. 유저 아이디로 DB 조회함

        //* 4. DB에서 이 유저의 닉네임과 이메일을 받아옴(실제 유저 객체를 꺼냄)
        String email = user.getEmail();
        String nickname = user.getNickname();

        //* 5. DB에서 가져온 유저의 이메일과 닉네임을 응답 객체(DTO)에 담아서 돌려줌.
        MyInfoResponse myinfoResponse = new MyInfoResponse();
        myinfoResponse.setEmail(email);
        myinfoResponse.setNickname(nickname);

        return myinfoResponse;
    }

    public void updateUserInfo(int userId, UserUpdateRequest userUpdateRequest) {
        Optional<User> userOptional = userRepository.findById(userId);

        //* 존재하지 않는 유저라면 예외 발생
        if (userOptional.isEmpty()) {
            throw new ResourceNotFoundException("User not found");
        }

        //? 실제 유저 객체를 꺼냄
        User user = userOptional.get();

        //* [핵심] 사용자가 보낸 이메일 값이 null이 아니고 공백이 아닐 때만 덮어쓴다.
        // 이메일
        if(userUpdateRequest.getEmail() != null
                && !userUpdateRequest.getEmail().isBlank()
                && !userUpdateRequest.getEmail().equals(user.getEmail())
        ){
            user.setEmail(userUpdateRequest.getEmail());
        }

        // 닉네임 체크
        if(userUpdateRequest.getNickname() != null && !userUpdateRequest.getNickname().isBlank()
        ) {
            user.setNickname(userUpdateRequest.getNickname());
        }
        userRepository.save(user);
    }

    public void deleteUser(int userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new ResourceNotFoundException("User not found");
        }
        User user = userOptional.get();

        if (user.getId() != userId) {
            throw new AuthorizationFailureException("본인만 탈퇴할 수 있습니다.");
        }
        userRepository.delete(user);
    }

}

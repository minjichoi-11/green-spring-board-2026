package com.green.spring_board.service;

import com.green.spring_board.dto.*;
import com.green.spring_board.entity.RefreshToken;
import com.green.spring_board.entity.User;
import com.green.spring_board.exceptions.*;
import com.green.spring_board.global.UserState;
import com.green.spring_board.repository.RefreshTokenRepository;
import com.green.spring_board.repository.UserRepository;
import com.green.spring_board.security.JwtTokenProvider;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor

public class UserService {
    //* 암호화 객체 생성
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    public void signup(SignupRequest signupRequest) {
        //* 이메일과 비밀번호가 공백이 아닌지 확인
        if (signupRequest.getEmail().isBlank() || signupRequest.getPassword().isBlank()) {
            throw new UserRequestException("Email or password cannot be blank");
        }

        //* 아이디가 사용 중인지 확인
        if( userRepository.existsByUsername(signupRequest.getUsername()) ){
            throw new ResourceConflictException("Username already exists");
        }

        //* 이메일이 사용 중인지 확인
        if (userRepository.existsByEmail(signupRequest.getEmail()))
            throw new ResourceConflictException("Email already exists");

        //* 비밀번호 해싱(가입 시 암호화하여 엔티티에 세팅)
        String hashedPassword = passwordEncoder.encode(signupRequest.getPassword());

        //* DB save
        User user = new User();
        user.setUsername(signupRequest.getUsername());
        user.setPassword(hashedPassword); //* 암호화된 비밀번호 저장
        user.setNickname(signupRequest.getNickname());
        user.setState(UserState.ACTIVE);

        userRepository.save(user);
    }

    public LoginResponse login(LoginRequest loginRequest) {

        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsername(), loginRequest.getPassword()
            ));
        } catch (AuthenticationException e) {
            throw new UnauthenticatedException("아이디 또는 비밀번호가 올바르지 않습니다.");
        }


        //* 1. 이메일이 존재하는지 확인
        User user = userRepository.findByUsername(loginRequest.getUsername());

        if (user == null) {
            throw new ResourceNotFoundException("User not found");
        }

        if (user.getState() == UserState.QUITIED) {
            throw new ResourceNotFoundException("탈퇴된 회원입니다.");
        }

//        //* 2. 비밀번호가 올바른지 확인
//        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
//            throw new UnauthenticatedException("Wrong password");
//        }

        //* 액세스 토큰 발급
        String accessToken = tokenProvider.generateAccessToken(loginRequest.getUsername());
        String refreshToken = tokenProvider.generateRefreshToken(loginRequest.getUsername());

        //* 1. 해당 유저의 기존 리프레시 토큰(들) 삭제
        List<RefreshToken> tokens = refreshTokenRepository.findByUserId(user.getId());
        refreshTokenRepository.deleteAll();

        //* 2. 해당 유저의 신규 리프레시 토큰 저장
        Date expiration = tokenProvider.getExpiration(refreshToken);
        LocalDateTime expirationDateTime = expiration.toInstant().atZone(
                ZoneId.systemDefault()
        ).toLocalDateTime();

        RefreshToken refresh =  new RefreshToken();
        refresh.setUser(user);
        refresh.setToken(refreshToken);
        refresh.setExpirationDateTime(expirationDateTime);
        refreshTokenRepository.save(refresh);

        //* 3. 로그인 성공
        return new LoginResponse(accessToken, refreshToken);
    }

    public RefreshResponse refresh(@Valid RefreshRequest refreshRequest) {
        String userRefreshToken = refreshRequest.getRefreshToken();
        //* 1. 리프레시 토큰 위변조 여부 검증
        if (!tokenProvider.validRefreshToken(userRefreshToken)) {
            // 검증 실패 예외처리
            throw new UnauthenticatedException("만료되었거나 유효하지 않은 리프레시 토큰입니다.");
        }

        //* 2. DB 내 존재하는 리프레시 토큰까지 검증
        Optional<RefreshToken> optionalRT = refreshTokenRepository.findByToken(userRefreshToken);
        if (optionalRT.isEmpty()) {
            throw new UnauthenticatedException("유효하지 않은 리프레시 토큰입니다.");
        }

        //* 3. 해당 리프레시 토큰이 인증되지 않았는지 검증
        if (!optionalRT.get().getExpirationDateTime().isAfter(LocalDateTime.now())) {
            throw new UnauthenticatedException("만료된 리프레시 토큰입니다.");
        }

        //* 4. 토큰 내 유저네임의 유저 정보가 유효한지 검증
        //* 해당 유저가 존재하는지, 활성화 상태인지(탈퇴하지는 않았는지) 검증
        String username = tokenProvider.getUsernameFromToken(userRefreshToken);
        User user = userRepository.findByUsername(username);
        if (user == null || user.getState() != (UserState.ACTIVE)) {
            throw new UnauthenticatedException("인증할 수 없는 계정입니다.");
        }

        String accessToken = tokenProvider.generateAccessToken(username);

        return new RefreshResponse(accessToken);
    }

    public MyInfoResponse getUserInfo(int userId) {
        //* 유저 아이디로 DB 조회. 변수 이름: userOptional
        Optional<User> userOptional = userRepository.findById(userId);
        // DB에 가서 Optional을 하나 가져옴
        // 여기에는 유저 정보가 들어 있을 수도, 주소를 잘못 찾았다면 빈 상자일 수도 있다.
        // 그래서 아직은 유저 안의 정보를 바로 쓸 수 없다.

        //* 존재하지 않는 유저라면 예외 발생
        if (userOptional.isEmpty()) {
            throw new ResourceNotFoundException("User not found");
        }

        //? 실제 유저 객체를 꺼냄
        User user = userOptional.get();

        //* 3. 유저 아이디로 DB 조회함
        //*

        if (user.getState() == UserState.QUITIED) {
            throw new ResourceNotFoundException("탈퇴된 회원입니다.");
        }

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

        if (user.getState() == UserState.QUITIED) {
            throw new ResourceNotFoundException("탈퇴된 회원입니다.");
        }

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

        user.setState(UserState.QUITIED);
        userRepository.save(user);

    }

    public RefreshResponse refresh() {
        return null;
    }
}

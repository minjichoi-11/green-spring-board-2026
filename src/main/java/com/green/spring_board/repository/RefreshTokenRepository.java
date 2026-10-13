package com.green.spring_board.repository;

import com.green.spring_board.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Integer> {
    void deleteByUserId(Integer userId);
    Optional<RefreshToken> findByToken(String token);
}

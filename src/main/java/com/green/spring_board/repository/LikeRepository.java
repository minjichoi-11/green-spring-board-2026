package com.green.spring_board.repository;

import com.green.spring_board.entity.Like;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<Like, Integer> {
    Optional<Like> findByUserIdAndBoardId(int userId, int boardId);

    //* 로그인한 내가 이 글에 좋아요를 눌렀는지 여부 판단
    boolean existsByUserIdAndBoardId(int userId, int boardId);
    List<Like> findByBoardId(int boardId);
}

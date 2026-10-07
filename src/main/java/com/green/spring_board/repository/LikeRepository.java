package com.green.spring_board.repository;

import com.green.spring_board.entity.Like;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<Like, Integer> {
    //* SELECT *
    //* FROM likes
    //* WHERE user_id=3 AND board_id=9;
    Optional<Like> findByUserIdAndBoardId(int user_id, int board_id);
}

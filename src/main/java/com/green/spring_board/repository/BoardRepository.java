package com.green.spring_board.repository;

import com.green.spring_board.entity.Board;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BoardRepository extends JpaRepository<Board, Integer> { // Jpa는 무조건 Boards class로만 받게 되어 있음.
    List<Board> findByUserId(int userId);
}

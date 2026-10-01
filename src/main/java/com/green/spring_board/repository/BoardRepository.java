package com.green.spring_board.repository;

import com.green.spring_board.entity.Boards;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BoardRepository extends JpaRepository<Boards, Integer> { // Jpa는 무조건 Boards class로만 받게 되어 있음.

}

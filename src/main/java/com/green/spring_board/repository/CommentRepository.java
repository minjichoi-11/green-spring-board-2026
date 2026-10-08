package com.green.spring_board.repository;

import com.green.spring_board.entity.Comment;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface CommentRepository extends CrudRepository<Comment, Integer> {
        //* 특정 게시글에 달린 모든 댓글 목록 조회
        List<Comment> findByBoardIdAndIsDeletedFalse(int boardId);

//    //* 특정 회원이 작성한 모든 댓글 목록 조회
//    List<Comment> findByUserId(int userId);


    // 특정 게시글의 댓글 목록을 등록 순서대로 조회 (게시글 상세 조회용 메서드)
    // List<Comment> findByBoardIdOrderByCreatedDatetimeAsc(int boardId);
}

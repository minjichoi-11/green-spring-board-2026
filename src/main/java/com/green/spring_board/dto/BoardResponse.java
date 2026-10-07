package com.green.spring_board.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter

public class BoardResponse {

    int id; //* Board Id
    String title; //* 제목
    String content; //* 내용
    int hits; //* 조회수
    int likeCount; //* 좋아요 갯수
    boolean isLikedByme; //* 내가 좋아요 눌렀는지
    Integer authorId; //* 작성자 아이디
    String authorNickname; //* 작성자 닉네임
    LocalDateTime createdDatetime; //* 생성일시
    LocalDateTime updatedDatetime; //* 수정일시
}

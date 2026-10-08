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

public class CommentRequest {

    private String content;

//    int id; //* comment Id
//    String content; //* 댓글 내용
//    Integer authorId; //* 작성자 아이디
//    String authorNickName; //* 작성자 닉네임
//    LocalDateTime createdDatetime; //* 댓글 작성 일시

}

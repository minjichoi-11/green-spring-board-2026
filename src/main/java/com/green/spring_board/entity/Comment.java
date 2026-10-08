package com.green.spring_board.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "comments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable =  false)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY) // DB의 board_id 외래키와 매핑
    @JoinColumn(name = "board_id", nullable = false)
    private Board board;

    @ManyToOne(fetch = FetchType.LAZY) // DB의 user_id 외래키와 매핑
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, insertable = false, updatable = false) // updatable = false -> 생성 일시의 무결성 유지
    private LocalDateTime createdDatetime;

    @Column(nullable = false)
    private boolean isDeleted;

}

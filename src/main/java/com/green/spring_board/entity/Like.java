package com.green.spring_board.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity

//* 같은 유저가 같은 글에 좋아요를 두 번 저장하는 것을 DB가 막아주도록 설정
@Table(name = "likes",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "board_id"})
)

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class Like {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", nullable = false)
    private Board board;

    @Column(nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdDatetime;



}
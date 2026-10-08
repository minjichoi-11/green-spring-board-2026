package com.green.spring_board.service;

import com.green.spring_board.dto.CommentRequest;
import com.green.spring_board.dto.CommentResponse;
import com.green.spring_board.entity.Board;
import com.green.spring_board.entity.Comment;
import com.green.spring_board.entity.User;
import com.green.spring_board.exceptions.AuthorizationFailureException;
import com.green.spring_board.exceptions.ResourceNotFoundException;
import com.green.spring_board.exceptions.UnauthenticatedException;
import com.green.spring_board.repository.BoardRepository;
import com.green.spring_board.repository.CommentRepository;
import com.green.spring_board.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor

public class CommentService {
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final BoardRepository boardRepository;

    //* 댓글 작성
    public void createComment(
            CommentRequest CommentRequest,
            int userId,
            int boardId
    ) {
        Optional<User> userOptional = userRepository.findById(userId);
        Optional<Board> boardOptional = boardRepository.findById(boardId);

        if (userOptional.isEmpty()) {
            throw new ResourceNotFoundException("User not found");
        }

        if (boardOptional.isEmpty()) {
            throw new ResourceNotFoundException("Board not found");
        }

        User user = userOptional.get();
        Board board = boardOptional.get();

        Comment comment = new Comment();
        comment.setContent(CommentRequest.getContent());
        comment.setUser(user);
        comment.setBoard(board);
        commentRepository.save(comment);

    }

    //* 댓글 조회
    public List<CommentResponse> readComments(int boardId){
        if (!boardRepository.existsById(boardId)) {
            throw new ResourceNotFoundException("Board not found");
        }
        // 댓글은 가져왔는데, 이걸 이제 CommentResponse로 변환

        List<Comment> comments = commentRepository.findByBoardIdAndIsDeletedFalse(boardId);
        List<CommentResponse> commentResponses = new ArrayList<>();
        for(Comment comment : comments){
            CommentResponse commentResponse = new CommentResponse();

            commentResponse.setCommentId(comment.getId());
            commentResponse.setContent(comment.getContent());
            commentResponse.setNickname(comment.getUser().getNickname());
            commentResponse.setCommentDate(comment.getCreatedDatetime());

            commentResponses.add(commentResponse);
        }
        return commentResponses;
    }

    //* 댓글 수정
    public void updateComment(
            CommentRequest commentRequest,
            int commentId,
            int userId
    ) {
        //? 수정할 댓글이 진짜 존재하는지 조회(댓글 아이디)
        Optional<Comment> commentOptional = commentRepository.findById(commentId);
        //? 수정할 댓글이 없다면 에러 발생
        if (commentOptional.isEmpty()) {
            throw new ResourceNotFoundException("댓글을 찾을 수 없습니다.");
        }
        //? 데이터가 있으면 댓글 꺼내기
        Comment comment = commentOptional.get();

        if (comment.isDeleted()) {
            throw new ResourceNotFoundException("삭제된 댓글입니다.");
        }

        //? 댓글 작성자와 수정을 요청한 유저가 일치하는지
        if (comment.getUser().getId() != userId) {
            throw new UnauthenticatedException("댓글 수정 권한이 없습니다.");
        }

        if (commentRequest.getContent() != null) {
            comment.setContent(commentRequest.getContent());
            commentRepository.save(comment);
        }
    }

        //? 댓글 내용 수정



    //* 댓글 삭제
    public void deleteComment (int commentId, int userId) {

        //? 삭제할 댓글이 진짜 존재하는지 조회
        Optional<Comment> commentOptional = commentRepository.findById(commentId);
        //? 삭제할 댓글이 없다면 에러 발생
        if (commentOptional.isEmpty()) {
            throw  new ResourceNotFoundException("Comment not found");
        }
        //? 데이터가 있으면 댓글 꺼내기
        Comment comment = commentOptional.get();

        if (comment.getUser().getId() != userId) {
            throw new AuthorizationFailureException("삭제할 권한이 없습니다.");
        }

        //? 이미 삭제된 댓글 재삭제 방어
        if (comment.isDeleted()) {
            throw new ResourceNotFoundException("이미 삭제된 댓글입니다.");
        }

        comment.setDeleted(true);
        commentRepository.save(comment);
        }


    }


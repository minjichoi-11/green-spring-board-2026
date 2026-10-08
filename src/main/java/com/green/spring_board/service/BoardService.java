package com.green.spring_board.service;

import com.green.spring_board.dto.BoardResponse;
import com.green.spring_board.dto.BoardUpdateRequest;
import com.green.spring_board.dto.LikeDetailResponse;
import com.green.spring_board.entity.Like;
import com.green.spring_board.entity.User;
import com.green.spring_board.exceptions.*;
import com.green.spring_board.dto.BoardCreateRequest;
import com.green.spring_board.entity.Board;
import com.green.spring_board.repository.BoardRepository;
import com.green.spring_board.repository.LikeRepository;
import com.green.spring_board.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor

public class BoardService {
    private BoardRepository boardRepository;
    private UserRepository userRepository;
    private LikeRepository likeRepository;

    //* 전체 조회
    public Page<BoardResponse> getAllBoards(int userId, int page, int size, String order) {
        Sort sort;
        //? 최신순 정렬 (createdDatetime 기준 내림차순)
        if (order.equals("latest")) {
            sort = Sort.by(Sort.Direction.DESC, "createdDatetime");

            //? 좋아요순 정렬 로직
        } else if (order.equals("likes")) {
            sort = Sort.by(Sort.Direction.DESC, "likeCount");

            //? 조회수순 정렬 로직 (필요 시 구현)
        } else if (order.equals("views")) {
            sort = Sort.by(Sort.Direction.DESC, "hits");
        } else {

            //? 정의되지 않은 정렬 값이 들어오면 예외 발생
            throw new InvalidStateException("잘못된 정렬 옵션입니다.");

        } //exception 던지기
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Board> boards = boardRepository.findAll(pageable);


        List<BoardResponse> boardResponses = new ArrayList<>();

        //* 2. Board 개수만큼 반복하며 new BoardResponse 생성
        for (Board board : boards) {
            //* 3. 1번에서 만든 리스트에 추가
            boardResponses.add(
                    new BoardResponse(
                            board.getId(),
                            board.getTitle(),
                            board.getContent(),
                            board.getHits(),
                            board.getLikeCount(),
                            (userId == -1) ? false : likeRepository.existsByUserIdAndBoardId(userId, board.getId()),
                            board.getUser().getId(),
                            board.getUser().getNickname(),
                            board.getCreatedDatetime(),
                            board.getUpdatedDatetime()
                    )
            );
        }
        return new PageImpl<>(boardResponses, pageable, boards.getTotalElements());
    }

    //* 상세 조회
    public BoardResponse getBoard(int id, int userId) {
        Optional<Board> optionalBoard = boardRepository.findById(id);
        if(optionalBoard.isEmpty()) {
            // 요청한 게시글을 찾지 못한 경우
            throw new ResourceNotFoundException("요청한 게시글을 찾지 못했습니다.");
        }
        Board board = optionalBoard.get();

        User user = board.getUser();
        System.out.println(user.getNickname()); //* 작성자 닉네임 출력
        board.setHits(board.getHits() + 1);
        boardRepository.save(board);
        return new BoardResponse( //* 클라이언트에 보낼 응답 데이터 생성(DTO 변환)
                        board.getId(),
                        board.getTitle(),
                        board.getContent(),
                        board.getHits(),
                        board.getLikeCount(),
                        // 지금 보드 id, 요청자의 user 콤보가 like 테이블에 존재하는지 확인
                        (userId == -1) ? false : likeRepository.existsByUserIdAndBoardId(userId, board.getId()),
                        board.getUser().getId(),
                        board.getUser().getNickname(),
                        board.getCreatedDatetime(),
                        board.getUpdatedDatetime()

                );
    }

    //* 내 글 조회
    public List<BoardResponse> getMyBoards(int userId) {
        List<Board> boards = boardRepository.findByUserId(userId); //* user_id가 userId인 글만

        //* 1. List<BoardResponse> 형태의 빈 리스트 생성
        List<BoardResponse> boardResponses = new ArrayList<>();

        //* 2. Board 개수만큼 반복하며 new BoardResponse 생성
        for (Board board : boards) {
            boardResponses.add(
                    new BoardResponse(
                            board.getId(),
                            board.getTitle(),
                            board.getContent(),
                            board.getHits(),
                            board.getLikeCount(),
                            likeRepository.existsByUserIdAndBoardId(userId, board.getId()),
                            board.getUser().getId(),
                            board.getUser().getNickname(),
                            board.getCreatedDatetime(),
                            board.getUpdatedDatetime()
                    )
            );
        }
        return boardResponses;
    }

    //* 삽입
    public int createBoard(BoardCreateRequest boardCreateRequest, Integer userId) {
        //* userId 유효성 체크 (해당 userId의 유저가 정상적으로 존재하는지)
        //* TODO :: 이후 삭제/탈퇴 유저에 대한 검증도 추가 필요

        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            throw new UnauthenticatedException("로그인한 사용자를 찾을 수 없습니다.");
        }

        Board board = new Board();
        board.setTitle(boardCreateRequest.getTitle());
        board.setContent(boardCreateRequest.getContent());
        board.setUser(user.get());

        Board savedBoard = boardRepository.save(board);

        return savedBoard.getId();

    }

    //? 수정
    public void updateBoard(int id, BoardUpdateRequest boardCreateRequest, int userId) {
        Optional<Board> optionalBoards = boardRepository.findById(id);
        if(optionalBoards.isEmpty()) {
            //? 게시글을 못 찾은 경우
            throw new ResourceNotFoundException("게시글을 찾을 수 없습니다.");
        }
        Board board = optionalBoards.get();

        // 요청자의 user id를 알 수 없음.
        // 요청자의 user id가 같은지 다른지 확인
//        board.getUser().getId();

        //* 작성자와 요청자 동일 여부 확인
        if (board.getUser().getId() != userId) {
            throw new AuthorizationFailureException("게시글 작업 권한이 없습니다.");
        }

        if(boardCreateRequest.getTitle() != null && !boardCreateRequest.getTitle().isBlank()) {
            board.setTitle(boardCreateRequest.getTitle());
        }
        if(boardCreateRequest.getContent() != null && !boardCreateRequest.getContent().isBlank()) {
            board.setContent(boardCreateRequest.getContent());
        }
        boardRepository.save(board);
    }

    public void deleteBoard(int id, int userId) {
        Optional<Board> OptionalBoard = boardRepository.findById(id);
        if (OptionalBoard.isEmpty()) {
            throw new ResourceNotFoundException("게시글을 찾을 수 없습니다.");
        }
        Board board = OptionalBoard.get();

        if (board.getUser().getId() != userId) {
            throw new AuthorizationFailureException("게시글 작업 권한이 없습니다.");
        }

        boardRepository.deleteById(id); // 성공한 경우를 안쪽에.
    }

    public void pressLike(int id, int userId) {
        Optional<Board> optionalBoard = boardRepository.findById(id);
        if (optionalBoard.isEmpty()) {
            throw new ResourceNotFoundException("존재하지 않는 게시물입니다.");
        }

        Board board = optionalBoard.get();

        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            throw new ResourceNotFoundException("존재하지 않는 유저입니다.");
        }

        User user = optionalUser.get();

        //* 1. 이 유저와 보드로 동일한 좋아요가 있는지 확인
        Optional<Like> likeOptional = likeRepository.findByUserIdAndBoardId(userId, id);
        if (likeOptional.isEmpty()) {
            // 없으면 좋아요 추가
            Like like = new Like();
            like.setUser(user);
            like.setBoard(board);
            likeRepository.save(like);

            // 좋아요 증감
            board.setLikeCount(board.getLikeCount() + 1);
            boardRepository.save(board);

        } else  {
            // 있으면 좋아요 삭제
            Like like = likeOptional.get();
            likeRepository.deleteById(like.getId());

            board.setLikeCount(board.getLikeCount() - 1);
            boardRepository.save(board);
        }

    }

    public LikeDetailResponse getLikeDetail(int id) {

        //* 1. 이 게시글의 좋아요 누른 유저 정보들을 Like 테이블에서 싹 가져옴
        List<Like> likes = likeRepository.findByBoardId(id);

        //* 2. 유저 닉네임을 하나하나 뽑아서, LikeDetailResponse에 집어넣음
        LikeDetailResponse likeDetailResponse = new LikeDetailResponse();
        List<String> nicknames = new ArrayList<>();
        for(Like like : likes) {
            nicknames.add(like.getUser().getNickname());
        }
        likeDetailResponse.setLikeUserNames(nicknames);

        //* 3. 끝
        return likeDetailResponse;
    }

}

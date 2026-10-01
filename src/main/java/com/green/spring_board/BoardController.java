package com.green.spring_board;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.resource.ResourceUrlProvider;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/board")
@AllArgsConstructor

public class BoardController {
    private final ResourceUrlProvider resourceUrlProvider;
    private BoardRepository boardRepository;

    //* 전체 조회
    @GetMapping
    public ResponseEntity<List<Boards>> getBoards() {
        return ResponseEntity.ok(
                boardRepository.findAll()
        );
//        return responseEntity boardRepository.findAll(); <- 데이터만
    }

    //* 상세 조회
    @GetMapping ("/{id}")
    public ResponseEntity<Boards> getBoardsDetail(@PathVariable int id) {
        Optional<Boards> optionalBoard = boardRepository.findById(id);
        if(optionalBoard.isEmpty()) {
            // 요청한 게시글을 찾지 못한 경우
            return ResponseEntity.notFound().build();
        }

        Boards board = optionalBoard.get();

        board.setHits(board.getHits() + 1);
        boardRepository.save(board);

        return ResponseEntity.ok(board);

    }

    //* 삽입
    @PostMapping
    public ResponseEntity<Boards> createBoard(@RequestBody BoardCreateRequest boardCreateRequest) {

        if(boardCreateRequest.getTitle() == null || boardCreateRequest.getTitle().isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        if(boardCreateRequest.getContent() == null || boardCreateRequest.getContent().isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        Boards board = new Boards();
        board.setTitle(boardCreateRequest.getTitle());
        board.setContent(boardCreateRequest.getContent());

        Boards savedBoard = boardRepository.save(board);
        int newBoardId = savedBoard.getId();
        URI location = URI.create("/api/board/" + newBoardId);

        return ResponseEntity.created(location).body(board);
    }

    //* 수정
    @PatchMapping("/{id}")
    public ResponseEntity<Void> updateBoard(
            @PathVariable int id,
            @RequestBody BoardCreateRequest boardCreateRequest
    ){
        Optional<Boards> optionalBoards = boardRepository.findById(id);
        if(optionalBoards.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Boards board = optionalBoards.get();

        if(boardCreateRequest.getTitle() != null && !boardCreateRequest.getTitle().isBlank()) {
            board.setTitle(boardCreateRequest.getTitle());
        }
        if(boardCreateRequest.getContent() != null && !boardCreateRequest.getContent().isBlank()) {
            board.setContent(boardCreateRequest.getContent());
        }
        boardRepository.save(board);

        return ResponseEntity.ok().build();
    }


    //* 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBoar(@PathVariable int id) {
        boolean isExist = boardRepository.existsById(id);
        if (!isExist) { // 실패한 경우를 바깥에
            return ResponseEntity.notFound().build();
        }

        boardRepository.deleteById(id); // 성공한 경우를 안쪽에
        return ResponseEntity.noContent().build();
    }
}

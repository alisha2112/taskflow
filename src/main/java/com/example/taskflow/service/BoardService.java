package com.example.taskflow.service;

import com.example.taskflow.model.dto.BoardRequestDto;
import com.example.taskflow.model.dto.BoardResponseDto;
import com.example.taskflow.model.entity.Board;
import com.example.taskflow.model.entity.User;
import com.example.taskflow.repository.BoardRepository;
import com.example.taskflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BoardService {
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;

    @Transactional
    public List<BoardResponseDto> getAllBoards(Long userId) {
        return boardRepository.findAllByOwnerId(userId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public BoardResponseDto createBoard(BoardRequestDto dto, User owner) {
        Board board = new Board();
        board.setTitle(dto.title());
        board.setOwner(owner);

        return mapToResponse(boardRepository.save(board));
    }

    @Transactional
    public BoardResponseDto updateBoard(Long boardId, BoardRequestDto dto, Long userId) {
        Board board = findAndValidateOwner(boardId, userId);
        board.setTitle(dto.title());
        return mapToResponse(boardRepository.save(board));
    }

    @Transactional
    public void deleteBoard(Long boardId, Long userId) {
        Board board = findAndValidateOwner(boardId, userId);
        boardRepository.delete(board);
    }

    private Board findAndValidateOwner(Long boardId, Long userId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new RuntimeException("Board not found"));

        if (!board.getOwner().getId().equals(userId)) {
            throw new RuntimeException("You don`t have permission to edit this board");
        }

        return board;
    }

    private BoardResponseDto mapToResponse(Board board) {
        return new BoardResponseDto(board.getId(), board.getTitle(), board.getOwner().getId());
    }
}

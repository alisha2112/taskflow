package com.example.taskflow.service;

import com.example.taskflow.exception.AccessDeniedException;
import com.example.taskflow.exception.ResourceNotFoundException;
import com.example.taskflow.model.dto.BoardRequestDto;
import com.example.taskflow.model.dto.BoardResponseDto;
import com.example.taskflow.model.entity.Board;
import com.example.taskflow.model.entity.User;
import com.example.taskflow.repository.BoardRepository;
import com.example.taskflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BoardService {
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;

    @Transactional
    @Cacheable(value = "boards", key = "#userId")
    public List<BoardResponseDto> getAllBoards(Long userId) {
        log.debug("Fetching boards for user ID: {}", userId);
        return boardRepository.findAllByOwnerId(userId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    @CacheEvict(value = "boards", key = "#owner.id")
    public BoardResponseDto createBoard(BoardRequestDto dto, User owner) {
        Board board = new Board();
        board.setTitle(dto.title());
        board.setOwner(owner);

        Board savedBoard = boardRepository.save(board);
        log.info("Board created: ID={} Title='{}' by UserID={}",
                savedBoard.getId(), savedBoard.getTitle(), owner.getId());

        return mapToResponse(boardRepository.save(board));
    }

    @Transactional
    @CacheEvict(value = "boards", key = "#userId")
    public BoardResponseDto updateBoard(Long boardId, BoardRequestDto dto, Long userId) {
        Board board = findAndValidateOwner(boardId, userId);

        if (!board.getTitle().equals(dto.title())) {
            log.info("Board title changed: ID={} From='{}' To='{}' by UserID={}",
                    boardId, board.getTitle(), dto.title(), userId);
        }

        board.setTitle(dto.title());
        return mapToResponse(boardRepository.save(board));
    }

    @Transactional
    @CacheEvict(value = "boards", key = "#userId")
    public void deleteBoard(Long boardId, Long userId) {
        Board board = findAndValidateOwner(boardId, userId);
        boardRepository.delete(board);

        log.info("Board deleted: ID={} by UserID={}", boardId, userId);
    }

    private Board findAndValidateOwner(Long boardId, Long userId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResourceNotFoundException("Board not found with id: " + boardId));

        if (!board.getOwner().getId().equals(userId)) {
            log.warn("Access denied: UserID={} tried to modify BoardID={} owned by UserID={}",
                    userId, boardId, board.getOwner().getId());
            throw new AccessDeniedException("You don`t have permission to edit this board");
        }

        return board;
    }

    private BoardResponseDto mapToResponse(Board board) {
        return new BoardResponseDto(board.getId(), board.getTitle(), board.getOwner().getId());
    }
}

package com.example.taskflow.controller;

import com.example.taskflow.model.dto.BoardRequestDto;
import com.example.taskflow.model.dto.BoardResponseDto;
import com.example.taskflow.model.entity.User;
import com.example.taskflow.service.BoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/boards")
@RequiredArgsConstructor
public class BoardController {
    private final BoardService boardService;

    @GetMapping
    public List<BoardResponseDto> getAll(@AuthenticationPrincipal User currentUser) {
        return boardService.getAllBoardsByOwner(currentUser.getId());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BoardResponseDto create(@RequestBody BoardRequestDto dto,
                                   @AuthenticationPrincipal User currentUser) {
        return boardService.createBoard(dto, currentUser);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@boardSecurity.isOwner(authentication, #id)")
    public BoardResponseDto update(@PathVariable Long id,
                                  @RequestBody BoardRequestDto dto) {
        return boardService.updateBoard(id, dto);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@boardSecurity.isOwner(authentication, #id)")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id,
                       @AuthenticationPrincipal User currentUser) {
        boardService.deleteBoard(id);
    }
}

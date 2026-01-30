package com.example.taskflow.controller;

import com.example.taskflow.model.dto.BoardRequestDto;
import com.example.taskflow.model.dto.BoardResponseDto;
import com.example.taskflow.model.entity.User;
import com.example.taskflow.service.BoardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/boards")
@RequiredArgsConstructor
@Tag(name = "Boards", description = "Управління дошками задач (створення, редагування, перегляд)")
public class BoardController {
    private final BoardService boardService;

    @Operation(summary = "Отримати всі дошки користувача",
            description = "Повертає список дошок, де поточний користувач є власником")
    @GetMapping
    public List<BoardResponseDto> getAll(@AuthenticationPrincipal User currentUser) {
        return boardService.getAllBoardsByOwner(currentUser.getId());
    }

    @Operation(summary = "Створити нову дошку",
            description = "Створює дошку з вказаною назвою та призначає автора власником")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BoardResponseDto create(@RequestBody BoardRequestDto dto,
                                   @AuthenticationPrincipal User currentUser) {
        return boardService.createBoard(dto, currentUser);
    }

    @Operation(summary = "Оновити назву дошки", description = "Дозволяє змінити назву дошки. Доступно лише власнику.")
    @PutMapping("/{id}")
    @PreAuthorize("@boardSecurity.isOwner(authentication, #id)")
    public BoardResponseDto update(@PathVariable Long id,
                                  @RequestBody BoardRequestDto dto) {
        return boardService.updateBoard(id, dto);
    }

    @Operation(summary = "Видалити дошку", description = "Видаляє дошку разом з усіма завданнями. Доступно лише власнику.")
    @DeleteMapping("/{id}")
    @PreAuthorize("@boardSecurity.isOwner(authentication, #id)")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id,
                       @AuthenticationPrincipal User currentUser) {
        boardService.deleteBoard(id);
    }
}

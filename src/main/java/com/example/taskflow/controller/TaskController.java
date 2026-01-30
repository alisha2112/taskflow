package com.example.taskflow.controller;

import com.example.taskflow.model.dto.TaskRequestDto;
import com.example.taskflow.model.dto.TaskResponseDto;
import com.example.taskflow.model.entity.TaskPriority;
import com.example.taskflow.model.entity.User;
import com.example.taskflow.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Tag(name = "Tasks", description = "Управління завданнями: створення, фільтрація, редагування та призначення виконавців")
public class TaskController {
    private final TaskService taskService;

    @Operation(summary = "Отримати список завдань",
            description = "Повертає завдання з конкретної дошки. Можна фільтрувати за пріоритетом та виконавцем.")
    @GetMapping
    public List<TaskResponseDto> getByBoard(@RequestParam Long boardId,
                                            @AuthenticationPrincipal User currentUser,
                                            @RequestParam(required = false) TaskPriority priority,
                                            @RequestParam(required = false) Long assigneeId) {
        return taskService.getTasksByBoard(boardId, currentUser.getId(), priority, assigneeId);
    }

    @Operation(summary = "Створити нове завдання",
            description = "Додає нове завдання на дошку. Доступно лише власнику дошки.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@boardSecurity.isOwner(authentication, #dto.boardId())")
    public TaskResponseDto create(@RequestBody @Valid TaskRequestDto dto,
                                  @AuthenticationPrincipal User currentUser) {
        return taskService.createTask(dto, currentUser.getId());
    }

    @Operation(summary = "Оновити завдання (повне)",
            description = "Повністю оновлює дані завдання (назва, опис, статус, пріоритет).")
    @PutMapping("/{id}")
    @PreAuthorize("@boardSecurity.isBoardOwnerOfTask(authentication, #id)")
    public TaskResponseDto update(@PathVariable Long id,
                                 @RequestBody @Valid TaskRequestDto dto,
                                 @AuthenticationPrincipal User currentUser) {
        return taskService.updateTask(id, dto, currentUser.getId());
    }

    @Operation(summary = "Оновити завдання (часткове)",
            description = "Дозволяє змінити окремі поля завдання (наприклад, тільки статус або тільки дедлайн).")
    @PatchMapping("/{id}")
    @PreAuthorize("@boardSecurity.isBoardOwnerOfTask(authentication, #id)")
    public TaskResponseDto patchUpdate(@PathVariable Long id,
                                       @RequestBody TaskRequestDto dto,
                                       @AuthenticationPrincipal User currentUser) {
        return taskService.patchUpdateTask(id, dto, currentUser.getId());
    }

    @Operation(summary = "Призначити виконавця",
            description = "Призначає користувача на завдання. Якщо userId не передано — знімає виконавця.")
    @PatchMapping("/{id}/assign")
    @PreAuthorize("@boardSecurity.isBoardOwnerOfTask(authentication, #id)")
    public TaskResponseDto assignTask(@PathVariable Long id,
                                      @RequestParam(required = false) Long userId,
                                      @AuthenticationPrincipal User currentUser) {
        return taskService.assignTask(id, userId, currentUser.getId());
    }

    @Operation(summary = "Видалити завдання", description = "Архівує або видаляє завдання. Доступно лише власнику дошки.")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@boardSecurity.isBoardOwnerOfTask(authentication, #id)")
    public void delete(@PathVariable Long id,
                       @AuthenticationPrincipal User currentUser) {
        taskService.deleteTask(id, currentUser.getId());
    }
}

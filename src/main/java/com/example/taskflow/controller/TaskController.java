package com.example.taskflow.controller;

import com.example.taskflow.model.dto.TaskRequestDto;
import com.example.taskflow.model.dto.TaskResponseDto;
import com.example.taskflow.model.entity.TaskPriority;
import com.example.taskflow.model.entity.User;
import com.example.taskflow.service.TaskService;
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
public class TaskController {
    private final TaskService taskService;

    @GetMapping("/board/{boardId}")
    public List<TaskResponseDto> getByBoard(@PathVariable Long boardId,
                                            @AuthenticationPrincipal User currentUser,
                                            @RequestParam(required = false) TaskPriority priority,
                                            @RequestParam(required = false) Long assigneeId) {
        return taskService.getTasksByBoard(boardId, currentUser.getId(), priority, assigneeId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@boardSecurity.isOwner(authentication, #dto.boardId())")
    public TaskResponseDto create(@RequestBody @Valid TaskRequestDto dto,
                                  @AuthenticationPrincipal User currentUser) {
        return taskService.createTask(dto, currentUser.getId());
    }

    @PutMapping("/{id}")
    @PreAuthorize("@boardSecurity.isBoardOwnerOfTask(authentication, #id)")
    public TaskResponseDto update(@PathVariable Long id,
                                 @RequestBody @Valid TaskRequestDto dto,
                                 @AuthenticationPrincipal User currentUser) {
        return taskService.updateTask(id, dto, currentUser.getId());
    }

    @PatchMapping("/{id}")
    @PreAuthorize("@boardSecurity.isBoardOwnerOfTask(authentication, #id)")
    public TaskResponseDto patchUpdate(@PathVariable Long id,
                                       @RequestBody TaskRequestDto dto,
                                       @AuthenticationPrincipal User currentUser) {
        return taskService.patchUpdateTask(id, dto, currentUser.getId());
    }

    @PatchMapping("/{id}/assign")
    @PreAuthorize("@boardSecurity.isBoardOwnerOfTask(authentication, #id)")
    public TaskResponseDto assignTask(@PathVariable Long id,
                                      @RequestParam(required = false) Long userId,
                                      @AuthenticationPrincipal User currentUser) {
        return taskService.assignTask(id, userId, currentUser.getId());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@boardSecurity.isBoardOwnerOfTask(authentication, #id)")
    public void delete(@PathVariable Long id,
                       @AuthenticationPrincipal User currentUser) {
        taskService.deleteTask(id, currentUser.getId());
    }
}

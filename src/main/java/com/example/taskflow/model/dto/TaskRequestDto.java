package com.example.taskflow.model.dto;

import com.example.taskflow.model.entity.TaskPriority;
import com.example.taskflow.model.entity.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record TaskRequestDto (
        @NotBlank(message = "Title of task is mandatory")
        String title,

        String description,

        @NotNull(message = "Status can`t be empty")
        TaskStatus status,

        @NotNull(message = "Priority is mandatory")
        TaskPriority priority,

        @NotNull(message = "ID is mandatory")
        Long boardId,

        LocalDateTime deadline
) {}

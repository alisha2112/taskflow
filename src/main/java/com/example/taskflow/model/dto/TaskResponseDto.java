package com.example.taskflow.model.dto;

import com.example.taskflow.model.entity.TaskPriority;
import com.example.taskflow.model.entity.TaskStatus;

import java.time.LocalDateTime;

public record TaskResponseDto (
        Long id,
        String title,
        String description,
        TaskStatus status,
        TaskPriority priority,
        Long boardId,
        LocalDateTime deadline,
        boolean isArchived,
        AssigneeDto assignee
){}

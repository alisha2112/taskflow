package com.example.taskflow.model.dto.event;

public record NotificationDto(
        String message,
        Long taskId,
        Long boardId,
        String type
) {}

package com.example.taskflow.model.dto.event;

import com.example.taskflow.model.dto.TaskResponseDto;

public record TaskEventDto(
        EventType type,
        Long boardId,
        TaskResponseDto task
) {}

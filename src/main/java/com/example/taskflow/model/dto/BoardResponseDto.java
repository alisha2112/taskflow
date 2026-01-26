package com.example.taskflow.model.dto;

public record BoardResponseDto(
        Long id,
        String title,
        Long ownerId
) {}

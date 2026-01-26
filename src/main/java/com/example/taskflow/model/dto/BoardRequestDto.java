package com.example.taskflow.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BoardRequestDto(
        @NotBlank(message = "Title can't be empty")
        @Size(max = 100, message = "Title can't exceed 100 characters")
        String title
) {}

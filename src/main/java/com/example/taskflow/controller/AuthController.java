package com.example.taskflow.controller;

import com.example.taskflow.model.dto.auth.AuthenticationResponse;
import com.example.taskflow.model.dto.auth.LoginRequest;
import com.example.taskflow.model.dto.auth.RegisterRequest;
import com.example.taskflow.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Реєстрація та вхід користувачів")
public class AuthController {
    private final AuthenticationService authenticationService;

    @Operation(summary = "Реєстрація нового користувача", description = "Створює користувача та повертає JWT токен")
    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(@RequestBody @Valid RegisterRequest request) {
        return ResponseEntity.ok(authenticationService.register(request));
    }

    @Operation(summary = "Вхід у систему", description = "Перевіряє email, пароль та повертає JWT токен")
    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(@RequestBody @Valid LoginRequest request) {
        return ResponseEntity.ok(authenticationService.authenticate(request));
    }
}

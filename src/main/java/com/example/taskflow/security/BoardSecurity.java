package com.example.taskflow.security;

import com.example.taskflow.repository.BoardRepository;
import com.example.taskflow.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("boardSecurity")
@RequiredArgsConstructor
public class BoardSecurity {
    private final BoardRepository boardRepository;
    private final TaskRepository taskRepository;

    public boolean isOwner(Authentication authentication, Long boardId) {
        if (boardId == null) return false;

        String userEmail = authentication.getName();

        return boardRepository.findById(boardId)
                .map(board -> board.getOwner().getEmail().equals(userEmail))
                .orElse(false);
    }

    public boolean isBoardOwnerOfTask(Authentication authentication, Long taskId) {
        if (taskId == null) return false;

        String userEmail = authentication.getName();

        return taskRepository.findById(taskId)
                .map(task -> task.getBoard().getOwner().getEmail().equals(userEmail))
                .orElse(false);
    }
}

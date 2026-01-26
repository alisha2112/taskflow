package com.example.taskflow.service;

import com.example.taskflow.model.dto.TaskRequestDto;
import com.example.taskflow.model.dto.TaskResponseDto;
import com.example.taskflow.model.entity.Board;
import com.example.taskflow.model.entity.Task;
import com.example.taskflow.repository.BoardRepository;
import com.example.taskflow.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepository taskRepository;
    private final BoardRepository boardRepository;

    @Transactional(readOnly = true)
    public List<TaskResponseDto> getTasksByBoard(Long boardId, Long userId) {
        validateBoardAccess(boardId, userId);
        return taskRepository.findAllByBoardId(boardId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public TaskResponseDto createTask(TaskRequestDto dto, Long userId) {
        Board board = validateBoardAccess(dto.boardId(), userId);

        Task task = new Task();
        task.setTitle(dto.title());
        task.setDescription(dto.description());
        task.setStatus(dto.status());
        task.setPriority(dto.priority());
        task.setBoard(board);

        return mapToResponse(taskRepository.save(task));
    }

    @Transactional
    public TaskResponseDto updateTask(Long taskId, TaskRequestDto dto, Long userId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        validateBoardAccess(task.getBoard().getId(), userId);

        task.setTitle(dto.title());
        task.setDescription(dto.description());
        task.setStatus(dto.status());
        task.setPriority(dto.priority());

        return mapToResponse(taskRepository.save(task));
    }

    @Transactional
    public void deleteTask(Long taskId, Long userId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        validateBoardAccess(task.getBoard().getId(), userId);

        taskRepository.delete(task);
    }

    private Board validateBoardAccess(Long boardId, Long userId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new RuntimeException("Board not found"));

        if (!board.getOwner().getId().equals(userId)) {
            throw new RuntimeException("You don't have permission to access this board");
        }

        return board;
    }

    private TaskResponseDto mapToResponse(Task task) {
        return new TaskResponseDto(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getPriority(),
                task.getBoard().getId(),
                task.getDeadline()
        );
    }
}

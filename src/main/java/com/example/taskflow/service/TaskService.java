package com.example.taskflow.service;

import com.example.taskflow.model.dto.AssigneeDto;
import com.example.taskflow.model.dto.TaskRequestDto;
import com.example.taskflow.model.dto.TaskResponseDto;
import com.example.taskflow.model.entity.Board;
import com.example.taskflow.model.entity.Task;
import com.example.taskflow.model.entity.TaskPriority;
import com.example.taskflow.model.entity.User;
import com.example.taskflow.repository.BoardRepository;
import com.example.taskflow.repository.TaskRepository;
import com.example.taskflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepository taskRepository;
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<TaskResponseDto> getTasksByBoard(Long boardId, Long userId, TaskPriority priority, Long assigneeId) {
        validateBoardAccess(boardId, userId);
        return taskRepository.findByBoardIdWithFilters(boardId, priority, assigneeId).stream()
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

        task.setArchived(true);
        taskRepository.save(task);
    }
    
    @Transactional
    public TaskResponseDto assignTask(Long taskId, Long assigneeId, Long userId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        
        validateBoardAccess(task.getBoard().getId(), userId);

        if (assigneeId != null) {
            User assignee = userRepository.findById(assigneeId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            task.setAssignee(assignee);
        } else {
            task.setAssignee(null);
        }

        return mapToResponse(taskRepository.save(task));
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
        AssigneeDto assigneeDto = null;

        if (task.getAssignee() != null) {
            assigneeDto = new AssigneeDto(
                    task.getAssignee().getId(),
                    task.getAssignee().getUsername(),
                    task.getAssignee().getEmail()
            );
        }

        return new TaskResponseDto(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getPriority(),
                task.getBoard().getId(),
                task.getDeadline(),
                task.isArchived(),
                assigneeDto
        );
    }
}

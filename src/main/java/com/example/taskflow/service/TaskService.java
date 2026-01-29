package com.example.taskflow.service;

import com.example.taskflow.exception.ResourceNotFoundException;
import com.example.taskflow.model.dto.AssigneeDto;
import com.example.taskflow.model.dto.TaskRequestDto;
import com.example.taskflow.model.dto.TaskResponseDto;
import com.example.taskflow.model.dto.event.EventType;
import com.example.taskflow.model.entity.Board;
import com.example.taskflow.model.entity.Task;
import com.example.taskflow.model.entity.TaskPriority;
import com.example.taskflow.model.entity.User;
import com.example.taskflow.repository.BoardRepository;
import com.example.taskflow.repository.TaskRepository;
import com.example.taskflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskService {
    private final TaskRepository taskRepository;
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Transactional(readOnly = true)
    @Cacheable(value = "tasks", key = "{#boardId, #priority != null ? #priority.name() : 'null', #assigneeId != null ? #assigneeId : 'null'}")
    public List<TaskResponseDto> getTasksByBoard(Long boardId, Long userId, TaskPriority priority, Long assigneeId) {
        log.debug("Fetching tasks for board {} by user {}", boardId, userId);

        return taskRepository.findByBoardIdWithFilters(boardId, priority, assigneeId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    @CacheEvict(value = "tasks", allEntries = true)
    public TaskResponseDto createTask(TaskRequestDto dto, Long userId) {
        Board board = boardRepository.findById(dto.boardId())
                .orElseThrow(() -> new ResourceNotFoundException("Board not found with id: " + dto.boardId()));

        Task task = new Task();
        task.setTitle(dto.title());
        task.setDescription(dto.description());
        task.setStatus(dto.status());
        task.setPriority(dto.priority());
        task.setDeadline(dto.deadline());
        task.setBoard(board);

        Task savedTask = taskRepository.save(task);
        TaskResponseDto responseDto = mapToResponse(savedTask);

        log.info("Task created: ID={} Title='{}' BoardID={} by UserID={}",
                savedTask.getId(), savedTask.getTitle(), board.getId(), userId);

        notificationService.sendBoardUpdate(board.getId(), EventType.TASK_CREATED, responseDto);

        return responseDto;
    }

    @Transactional
    @CacheEvict(value = "tasks", allEntries = true)
    public TaskResponseDto updateTask(Long taskId, TaskRequestDto dto, Long userId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));

        if (task.getStatus() != dto.status()) {
            log.info("Task status changed: ID={} From={} To={} by UserID={}",
                    taskId, task.getStatus(), dto.status(), userId);
        }

        task.setTitle(dto.title());
        task.setDescription(dto.description());
        task.setStatus(dto.status());
        task.setPriority(dto.priority());
        task.setDeadline(dto.deadline());

        Task savedTask = taskRepository.save(task);
        TaskResponseDto responseDto = mapToResponse(savedTask);

        notificationService.sendBoardUpdate(task.getBoard().getId(), EventType.TASK_UPDATED, responseDto);

        return responseDto;
    }

    @Transactional
    @CacheEvict(value = "tasks", allEntries = true)
    public TaskResponseDto patchUpdateTask(Long taskId, TaskRequestDto dto, Long userId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));

        if (dto.title() != null) task.setTitle(dto.title());
        if (dto.description() != null) task.setDescription(dto.description());

        if (dto.status() != null) {
            if (task.getStatus() != dto.status()) {
                log.info("Task status changed (PATCH): ID={} From={} To={} by UserID={}",
                        taskId, task.getStatus(), dto.status(), userId);
            }
            task.setStatus(dto.status());
        }

        if (dto.priority() != null) task.setPriority(dto.priority());
        if (dto.deadline() != null) task.setDeadline(dto.deadline());

        Task savedTask = taskRepository.save(task);
        TaskResponseDto responseDto = mapToResponse(savedTask);

        notificationService.sendBoardUpdate(task.getBoard().getId(), EventType.TASK_UPDATED, responseDto);

        return responseDto;
    }

    @Transactional
    @CacheEvict(value = "tasks", allEntries = true)
    public void deleteTask(Long taskId, Long userId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));

        task.setArchived(true);
        Task savedTask = taskRepository.save(task);

        log.info("Task archived: ID={} by UserID={}", taskId, userId);

        notificationService.sendBoardUpdate(task.getBoard().getId(), EventType.TASK_DELETED, mapToResponse(savedTask));
    }

    @Transactional
    public TaskResponseDto assignTask(Long taskId, Long assigneeId, Long userId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));

        if (assigneeId != null) {
            User assignee = userRepository.findById(assigneeId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + assigneeId));
            task.setAssignee(assignee);
            log.info("Task assigned: ID={} to UserID={} by OwnerID={}", taskId, assigneeId, userId);
            notificationService.sendPrivateNotification(task);
        } else {
            task.setAssignee(null);
            log.info("Task unassigned: ID={} by OwnerID={}", taskId, userId);
        }

        Task savedTask = taskRepository.save(task);
        TaskResponseDto responseDto = mapToResponse(savedTask);

        notificationService.sendBoardUpdate(task.getBoard().getId(), EventType.TASK_UPDATED, responseDto);

        return responseDto;
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
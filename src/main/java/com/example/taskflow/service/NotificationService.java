package com.example.taskflow.service;

import com.example.taskflow.model.dto.TaskResponseDto;
import com.example.taskflow.model.dto.event.EventType;
import com.example.taskflow.model.dto.event.NotificationDto;
import com.example.taskflow.model.dto.event.TaskEventDto;
import com.example.taskflow.model.entity.Task;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    private final SimpMessagingTemplate messagingTemplate;

    @Async
    public void sendBoardUpdate(Long boardId, EventType eventType, TaskResponseDto taskDto) {
        String destination = "/topic/board/" + boardId;
        TaskEventDto event = new TaskEventDto(eventType, boardId, taskDto);

        log.info("Sending WebSocket event {} to {}", eventType, destination);
        messagingTemplate.convertAndSend(destination, event);
    }

    @Async
    public void sendPrivateNotification(Task task) {
        if (task.getAssignee() == null) return;

        String email = task.getAssignee().getEmail();
        NotificationDto notification = new NotificationDto(
                "assigned to a task: " + task.getTitle(),
                task.getId(),
                task.getBoard().getId(),
                "ASSIGNMENT"
        );

        String destination = "/topic/user/" + email + "/notification";

        log.info("Sending async assignment notification to thread: {}", Thread.currentThread().getName());
        messagingTemplate.convertAndSend(destination, notification);
    }

    @Async
    public void sendDeadlineWarning(Long taskId, Long boardId, String taskTitle, String userEmail) {
        NotificationDto notification = new NotificationDto(
                "Warning! The deadline of a task" + taskTitle + " expires in 24 hours.",
                taskId,
                boardId,
                "DEADLINE_WARNING"
        );

        String destination = "/topic/user/" + userEmail + "/notifications";

        log.info("Sending async deadline warning in thread: {}", Thread.currentThread().getName());
        messagingTemplate.convertAndSend(destination, notification);
    }
}

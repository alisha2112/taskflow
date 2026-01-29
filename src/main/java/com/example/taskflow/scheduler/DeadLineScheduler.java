package com.example.taskflow.scheduler;

import com.example.taskflow.model.entity.Task;
import com.example.taskflow.repository.TaskRepository;
import com.example.taskflow.service.NotificationService;
import com.example.taskflow.service.TaskService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DeadLineScheduler {
    private final TaskRepository taskRepository;
    private final NotificationService notificationService;

    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void checkUpcomingDeadlines() {
        log.info("Checking for upcoming deadlines...");

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startWindow = now.plusHours(24);
        LocalDateTime endWindow = now.plusHours(25);

        List<Task> tasksDue = taskRepository.findAllByDeadlineBetween(endWindow, endWindow);

        if (tasksDue.isEmpty()) {
            log.info("No tasks due in 24 hours found.");
        } else {
            log.info("Found {} tasks due in 24 hours found. Scheduler thread: {}", tasksDue.size(), Thread.currentThread().getName());
            tasksDue.forEach(task -> {
                if (task.getAssignee() != null) {
                    notificationService.sendDeadlineWarning(
                            task.getId(),
                            task.getBoard().getId(),
                            task.getTitle(),
                            task.getAssignee().getEmail()
                    );
                }
            });
        }
    }
}

package com.example.taskflow.repository;

import com.example.taskflow.model.entity.Task;
import com.example.taskflow.model.entity.TaskPriority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    @Query("""
        SELECT t FROM Task t 
        WHERE t.board.id = :boardId 
        AND t.isArchived = false
        AND (:priority IS NULL OR t.priority = :priority)
        AND (:assigneeId IS NULL OR t.assignee.id = :assigneeId)
    """)
    List<Task> findByBoardIdWithFilters(
            @Param("boardId") Long boardId,
            @Param("priority") TaskPriority priority,
            @Param("assigneeId") Long assigneeId
    );
    List<Task> findAllByBoardId(Long boardId);
}

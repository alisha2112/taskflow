package com.example.taskflow;

import com.example.taskflow.model.dto.BoardRequestDto;
import com.example.taskflow.model.dto.TaskRequestDto;
import com.example.taskflow.model.dto.auth.LoginRequest;
import com.example.taskflow.model.dto.auth.RegisterRequest;
import com.example.taskflow.model.entity.TaskPriority;
import com.example.taskflow.model.entity.TaskStatus;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class FullFlowTest extends AbstractIntegrationTest {
    @Test
    void shouldCompleteFullUserFlow() {
        RegisterRequest registerRequest = new RegisterRequest(
                "testuser",
                "taskflow_test@example.com",
                "securePass123"
        );

        String token = given()
                .contentType(ContentType.JSON)
                .body(registerRequest)
                .when()
                .post("/api/auth/register")
                .then()
                .statusCode(200)
                .body("token", notNullValue())
                .extract()
                .path("token");

        LoginRequest loginRequest = new LoginRequest(
                "taskflow_test@example.com",
                "securePass123"
        );

        String loginToken = given()
                .contentType(ContentType.JSON)
                .body(loginRequest)
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(200)
                .body("token", notNullValue())
                .extract()
                .path("token"
                );

        BoardRequestDto boardRequest = new BoardRequestDto("Test project");

        int boardId = given()
                .header("Authorization", "Bearer " + loginToken)
                .contentType(ContentType.JSON)
                .body(boardRequest)
                .when()
                .post("/api/boards")
                .then()
                .statusCode(201)
                .body("title", equalTo("Test project"))
                .extract()
                .path("id"
                );

        TaskRequestDto taskRequest = new TaskRequestDto(
                "Task for testing",
                "Test description",
                TaskStatus.TODO,
                TaskPriority.HIGH,
                (long) boardId,
                LocalDateTime.now().plusDays(1)
        );

        given()
                .header("Authorization", "Bearer " + loginToken)
                .contentType(ContentType.JSON)
                .body(taskRequest)
                .when()
                .post("/api/tasks")
                .then()
                .statusCode(201)
                .body("title", equalTo("Task for testing"))
                .body("boardId", equalTo(boardId))
                .body("priority", equalTo("HIGH"));

        given()
                .header("Authorization", "Bearer " + loginToken)
                .when()
                .get("/api/tasks?boardId=" + boardId)
                .then()
                .statusCode(200)
                .body("size()", is(1))
                .body("[0].title", equalTo("Task for testing"));
    }
}

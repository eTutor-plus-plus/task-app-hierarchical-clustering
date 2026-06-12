package at.jku.dke.task_app.hierarchical_clustering.controllers;

import at.jku.dke.etutor.task_app.auth.AuthConstants;
import at.jku.dke.etutor.task_app.dto.ModifyTaskDto;
import at.jku.dke.etutor.task_app.dto.TaskStatus;
import at.jku.dke.task_app.hierarchical_clustering.ClientSetupExtension;
import at.jku.dke.task_app.hierarchical_clustering.DatabaseSetupExtension;
import at.jku.dke.task_app.hierarchical_clustering.data.entities.HierarchicalClusteringTask;
import at.jku.dke.task_app.hierarchical_clustering.data.repositories.HierarchicalClusteringTaskRepository;
import at.jku.dke.task_app.hierarchical_clustering.dto.AssignmentTypeDto;
import at.jku.dke.task_app.hierarchical_clustering.generators.matrix.DistanceMetric;
import at.jku.dke.task_app.hierarchical_clustering.clustering.LinkageMethod;
import at.jku.dke.task_app.hierarchical_clustering.dto.ModifyHierarchicalClusteringTaskDto;
import at.jku.dke.task_app.hierarchical_clustering.dendrogram.DendrogramModel;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.math.BigDecimal;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith({DatabaseSetupExtension.class, ClientSetupExtension.class})
class TaskControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private HierarchicalClusteringTaskRepository repository;

    private long taskId;

    @BeforeEach
    void initDb() {
        this.repository.deleteAll();

        this.taskId = this.repository.save(getNewTask()).getId();
    }

    //#region --- GET ---
    @Test
    void getShouldReturnOk() {
        given()
            .port(port)
            .header(AuthConstants.AUTH_TOKEN_HEADER_NAME, ClientSetupExtension.CRUD_API_KEY)
            .accept(ContentType.JSON)
            // WHEN
            .when()
            .get("/api/task/{id}", this.taskId)
            // THEN
            .then()
            .log().ifValidationFails()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("nDataPoints", equalTo(3))
            .body("distanceMetric", equalTo(DistanceMetric.MANHATTAN.name()))
            .body("linkageMethod", equalTo(LinkageMethod.COMPLETE.name()))
            .body("wrongOrderPenalty", equalTo(BigDecimal.ONE.floatValue()));
    }

    @Test
    void getShouldReturnNotFound() {
        given()
            .port(port)
            .header(AuthConstants.AUTH_TOKEN_HEADER_NAME, ClientSetupExtension.CRUD_API_KEY)
            .accept(ContentType.JSON)
            // WHEN
            .when()
            .get("/api/task/{id}", this.taskId + 1)
            // THEN
            .then()
            .log().ifValidationFails()
            .statusCode(404);
    }

    @Test
    void getShouldReturnForbidden() {
        given()
            .port(port)
            .header(AuthConstants.AUTH_TOKEN_HEADER_NAME, ClientSetupExtension.SUBMIT_API_KEY)
            .accept(ContentType.JSON)
            // WHEN
            .when()
            .get("/api/task/{id}", this.taskId)
            // THEN
            .then()
            .log().ifValidationFails()
            .statusCode(403);
    }
    //#endregion

    //#region --- CREATE ---
    @Test
    void createShouldReturnCreated() {
        given()
            .port(port)
            .header(AuthConstants.AUTH_TOKEN_HEADER_NAME, ClientSetupExtension.CRUD_API_KEY)
            .contentType(ContentType.JSON)
            .body(new ModifyTaskDto<>(null, new BigDecimal(4), "hierarchical-clustering", TaskStatus.APPROVED, new ModifyHierarchicalClusteringTaskDto(
                AssignmentTypeDto.MATRIX,
                null,
                5,
                LinkageMethod.COMPLETE,
                new BigDecimal(1),
                new BigDecimal(2),
                null,
                null
            )))
            // WHEN
            .when()
            .post("/api/task/{id}", this.taskId + 2)
            // THEN
            .then()
            .log().ifValidationFails()
            .statusCode(201)
            .contentType(ContentType.JSON)
            .header("Location", containsString("/api/task/" + (this.taskId + 2)))
            .body("descriptionDe", any(String.class))
            .body("descriptionEn", any(String.class));
    }

    @Test
    void createShouldReturnBadRequestOnInvalidBody() {
        given()
            .port(port)
            .header(AuthConstants.AUTH_TOKEN_HEADER_NAME, ClientSetupExtension.CRUD_API_KEY)
            .contentType(ContentType.JSON)
            .body(new ModifyTaskDto<>(null, BigDecimal.TEN, "", TaskStatus.APPROVED, new ModifyHierarchicalClusteringTaskDto(
                AssignmentTypeDto.MATRIX,
                null,
                5,
                LinkageMethod.COMPLETE,
                new BigDecimal(1),
                new BigDecimal(2),
                null,
                null
            )))
            // WHEN
            .when()
            .post("/api/task/{id}", this.taskId + 2)
            // THEN
            .then()
            .log().ifValidationFails()
            .statusCode(400);
    }

    @Test
    void createShouldReturnBadRequestOnEmptyBody() {
        given()
            .port(port)
            .header(AuthConstants.AUTH_TOKEN_HEADER_NAME, ClientSetupExtension.CRUD_API_KEY)
            .contentType(ContentType.JSON)
            // WHEN
            .when()
            .post("/api/task/{id}", this.taskId + 2)
            // THEN
            .then()
            .log().ifValidationFails()
            .statusCode(400);
    }

    @Test
    void createShouldReturnForbidden() {
        given()
            .port(port)
            .header(AuthConstants.AUTH_TOKEN_HEADER_NAME, ClientSetupExtension.SUBMIT_API_KEY)
            .contentType(ContentType.JSON)
            .body(new ModifyTaskDto<>(null, BigDecimal.TEN, "hierarchical-clustering", TaskStatus.APPROVED, new ModifyHierarchicalClusteringTaskDto(
                AssignmentTypeDto.MATRIX,
                null,
                5,
                LinkageMethod.COMPLETE,
                new BigDecimal(1),
                new BigDecimal(2),
                null,
                null
            )))
            // WHEN
            .when()
            .post("/api/task/{id}", this.taskId + 2)
            // THEN
            .then()
            .log().ifValidationFails()
            .statusCode(403);
    }
    //#endregion

    //#region --- UPDATE ---
    @Test
    void updateShouldReturnOk() {
        given()
            .port(port)
            .header(AuthConstants.AUTH_TOKEN_HEADER_NAME, ClientSetupExtension.CRUD_API_KEY)
            .contentType(ContentType.JSON)
            .body(new ModifyTaskDto<>(null, new BigDecimal(4), "hierarchical-clustering", TaskStatus.APPROVED, new ModifyHierarchicalClusteringTaskDto(
                AssignmentTypeDto.COORDINATES,
                DistanceMetric.MANHATTAN,
                5,
                LinkageMethod.SINGLE,
                new BigDecimal(1),
                new BigDecimal(2),
                new HierarchicalClusteringTask.CoordinateSystem(0, 10, 0, 10, List.of()),
                new HierarchicalClusteringTask.DistanceMatrix(List.of(), new BigDecimal[][]{})
            )))
            // WHEN
            .when()
            .put("/api/task/{id}", this.taskId)
            // THEN
            .then()
            .log().ifValidationFails()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("descriptionDe", any(String.class))
            .body("descriptionEn", any(String.class));
    }

    @Test
    void updateShouldReturnNotFound() {
        given()
            .port(port)
            .header(AuthConstants.AUTH_TOKEN_HEADER_NAME, ClientSetupExtension.CRUD_API_KEY)
            .contentType(ContentType.JSON)
            .body(new ModifyTaskDto<>(null, BigDecimal.TEN, "hierarchical-clustering", TaskStatus.APPROVED, new ModifyHierarchicalClusteringTaskDto(
                AssignmentTypeDto.COORDINATES,
                DistanceMetric.MANHATTAN,
                5,
                LinkageMethod.SINGLE,
                new BigDecimal(1),
                new BigDecimal(2),
                null,
                null
            )))
            // WHEN
            .when()
            .put("/api/task/{id}", this.taskId + 1)
            // THEN
            .then()
            .log().ifValidationFails()
            .statusCode(404);
    }

    @Test
    void updateShouldReturnBadRequestOnInvalidBody() {
        given()
            .port(port)
            .header(AuthConstants.AUTH_TOKEN_HEADER_NAME, ClientSetupExtension.CRUD_API_KEY)
            .contentType(ContentType.JSON)
            .body(new ModifyTaskDto<>(null, BigDecimal.TEN, "sql", TaskStatus.APPROVED, new ModifyHierarchicalClusteringTaskDto(
                AssignmentTypeDto.COORDINATES,
                DistanceMetric.MANHATTAN,
                5,
                LinkageMethod.SINGLE,
                new BigDecimal(1),
                new BigDecimal(2),
                null,
                null
            )))
            // WHEN
            .when()
            .put("/api/task/{id}", this.taskId)
            // THEN
            .then()
            .log().ifValidationFails()
            .statusCode(400);
    }

    @Test
    void updateShouldReturnBadRequestOnEmptyBody() {
        given()
            .port(port)
            .header(AuthConstants.AUTH_TOKEN_HEADER_NAME, ClientSetupExtension.CRUD_API_KEY)
            .contentType(ContentType.JSON)
            // WHEN
            .when()
            .put("/api/task/{id}", this.taskId)
            // THEN
            .then()
            .log().ifValidationFails()
            .statusCode(400);
    }

    @Test
    void updateShouldReturnForbidden() {
        given()
            .port(port)
            .header(AuthConstants.AUTH_TOKEN_HEADER_NAME, ClientSetupExtension.SUBMIT_API_KEY)
            .contentType(ContentType.JSON)
            .body(new ModifyTaskDto<>(null, BigDecimal.TEN, "hierarchical-clustering", TaskStatus.APPROVED, new ModifyHierarchicalClusteringTaskDto(
                AssignmentTypeDto.COORDINATES,
                DistanceMetric.MANHATTAN,
                5,
                LinkageMethod.SINGLE,
                new BigDecimal(1),
                new BigDecimal(2),
                null,
                null
            )))
            // WHEN
            .when()
            .put("/api/task/{id}", this.taskId)
            // THEN
            .then()
            .log().ifValidationFails()
            .statusCode(403);
    }
    //#endregion

    //#region --- DELETE ---
    @Test
    void deleteShouldReturnNoContent() {
        given()
            .port(port)
            .header(AuthConstants.AUTH_TOKEN_HEADER_NAME, ClientSetupExtension.CRUD_API_KEY)
            // WHEN
            .when()
            .delete("/api/task/{id}", this.taskId)
            // THEN
            .then()
            .log().ifValidationFails()
            .statusCode(204);
    }

    @Test
    void deleteShouldReturnNoContentOnNotFound() {
        given()
            .port(port)
            .header(AuthConstants.AUTH_TOKEN_HEADER_NAME, ClientSetupExtension.CRUD_API_KEY)
            // WHEN
            .when()
            .delete("/api/task/{id}", this.taskId + 1)
            // THEN
            .then()
            .log().ifValidationFails()
            .statusCode(204);
    }

    @Test
    void deleteShouldReturnForbidden() {
        given()
            .port(port)
            .header(AuthConstants.AUTH_TOKEN_HEADER_NAME, ClientSetupExtension.SUBMIT_API_KEY)
            // WHEN
            .when()
            .delete("/api/task/{id}", this.taskId)
            // THEN
            .then()
            .log().ifValidationFails()
            .statusCode(403);
    }
    //#endregion

    @Test
    void mapToDto() {
        // Arrange
        var task = getNewTask();

        // Act
        var result = new TaskController(null).mapToDto(task);

        // Assert
        assertEquals(List.of("1", "2", "3"), result.distanceMatrix().getLabels());
        assertEquals(3, result.distanceMatrix().getDistances().length);
        assertEquals(3, result.distanceMatrix().getDistances()[0].length);
        assertEquals(LinkageMethod.COMPLETE, result.linkageMethod());
        assertEquals(2, result.pointsPerCorrectCluster().intValue());
        assertEquals(1, result.wrongOrderPenalty().intValue());
    }

    private HierarchicalClusteringTask getNewTask() {
        // initialize task with empty values to avoid NullPointerExceptions
        var task = new HierarchicalClusteringTask(
            1L,
            BigDecimal.TWO,
            TaskStatus.APPROVED,
            new HierarchicalClusteringTask.DistanceMatrix(
                List.of("1", "2", "3"),
                new BigDecimal[][]{
                    { BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.valueOf(2) },
                    { BigDecimal.ONE,  BigDecimal.ZERO, BigDecimal.valueOf(3) },
                    { BigDecimal.valueOf(2), BigDecimal.valueOf(3), BigDecimal.ZERO }
                }
            ),
            BigDecimal.valueOf(2)
        );
        task.setCoordinateSystem(new HierarchicalClusteringTask.CoordinateSystem(0, 10, 0, 10, List.of()));
        task.setDistanceMetric(DistanceMetric.MANHATTAN);
        task.setWrongOrderPenalty(BigDecimal.ONE);
        task.setLinkageMethod(LinkageMethod.COMPLETE);
        task.setDendrogramModel(new DendrogramModel(List.of(), new DendrogramModel.Node()));

        return task;
    }

}

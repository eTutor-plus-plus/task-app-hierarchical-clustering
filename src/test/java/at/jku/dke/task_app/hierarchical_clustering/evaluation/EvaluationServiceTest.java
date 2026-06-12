package at.jku.dke.task_app.hierarchical_clustering.evaluation;

import at.jku.dke.etutor.task_app.dto.SubmissionMode;
import at.jku.dke.etutor.task_app.dto.SubmitSubmissionDto;
import at.jku.dke.etutor.task_app.dto.TaskStatus;
import at.jku.dke.task_app.hierarchical_clustering.DatabaseSetupExtension;
import at.jku.dke.task_app.hierarchical_clustering.clustering.LinkageMethod;
import at.jku.dke.task_app.hierarchical_clustering.data.entities.HierarchicalClusteringCluster;
import at.jku.dke.task_app.hierarchical_clustering.data.entities.HierarchicalClusteringMerge;
import at.jku.dke.task_app.hierarchical_clustering.data.entities.HierarchicalClusteringTask;
import at.jku.dke.task_app.hierarchical_clustering.data.repositories.HierarchicalClusteringClusterRepository;
import at.jku.dke.task_app.hierarchical_clustering.data.repositories.HierarchicalClusteringMergeRepository;
import at.jku.dke.task_app.hierarchical_clustering.data.repositories.HierarchicalClusteringTaskRepository;
import at.jku.dke.task_app.hierarchical_clustering.dto.*;
import at.jku.dke.task_app.hierarchical_clustering.clustering.NaiveAgglomerativeClusteringAlgorithm;
import at.jku.dke.task_app.hierarchical_clustering.dendrogram.DendrogramModelBuilder;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ExtendWith(DatabaseSetupExtension.class)
class EvaluationServiceTest {

    @Autowired
    private EvaluationService evaluationService;
    @Autowired
    private HierarchicalClusteringTaskRepository taskRepository;
    @Autowired
    private HierarchicalClusteringMergeRepository mergeRepository;
    @Autowired
    private HierarchicalClusteringClusterRepository clusterRepository;
    private long matrixTaskId;

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();

        this.matrixTaskId = 1L;
        var task = getNewTask(matrixTaskId);
        taskRepository.save(task);
    }

    @Test
    void testEvaluationMergeHistoryBuild() {
        // Arrange & Act
        SortedMap<BigDecimal, EvaluationService.MergeEventAtDistance> events = EvaluationService.buildEvaluationMergeHistoryForTask(taskRepository.findById(this.matrixTaskId).orElse(getNewTask(100L)));

        // Assert
        assertEquals(5, events.size());
        assertEquals(BigDecimal.valueOf(1).stripTrailingZeros(), events.firstKey().stripTrailingZeros());
        assertEquals(BigDecimal.valueOf(5).stripTrailingZeros(), events.lastKey().stripTrailingZeros());
        assertEquals(1, events.get(BigDecimal.valueOf(3).stripTrailingZeros()).newMerges().size());
        assertEquals(2, events.get(BigDecimal.valueOf(3).stripTrailingZeros()).inheritedMerges().size());
        assertEquals(BigDecimal.valueOf(1).stripTrailingZeros(), events.get(BigDecimal.valueOf(4).stripTrailingZeros()).pointsForDistance().stripTrailingZeros());
    }

    @Test
    void evaluateRun() {
        // Arrange
        SubmitSubmissionDto<HierarchicalClusteringSubmissionDto> dto = new SubmitSubmissionDto<>("test-user", "test-assignment", matrixTaskId, "en",
            SubmissionMode.RUN, 3, new HierarchicalClusteringSubmissionDto("""
            Distance 1.0: (3,4)
            Distance 2.0: (1,2), (3,4)
            Distance 3.0: (5,6), (1,2), (3,4)
            Distance 4.0: (1,2,3,4), (5,6)
            Distance 5.0: (1,2,3,4)
            """));

        // Act
        var result = evaluationService.evaluate(dto);

        // Assert
        assertNotNull(result);
        assertEquals(BigDecimal.ZERO.stripTrailingZeros(), result.points().stripTrailingZeros());
        assertEquals(new BigDecimal(5).stripTrailingZeros(), result.maxPoints().stripTrailingZeros());
        assertEquals("""
            Your Input: Distance 1.0: (3,4)
            Distance 2.0: (1,2), (3,4)
            Distance 3.0: (5,6), (1,2), (3,4)
            Distance 4.0: (1,2,3,4), (5,6)
            Distance 5.0: (1,2,3,4)
            """, result.generalFeedback());
        assertTrue(result.criteria().stream().anyMatch(x -> x.name().equals("Syntax") && x.feedback().equals("Valid Syntax")));
        assertEquals(1, result.criteria().size());
    }

    @Test
    void evaluateSubmitValid() {
        // Arrange
        SubmitSubmissionDto<HierarchicalClusteringSubmissionDto> dto = new SubmitSubmissionDto<>("test-user", "test-assignment", matrixTaskId, "en",
            SubmissionMode.SUBMIT, 0, new HierarchicalClusteringSubmissionDto("""
            Distance 1.0: (3,4)
            Distance 2.0: (1,2), (3,4)
            Distance 3.0: (5,6), (1,2), (3,4)
            Distance 4.0: (1,2,3,4), (5,6)
            Distance 5.0: (1,2,3,4,5,6)
            """));

        // Act
        var result = evaluationService.evaluate(dto);

        // Assert
        assertNotNull(result);
        assertEquals(new BigDecimal(5).stripTrailingZeros(), result.points().stripTrailingZeros());
        assertEquals(new BigDecimal(5).stripTrailingZeros(), result.maxPoints().stripTrailingZeros());
        assertEquals("Your solution is correct.", result.generalFeedback());
        assertTrue(result.criteria().stream().anyMatch(x -> x.name().equals("Syntax") && x.feedback().equals("Valid Syntax")));
        assertEquals(1, result.criteria().size());
    }

    @Test
    void evaluateSubmitInvalidSyntax() {
        // Arrange
        SubmitSubmissionDto<HierarchicalClusteringSubmissionDto> dto = new SubmitSubmissionDto<>("test-user", "test-assignment", matrixTaskId, "en",
            SubmissionMode.SUBMIT, 3, new HierarchicalClusteringSubmissionDto("Distance 1.0 (1,2)"));

        // Act
        var result = evaluationService.evaluate(dto);

        // Assert
        assertNotNull(result);
        assertTrue(result.criteria().stream().anyMatch(x -> x.name().equals("Syntax")));
        assertEquals("Your solution is incorrect.", result.generalFeedback());
        assertEquals(BigDecimal.ZERO.stripTrailingZeros(), result.points().stripTrailingZeros());
        assertEquals(new BigDecimal(5).stripTrailingZeros(), result.maxPoints().stripTrailingZeros());
        assertEquals(1, result.criteria().size());
    }

    @Test
    void evaluateDiagnoseValid() {
        // Arrange
        SubmitSubmissionDto<HierarchicalClusteringSubmissionDto> dto = new SubmitSubmissionDto<>("test-user", "test-assignment", matrixTaskId, "en",
            SubmissionMode.DIAGNOSE, 0, new HierarchicalClusteringSubmissionDto("""
            Distance 1.0: (3,4)
            Distance 2.0: (1,2), (3,4)
            Distance 3.0: (5,6), (1,2), (3,4)
            Distance 4.0: (1,2,3,4), (5,6)
            Distance 5.0: (1,2,3,4,5,6)
            """));

        // Act
        var result = evaluationService.evaluate(dto);

        // Assert
        assertNotNull(result);
        assertEquals(new BigDecimal(5).stripTrailingZeros(), result.points().stripTrailingZeros());
        assertEquals(new BigDecimal(5).stripTrailingZeros(), result.maxPoints().stripTrailingZeros());
        assertEquals("Your solution is correct.", result.generalFeedback());
        assertEquals(1, result.criteria().size());
        assertTrue(result.criteria().stream().anyMatch(x -> x.name().equals("Syntax") && x.feedback().equals("Valid Syntax")));
    }

    @Test
    void evaluateDiagnoseInvalidSyntax() {
        // Arrange
        SubmitSubmissionDto<HierarchicalClusteringSubmissionDto> dto = new SubmitSubmissionDto<>("test-user", "test-assignment", matrixTaskId, "en",
            SubmissionMode.DIAGNOSE, 3, new HierarchicalClusteringSubmissionDto("Distance 1.0 (1,2)"));

        // Act
        var result = evaluationService.evaluate(dto);

        // Assert
        assertNotNull(result);
        assertEquals(BigDecimal.ZERO.stripTrailingZeros(), result.points().stripTrailingZeros());
        assertEquals(new BigDecimal(5).stripTrailingZeros(), result.maxPoints().stripTrailingZeros());
        assertEquals("Your solution is incorrect.", result.generalFeedback());
        assertEquals(1, result.criteria().size());
        assertTrue(result.criteria().stream().anyMatch(x -> x.name().equals("Syntax")));
    }

    @Test
    void evaluateDiagnoseNoFeedback() {
        // Arrange
        SubmitSubmissionDto<HierarchicalClusteringSubmissionDto> dto = new SubmitSubmissionDto<>("test-user", "test-assignment", matrixTaskId, "en",
            SubmissionMode.DIAGNOSE, 0, new HierarchicalClusteringSubmissionDto("""
            Distance 1.0: (3,4)
            Distance 2.0: (1,2), (3,4)
            Distance 3.0: (5,6), (1,2), (3,4)
            Distance 4.0: (1,2,3,4,5), (5,6)
            Distance 5.0: (1,2,3,4,4,5,6)
            """));


        // Act
        var result = evaluationService.evaluate(dto);

        // Assert
        assertNotNull(result);
        assertEquals(new BigDecimal(3).stripTrailingZeros(), result.points().stripTrailingZeros());
        assertEquals(new BigDecimal(5).stripTrailingZeros(), result.maxPoints().stripTrailingZeros());
        assertEquals("Your solution is partially correct.", result.generalFeedback());
        assertEquals(1, result.criteria().size());
        assertTrue(result.criteria().stream().anyMatch(x -> x.name().equals("Syntax") && x.feedback().equals("Valid Syntax")));
    }

    @Test
    void evaluateWrongOrder() {
        // Arrange
        var task = getNewTask(2L);
        task.setWrongOrderPenalty(BigDecimal.TWO);
        taskRepository.save(task);

        SubmitSubmissionDto<HierarchicalClusteringSubmissionDto> dto = new SubmitSubmissionDto<>("test-user", "test-assignment", 2L, "en",
            SubmissionMode.DIAGNOSE, 2, new HierarchicalClusteringSubmissionDto("""
            Distance 1.0: (3,4)
            Distance 3.0: (5,6), (1,2), (3,4)
            Distance 2.0: (1,2), (3,4)
            Distance 5.0: (1,2,3,4,5,6)
            Distance 4.0: (1,2,3,4), (5,6)
            """));


        // Act
        var result = evaluationService.evaluate(dto);

        // Assert
        assertNotNull(result);
        assertEquals(new BigDecimal(3).stripTrailingZeros(), result.points().stripTrailingZeros());
        assertEquals(new BigDecimal(5).stripTrailingZeros(), result.maxPoints().stripTrailingZeros());
        assertEquals("Your solution is partially correct.", result.generalFeedback());
        assertEquals(2, result.criteria().size());
        assertTrue(result.criteria().stream().anyMatch(x -> x.name().equals("Order") && x.feedback().equals("The distances in your solution are in the wrong order.")));
    }

    private @NotNull HierarchicalClusteringTask getNewTask(Long id) {
        var task = new HierarchicalClusteringTask(id, new BigDecimal(5), TaskStatus.APPROVED, new HierarchicalClusteringTask.DistanceMatrix(List.of("1", "2", "3", "4", "5", "6"),
            toBigDecimalMatrix(new double[][]{
                { 0,  2,  6,  7,  5,  8},
                { 2,  0,  4, 13, 12, 11},
                { 6,  4,  0,  1, 14,  9},
                { 7, 13,  1,  0, 10, 10},
                { 5, 12, 14, 10,  0,  3},
                { 8, 11,  9, 10,  3,  0},
            })), BigDecimal.ONE);
        task.setLinkageMethod(LinkageMethod.SINGLE);
        createSolution(task);
        return task;
    }

    private void createSolution(HierarchicalClusteringTask task) {
        // delete all old clusters and merges for this task (as they are not needed anymore) and compute and persist the new solution
        List<HierarchicalClusteringMerge> oldSolution = task.getSolutionMergeHistory();
        Map<List<String>, HierarchicalClusteringCluster> clusterLookup = new HashMap<>();

        for (HierarchicalClusteringMerge merge : oldSolution) {
            HierarchicalClusteringCluster clusterLeft = merge.getSourceCluster1();
            clusterLookup.putIfAbsent(clusterLeft.getDataPoints(), clusterLeft);

            HierarchicalClusteringCluster clusterRight = merge.getSourceCluster2();
            clusterLookup.putIfAbsent(clusterRight.getDataPoints(), clusterRight);

            HierarchicalClusteringCluster result = merge.getResult();
            clusterLookup.putIfAbsent(result.getDataPoints(), result);
        }

        task.getSolutionMergeHistory().clear();
        mergeRepository.flush();
        clusterRepository.deleteAll(clusterLookup.values());

        // create new solution
        List<HierarchicalClusteringMerge> solutionMergeHistory = new NaiveAgglomerativeClusteringAlgorithm(task.getLinkageMethod())
            .cluster(task.getDistanceMatrix());

        for (HierarchicalClusteringMerge merge : solutionMergeHistory) {
            HierarchicalClusteringCluster clusterLeft = merge.getSourceCluster1();
            if (clusterLeft != null && clusterLeft.getDataPoints().size() == 1) {
                clusterRepository.save(clusterLeft);
            }

            HierarchicalClusteringCluster clusterRight = merge.getSourceCluster2();
            if (clusterRight != null && clusterRight.getDataPoints().size() == 1) {
                clusterRepository.save(clusterRight);
            }

            clusterRepository.save(merge.getResult());

            task.getSolutionMergeHistory().add(merge);
            merge.setTask(task);
        }

        task.setDendrogramModel(new DendrogramModelBuilder().build(solutionMergeHistory));
    }

    private BigDecimal[][] toBigDecimalMatrix(double[][] values) {
        BigDecimal[][] result = new BigDecimal[values.length][];
        for (int i = 0; i < values.length; i++) {
            result[i] = new BigDecimal[values[i].length];
            for (int j = 0; j < values[i].length; j++) {
                result[i][j] = BigDecimal.valueOf(values[i][j]);
            }
        }
        return result;
    }
}

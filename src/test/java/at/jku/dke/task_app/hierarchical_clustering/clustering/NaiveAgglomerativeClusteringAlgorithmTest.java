package at.jku.dke.task_app.hierarchical_clustering.clustering;

import at.jku.dke.task_app.hierarchical_clustering.data.entities.HierarchicalClusteringMerge;
import at.jku.dke.task_app.hierarchical_clustering.data.entities.HierarchicalClusteringTask;
import at.jku.dke.task_app.hierarchical_clustering.evaluation.EvaluationService;
import at.jku.dke.task_app.hierarchical_clustering.evaluation.SubmissionInputParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.SortedMap;

import static org.junit.jupiter.api.Assertions.*;

class NaiveAgglomerativeClusteringAlgorithmTest {

    private HierarchicalClusteringTask.DistanceMatrix matrix;
    private HierarchicalClusteringAlgorithm singleLinkageClustering;
    private HierarchicalClusteringAlgorithm completeLinkageClustering;
    private SubmissionInputParser parser;

    @BeforeEach
    void setUp() {
        this.matrix = new HierarchicalClusteringTask.DistanceMatrix(
            List.of("1", "2", "3", "4", "5", "6", "7", "8"),
            toBigDecimalMatrix(new double[][]{
                {  0.0, 17.5,  1.0, 25.5, 14.5, 19.0,  8.0, 12.0 },
                { 17.5,  0.0, 13.0, 18.5,  9.0,  1.0, 20.5,  0.5 },
                {  1.0, 13.0,  0.0,  5.5,  7.5, 26.0, 21.0, 16.0 },
                { 25.5, 18.5,  5.5,  0.0, 17.0, 11.5,  6.0, 22.5 },
                { 14.5,  9.0,  7.5, 17.0,  0.0, 15.5,  5.5, 16.5 },
                { 19.0,  1.0, 26.0, 11.5, 15.5,  0.0, 17.5, 25.0 },
                {  8.0, 20.5, 21.0,  6.0,  5.5, 17.5,  0.0,  9.5 },
                { 12.0,  0.5, 16.0, 22.5, 16.5, 25.0,  9.5,  0.0 }
            })
        );

        this.singleLinkageClustering = new NaiveAgglomerativeClusteringAlgorithm(LinkageMethod.SINGLE);
        this.completeLinkageClustering = new NaiveAgglomerativeClusteringAlgorithm(LinkageMethod.COMPLETE);

        this.parser = new SubmissionInputParser(null, null);
    }

//    @Test
//    void genRandomMatrix() {
//        HierarchicalClusteringTask.DistanceMatrix distanceMatrix = new DistanceMatrixGenerator().generate(8);
//        double[][] distances = distanceMatrix.getDistances();
//        for (double[] distance : distances) {
//            System.out.print("{ ");
//            for (double v : distance) {
//                System.out.print(v + ",  ");
//            }
//            System.out.println("},");
//        }
//    }

    @Test
    void testSingleLinkage() {
        // Arrange & Act
        String expectedSolutionString = """
            Distance 0.5: (2,8)
            Distance 1.0: (1,3), (2,6,8)
            Distance 5.5: (1,3,4), (5,7), (2,6,8)
            Distance 6.0: (1,3,4,5,7), (2,6,8)
            Distance 9.0: (1,2,3,4,5,6,7,8)
            """;
        SortedMap<BigDecimal, EvaluationService.MergeEventAtDistance> expected = parser.parse(expectedSolutionString).mergeEvents();

        List<HierarchicalClusteringMerge> mergeHistory = singleLinkageClustering.cluster(this.matrix);
        var task = new HierarchicalClusteringTask();
        task.setSolutionMergeHistory(mergeHistory);
        task.setPointsPerCorrectCluster(BigDecimal.ONE);

        SortedMap<BigDecimal, EvaluationService.MergeEventAtDistance> actual = EvaluationService.buildEvaluationMergeHistoryForTask(task);

        // Assert
        assertEquals(expected.size(), actual.size());
        assertEquals(expected.keySet(), actual.keySet());
        for (BigDecimal key : actual.keySet()) {
            assertTrue(expected.containsKey(key));
            for (HierarchicalClusteringMerge merge : actual.get(key).newMerges()) {
                List<String> sorted = merge.getResult().getDataPoints().stream().sorted().toList();
                assertTrue(expected.get(key).newMerges().stream().anyMatch(m -> m.getResult().getDataPoints().stream().sorted().toList().equals(sorted)));
            }

            for (HierarchicalClusteringMerge merge : actual.get(key).inheritedMerges()) {
                List<String> sorted = merge.getResult().getDataPoints().stream().sorted().toList();
                assertTrue(expected.get(key).inheritedMerges().stream().anyMatch(m -> m.getResult().getDataPoints().stream().sorted().toList().equals(sorted)));
            }
        }
    }

    @Test
    void testCompleteLinkage() {
        // Arrange & Act
        String expectedSolutionString = """
            Distance 0.5: (2,8)
            Distance 1.0: (1,3), (2,8)
            Distance 5.5: (5,7), (2,8), (1,3)
            Distance 11.5: (4,6), (2,8), (1,3), (5,7)
            Distance 17.5: (1,2,3,8), (4,5,6,7)
            Distance 26.0: (1,2,3,4,5,6,7,8)
            """;
        SortedMap<BigDecimal, EvaluationService.MergeEventAtDistance> expected = parser.parse(expectedSolutionString).mergeEvents();

        List<HierarchicalClusteringMerge> mergeHistory = completeLinkageClustering.cluster(this.matrix);
        var task = new HierarchicalClusteringTask();
        task.setSolutionMergeHistory(mergeHistory);
        task.setPointsPerCorrectCluster(BigDecimal.ONE);

        SortedMap<BigDecimal, EvaluationService.MergeEventAtDistance> actual = EvaluationService.buildEvaluationMergeHistoryForTask(task);

        // Assert
        assertEquals(expected.size(), actual.size());
        assertEquals(expected.keySet(), actual.keySet());
        for (BigDecimal key : actual.keySet()) {
            assertTrue(expected.containsKey(key));
            for (HierarchicalClusteringMerge merge : actual.get(key).newMerges()) {
                List<String> sorted = merge.getResult().getDataPoints().stream().sorted().toList();
                assertTrue(expected.get(key).newMerges().stream().anyMatch(m -> m.getResult().getDataPoints().stream().sorted().toList().equals(sorted)));
            }

            for (HierarchicalClusteringMerge merge : actual.get(key).inheritedMerges()) {
                List<String> sorted = merge.getResult().getDataPoints().stream().sorted().toList();
                assertTrue(expected.get(key).inheritedMerges().stream().anyMatch(m -> m.getResult().getDataPoints().stream().sorted().toList().equals(sorted)));
            }
        }
    }

    @Test
    void testTieDetection() {
        // Arrange
        HierarchicalClusteringTask.DistanceMatrix matrix1 = new HierarchicalClusteringTask.DistanceMatrix(
            List.of("1", "2", "3", "4", "5", "6", "7", "8"),
            toBigDecimalMatrix(new double[][]{
                {  0.0, 17.5,  1.0, 25.5, 14.5, 19.0,  8.0, 12.0 },
                { 17.5,  0.0, 13.0, 18.5,  9.0,  1.0, 20.5,  0.5 },
                {  1.0, 13.0,  0.0,  5.5,  7.5, 26.0, 21.0, 16.0 },
                { 25.5, 18.5,  5.5,  0.0, 17.0, 11.5,  6.0, 22.5 },
                { 14.5,  9.0,  7.5, 17.0,  0.0, 15.5,  5.5, 16.5 },
                { 19.0,  1.0, 26.0, 11.5, 15.5,  0.0, 17.5,  0.5 },
                {  8.0, 20.5, 21.0,  6.0,  5.5, 17.5,  0.0,  9.5 },
                { 12.0,  0.5, 16.0, 22.5, 16.5,  0.5,  9.5,  0.0 }
            })
        );

        HierarchicalClusteringTask.DistanceMatrix matrix2 = new HierarchicalClusteringTask.DistanceMatrix(
            List.of("1", "2", "3", "4", "5", "6", "7", "8"),
            toBigDecimalMatrix(new double[][]{
                {  0.0, 17.5,  1.0, 25.5, 14.5, 19.0,  8.0,  6.0 },
                { 17.5,  0.0, 13.0, 18.5,  9.0,  1.0, 20.5,  0.5 },
                {  1.0, 13.0,  0.0,  5.5,  7.5, 26.0, 21.0, 16.0 },
                { 25.5, 18.5,  5.5,  0.0, 17.0, 11.5,  6.0, 22.5 },
                { 14.5,  9.0,  7.5, 17.0,  0.0, 15.5,  5.5, 16.5 },
                { 19.0,  1.0, 26.0, 11.5, 15.5,  0.0, 17.5, 25.0 },
                {  8.0, 20.5, 21.0,  6.0,  5.5, 17.5,  0.0,  9.5 },
                {  6.0,  0.5, 16.0, 22.5, 16.5, 25.0,  9.5,  0.0 }
            })
        );

        HierarchicalClusteringTask.DistanceMatrix matrix3 = new HierarchicalClusteringTask.DistanceMatrix(
            List.of("1", "2", "3", "4", "5", "6", "7", "8"),
            toBigDecimalMatrix(new double[][]{
                {  0.0, 17.5,  1.0, 25.5, 14.5, 10.0,  8.0, 12.0 },
                { 17.5,  0.0, 13.0, 18.5,  9.0,  1.0, 20.5,  0.5 },
                {  1.0, 13.0,  0.0,  5.5,  7.5, 11.5, 21.0, 16.0 },
                { 25.5, 18.5,  5.5,  0.0, 17.0, 11.5,  6.0, 22.5 },
                { 14.5,  9.0,  7.5, 17.0,  0.0, 15.5,  5.5, 16.5 },
                { 10.0,  1.0, 11.5, 11.5, 15.5,  0.0, 17.5, 25.0 },
                {  8.0, 20.5, 21.0,  6.0,  5.5, 17.5,  0.0,  9.5 },
                { 12.0,  0.5, 16.0, 22.5, 16.5, 25.0,  9.5,  0.0 }
            })
        );

        HierarchicalClusteringTask.DistanceMatrix matrix4 = new HierarchicalClusteringTask.DistanceMatrix(
            List.of("1", "2", "3", "4", "5", "6", "7", "8"),
            toBigDecimalMatrix(new double[][]{
                {  0.0, 17.5,  1.0, 25.5, 14.5, 10.0,  8.0,  6.0 },
                { 17.5,  0.0, 13.0, 18.5,  9.0,  1.0, 20.5,  0.5 },
                {  1.0, 13.0,  0.0,  5.5,  7.5, 11.5, 21.0, 16.0 },
                { 25.5, 18.5,  5.5,  0.0, 17.0, 11.5,  6.0, 22.5 },
                { 14.5,  9.0,  7.5, 17.0,  0.0, 15.5,  5.5, 16.5 },
                { 10.0,  1.0, 11.5, 11.5, 15.5,  0.0, 17.5,  0.5 },
                {  8.0, 20.5, 21.0,  6.0,  5.5, 17.5,  0.0,  9.5 },
                {  6.0,  0.5, 16.0, 22.5, 16.5,  0.5,  9.5,  0.0 }
            })
        );

        // Act & Assert
        IllegalStateException e = assertThrows(IllegalStateException.class, () -> singleLinkageClustering.cluster(matrix1));
        assertThrows(IllegalStateException.class, () -> completeLinkageClustering.cluster(matrix1));
        assertEquals("Solution for current Matrix leads to Tiebreaker! Consider changing or re-arranging duplicate distances.", e.getMessage());

        assertThrows(IllegalStateException.class, () -> singleLinkageClustering.cluster(matrix2));
        assertDoesNotThrow(() -> completeLinkageClustering.cluster(matrix2));

        assertDoesNotThrow(() -> singleLinkageClustering.cluster(matrix3));
        assertThrows(IllegalStateException.class, () -> completeLinkageClustering.cluster(matrix3));

        assertThrows(IllegalStateException.class, () -> singleLinkageClustering.cluster(matrix4));
        assertThrows(IllegalStateException.class, () -> completeLinkageClustering.cluster(matrix4));
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

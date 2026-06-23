package at.jku.dke.task_app.hierarchical_clustering.dendrogram;

import at.jku.dke.task_app.hierarchical_clustering.data.entities.HierarchicalClusteringCluster;
import at.jku.dke.task_app.hierarchical_clustering.data.entities.HierarchicalClusteringMerge;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Builds a dendrogram model from a list of hierarchical clustering merges.
 */
public class DendrogramModelBuilder {

    /**
     * Builds a {@link DendrogramModel} from the provided list of merges.
     *
     * @param merges The list of hierarchical clustering merges.
     * @return The resulting dendrogram model.
     * @throws IllegalArgumentException If the merge list is null or empty.
     * @throws IllegalStateException If a required node is missing during tree construction.
     */
    public DendrogramModel build(List<HierarchicalClusteringMerge> merges) {
        if (merges == null || merges.isEmpty()) {
            throw new IllegalArgumentException("Merge list must not be empty.");
        }

        // label -> node; pre-populate with all leaf nodes seen across all merges
        Map<String, DendrogramModel.Node> nodeMap = new HashMap<>();

        for (HierarchicalClusteringMerge merge : merges) {
            ensureLeaves(merge.getSourceCluster1(), nodeMap);
            ensureLeaves(merge.getSourceCluster2(), nodeMap);
        }

        // Replay merges in step order, building the tree bottom-up
        DendrogramModel.Node root = null;
        for (HierarchicalClusteringMerge merge : merges) {
            String leftLabel = merge.getSourceCluster1().getLabel();
            String rightLabel = merge.getSourceCluster2().getLabel();
            String newLabel = merge.getResult().getLabel();
            BigDecimal height = merge.getDistance();

            DendrogramModel.Node leftNode = nodeMap.get(leftLabel);
            DendrogramModel.Node rightNode = nodeMap.get(rightLabel);

            if (leftNode  == null) {
                throw new IllegalStateException("No node for left label:  " + leftLabel);
            }

            if (rightNode == null) {
                throw new IllegalStateException("No node for right label: " + rightLabel);
            }

            DendrogramModel.Node merged = new DendrogramModel.Node(newLabel, height.doubleValue(), leftNode, rightNode);
            nodeMap.put(newLabel, merged);
            root = merged;
        }

        List<String> leafOrder = inOrderLeaves(root);

        return new DendrogramModel(leafOrder, root);
    }

    /**
     * Ensures that leaf nodes are registered for a cluster in the node map.
     *
     * @param cluster The cluster to check.
     * @param nodeMap The map of cluster labels to dendrogram nodes.
     */
    private void ensureLeaves(HierarchicalClusteringCluster cluster, Map<String, DendrogramModel.Node> nodeMap) {
        List<String> points = cluster.getDataPoints();
        if (points.size() == 1) {
            nodeMap.putIfAbsent(cluster.getLabel(), new DendrogramModel.Node(cluster.getLabel()));
        }
    }

    /**
     * Collects the leaf labels of a subtree in in-order traversal.
     *
     * @param node The root node of the subtree.
     * @return The list of leaf labels.
     */
    private List<String> inOrderLeaves(DendrogramModel.Node node) {
        List<String> result = new ArrayList<>();
        collectLeaves(node, result);
        return result;
    }

    /**
     * Helper method to recursively collect leaf labels.
     *
     * @param node The current node.
     * @param acc The accumulator list to collect leaf labels into.
     */
    private void collectLeaves(DendrogramModel.Node node, List<String> acc) {
        if (node == null) {
            return;
        }

        if (node.isLeaf()) {
            acc.add(node.getLabel());
            return;
        }

        collectLeaves(node.getLeft(),  acc);
        collectLeaves(node.getRight(), acc);
    }
}

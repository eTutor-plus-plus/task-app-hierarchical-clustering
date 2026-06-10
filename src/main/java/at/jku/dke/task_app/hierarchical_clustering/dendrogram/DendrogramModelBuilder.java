package at.jku.dke.task_app.hierarchical_clustering.dendrogram;

import at.jku.dke.task_app.hierarchical_clustering.data.entities.HierarchicalClusteringCluster;
import at.jku.dke.task_app.hierarchical_clustering.data.entities.HierarchicalClusteringMerge;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DendrogramModelBuilder {

    public DendrogramModel build(List<HierarchicalClusteringMerge> merges) {
        if (merges == null || merges.isEmpty()) {
            throw new IllegalArgumentException("Merge list must not be empty.");
        }

        // label -> node; pre-populate with all leaf nodes seen across all merges
        Map<String, DendrogramModel.Node> nodeMap = new HashMap<>();

        for (HierarchicalClusteringMerge merge : merges) {
            ensureLeaves(merge.getSourceCluster1(),  nodeMap);
            ensureLeaves(merge.getSourceCluster2(), nodeMap);
        }

        // Replay merges in step order, building the tree bottom-up
        DendrogramModel.Node root = null;
        for (HierarchicalClusteringMerge merge : merges) {
            String leftLabel  = merge.getSourceCluster1().getLabel();
            String rightLabel = merge.getSourceCluster2().getLabel();
            String newLabel   = merge.getResult().getLabel();
            BigDecimal height     = merge.getDistance();

            DendrogramModel.Node leftNode  = nodeMap.get(leftLabel);
            DendrogramModel.Node rightNode = nodeMap.get(rightLabel);

            if (leftNode  == null) throw new IllegalStateException("No node for left label:  " + leftLabel);
            if (rightNode == null) throw new IllegalStateException("No node for right label: " + rightLabel);

            DendrogramModel.Node merged = new DendrogramModel.Node(newLabel, height.doubleValue(), leftNode, rightNode);
            nodeMap.put(newLabel, merged);
            root = merged;
        }

        List<String> leafOrder = inOrderLeaves(root);

        return new DendrogramModel(leafOrder, root);
    }

    private void ensureLeaves(HierarchicalClusteringCluster cluster, Map<String, DendrogramModel.Node> nodeMap) {
        List<String> points = cluster.getDataPoints();
        if (points.size() == 1) {
            // True leaf — register only if not already present (may appear in multiple merges)
            nodeMap.putIfAbsent(cluster.getLabel(), new DendrogramModel.Node(cluster.getLabel()));
        }
        // Multi-point clusters will be added as internal nodes when their merge step is processed
    }

    private List<String> inOrderLeaves(DendrogramModel.Node node) {
        List<String> result = new ArrayList<>();
        collectLeaves(node, result);
        return result;
    }

    private void collectLeaves(DendrogramModel.Node node, List<String> acc) {
        if (node == null) return;
        if (node.isLeaf()) {
            acc.add(node.getLabel());
            return;
        }
        collectLeaves(node.getLeft(),  acc);
        collectLeaves(node.getRight(), acc);
    }
}

package at.jku.dke.task_app.hierarchical_clustering.generators.dendrogram;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class DendrogramModel {

    private List<String> leafOrder;
    private Node root;

    public DendrogramModel() {}

    public DendrogramModel(List<String> leafOrder, Node root) {
        this.leafOrder = leafOrder;
        this.root = root;
    }

    public List<String> getLeafOrder() {
        return leafOrder;
    }

    public void setLeafOrder(List<String> leafOrder) {
        this.leafOrder = leafOrder;
    }

    public Node getRoot() {
        return root;
    }

    public void setRoot(Node root) {
        this.root = root;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Node {

        private String label;
        private double height;
        private Node left;
        private Node right;

        public Node() {}

        public Node(String label) {
            this.label  = label;
            this.height = 0.0;
        }

        public Node(String label, double height, Node left, Node right) {
            this.label = label;
            this.height = height;
            this.left = left;
            this.right = right;
        }

        @JsonIgnore
        public boolean isLeaf() {
            return left == null && right == null;
        }

        public String getLabel() {
            return label;
        }

        public double getHeight() {
            return height;
        }

        public Node getLeft() {
            return left;
        }

        public Node getRight() {
            return right;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public void setHeight(double height) {
            this.height = height;
        }

        public void setLeft(Node left) {
            this.left = left;
        }

        public void setRight(Node right) {
            this.right = right;
        }
    }
}

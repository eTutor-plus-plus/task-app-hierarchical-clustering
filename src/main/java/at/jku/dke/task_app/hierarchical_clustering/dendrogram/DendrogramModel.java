package at.jku.dke.task_app.hierarchical_clustering.dendrogram;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * Represents a dendrogram model used in hierarchical clustering.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DendrogramModel {

    private List<String> leafOrder;
    private Node root;

    /**
     * Creates a new instance of {@link DendrogramModel}.
     */
    public DendrogramModel() {}

    /**
     * Creates a new instance of {@link DendrogramModel}.
     *
     * @param leafOrder The leaf order.
     * @param root The root node.
     */
    public DendrogramModel(List<String> leafOrder, Node root) {
        this.leafOrder = leafOrder;
        this.root = root;
    }

    /**
     * Gets the leaf order.
     *
     * @return The leaf order.
     */
    public List<String> getLeafOrder() {
        return leafOrder;
    }

    /**
     * Sets the leaf order.
     *
     * @param leafOrder The leaf order.
     */
    public void setLeafOrder(List<String> leafOrder) {
        this.leafOrder = leafOrder;
    }

    /**
     * Gets the root node.
     *
     * @return The root node.
     */
    public Node getRoot() {
        return root;
    }

    /**
     * Sets the root node.
     *
     * @param root The root node.
     */
    public void setRoot(Node root) {
        this.root = root;
    }

    /**
     * Represents a node in the dendrogram.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Node {

        private String label;
        private double height;
        private Node left;
        private Node right;

        /**
         * Creates a new instance of {@link Node}.
         */
        public Node() {}

        /**
         * Creates a new instance of {@link Node}.
         *
         * @param label The label.
         */
        public Node(String label) {
            this.label  = label;
            this.height = 0.0;
        }

        /**
         * Creates a new instance of {@link Node}.
         *
         * @param label The label.
         * @param height The height.
         * @param left The left child node.
         * @param right The right child node.
         */
        public Node(String label, double height, Node left, Node right) {
            this.label = label;
            this.height = height;
            this.left = left;
            this.right = right;
        }

        /**
         * Checks whether the node is a leaf.
         *
         * @return True if the node is a leaf, false otherwise.
         */
        @JsonIgnore
        public boolean isLeaf() {
            return left == null && right == null;
        }

        /**
         * Gets the label.
         *
         * @return The label.
         */
        public String getLabel() {
            return label;
        }

        /**
         * Sets the label.
         *
         * @param label The label.
         */
        public void setLabel(String label) {
            this.label = label;
        }

        /**
         * Gets the height.
         *
         * @return The height.
         */
        public double getHeight() {
            return height;
        }

        /**
         * Sets the height.
         *
         * @param height The height.
         */
        public void setHeight(double height) {
            this.height = height;
        }

        /**
         * Gets the left child node.
         *
         * @return The left child node.
         */
        public Node getLeft() {
            return left;
        }

        /**
         * Sets the left child node.
         *
         * @param left The left child node.
         */
        public void setLeft(Node left) {
            this.left = left;
        }

        /**
         * Gets the right child node.
         *
         * @return The right child node.
         */
        public Node getRight() {
            return right;
        }

        /**
         * Sets the right child node.
         *
         * @param right The right child node.
         */
        public void setRight(Node right) {
            this.right = right;
        }
    }
}

package ir.analysis.liveness;

import utils.NotNull;

import java.util.HashSet;
import java.util.Set;

/**
 * Undirected graph node
 */
class Node<K> {
    @NotNull
    private K value;
    @NotNull
    private Set<@NotNull Node<K>> edges = new HashSet<>();
    @NotNull
    private Set<@NotNull Node<K>> removedEdges = new HashSet<>();

    public Node(@NotNull K value) {
        this.value = value;
    }

    public void edge(Node<K> node) {
        node.edges.add(this);
        this.edges.add(node);

        removedEdges.remove(node);
    }

    public void removeEdge(Node<K> node) {
        if (edges.contains(node)) {
            node.edges.remove(this);
            node.removedEdges.add(this);

            edges.remove(node);
            removedEdges.add(node);
        }
    }

    public void restore(Node<K> node) {
        if (removedEdges.contains(node)) {
            removedEdges.remove(node);
            edges.add(node);

            node.removedEdges.remove(this);
            node.edges.add(this);
        }
    }

    public boolean hasEdge(Node<K> node) {
        return edges.contains(node);
    }

    @NotNull
    public K getValue() {
        return value;
    }

    @NotNull
    public Set<@NotNull Node<K>> edges() {
        return edges;
    }

    public int edgesCount() {
        return edges.size();
    }

}

package ir.optimizations.liveness;

import utils.NotNull;

import java.util.HashSet;
import java.util.Set;

class Node<K> {
    @NotNull
    private K value;
    @NotNull
    private Set<@NotNull Node<K>> edges = new HashSet<>();

    public Node(@NotNull K value) {
        this.value = value;
    }

    public void edge(Node<K> node) {
        node.edges.add(this);
        this.edges.add(node);
    }

    public boolean hasEdge(Node<K> node) {
        return edges.contains(node);
    }

    @NotNull
    public K getValue() {
        return value;
    }

    @NotNull
    public Set<@NotNull Node<K>> getEdges() {
        return edges;
    }
}

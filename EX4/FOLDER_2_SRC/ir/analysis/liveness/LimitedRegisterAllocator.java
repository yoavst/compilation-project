package ir.analysis.liveness;

import ir.analysis.IRBlock;
import ir.registers.Register;
import utils.NotNull;

import java.util.*;
import java.util.function.Function;

/**
 * Allocates real register for temporary registers
 */
public class LimitedRegisterAllocator {
    private final int registersCount;

    public LimitedRegisterAllocator(int registersCount) {
        this.registersCount = registersCount;
    }

    public Map<Register, Integer> allocateRealRegister(@NotNull Collection<IRBlock> blocks) {
        LivenessAnalysis analysis = new LivenessAnalysis();
        analysis.run(blocks);
        List<Node<Register>> graph = analysis.inferenceGraph();
        removeNonTemporaryRegisters(graph);

        Map<Node<Register>, Integer> coloredGraph = color(graph, registersCount);
        Map<Register, Integer> result = new HashMap<>();
        coloredGraph.forEach((node, color) -> result.put(node.getValue(), color));
        return result;
    }

    @NotNull
    private static <K> Map<@NotNull Node<K>, @NotNull Integer> color(@NotNull List<@NotNull Node<K>> graph, int colorsCount) {
        assert colorsCount > 0;
        List<Node<K>> left = new ArrayList<>(graph);
        Stack<Node<K>> stack = new Stack<>();

        // 1. simplification
        while (!left.isEmpty()) {
            // find nodes with minimal number of edges
            int minimal = minimal(left, Node::edgesCount);
            Node<K> simplifiedNode = left.get(minimal);
            assert simplifiedNode.edgesCount() < colorsCount;
            // remove it from the graph and updated edges
            left.remove(minimal);
            stack.add(simplifiedNode);
            for (Node<K> leftNode : left) {
                leftNode.removeEdge(simplifiedNode);
            }
        }

        // 2. coloring
        Map<Node<K>, Integer> colors = new HashMap<>();
        while (!stack.isEmpty()) {
            // pop variable from the stack
            Node<K> node = stack.pop();
            // add it back to the graph
            colors.keySet().forEach(coloredNode -> coloredNode.restore(node));
            // color it
            boolean[] usedColors = new boolean[colorsCount];
            node.edges().stream().mapToInt(colors::get).forEach(i -> usedColors[i] = true);
            int chosenColor = -1;
            for (int i = 0; i < colorsCount; i++) {
                if (!usedColors[i]) {
                    chosenColor = i;
                    break;
                }
            }
            assert chosenColor >= 0;
            colors.put(node, chosenColor);
        }
        return colors;
    }

    private static <K> int minimal(@NotNull List<K> list, @NotNull Function<K, Integer> scoreFunction) {
        int minScore = Integer.MAX_VALUE;
        int index = 0;
        for (int i = 0; i < list.size(); i++) {
            int score = scoreFunction.apply(list.get(i));
            if (score <= minScore) {
                minScore = score;
                index = i;
            }
        }
        return index;
    }

    private static void removeNonTemporaryRegisters(@NotNull List<Node<Register>> graph) {
        // remove edges
        for (Node<Register> node : graph) {
            if (!node.getValue().isTemporary()) {
                for (Node<Register> registerNode : graph) {
                    registerNode.removeEdge(node);
                }
            }
        }
        // remove nodes
        graph.removeIf(node -> !node.getValue().isTemporary());
    }
}

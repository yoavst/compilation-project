package ir.analysis.liveness;

import ir.analysis.Analysis;
import ir.analysis.IRBlock;
import ir.registers.Register;
import ir.registers.ReturnRegister;
import utils.NotNull;

import java.util.*;

/**
 * Liveness analysis for set of blocks.
 */
public class LivenessAnalysis extends Analysis<Set<Register>> {
    private static final Set<Register> DEFAULT_VALUE = Collections.singleton(ReturnRegister.instance);

    public LivenessAnalysis() {
        super(false,
                LivenessAnalysis::union,
                Collections.singletonList(((command, old) -> update(old, command.getDependencies(), command.getInvalidates()))),
                DEFAULT_VALUE,
                Collections.emptySet()
        );
    }

    /**
     * Generate inference graph for the given analysis.
     */
    @NotNull
    public List<Node<Register>> inferenceGraph() {
        assert runner != null;

        Map<Register, Node<Register>> nodes = new HashMap<>();
        Map<IRBlock, List<Set<Register>>> outs = runner.out();
        for (List<Set<Register>> livenessInfoList : outs.values()) {
            for (Set<Register> registers : livenessInfoList) {

                List<Register> list = new ArrayList<>(registers);
                if (list.size() == 1) {
                    // add single node if needed.
                    nodes.computeIfAbsent(list.get(0), Node::new);
                } else {
                    for (int i = 0; i < list.size(); i++) {
                        for (int j = i + 1; j < list.size(); j++) {
                            Node<Register> first = nodes.computeIfAbsent(list.get(i), Node::new);
                            Node<Register> second = nodes.computeIfAbsent(list.get(j), Node::new);
                            first.edge(second);
                        }
                    }
                }
            }
        }
        return new ArrayList<>(nodes.values());
    }

    @NotNull
    private static <K> Set<K> union(@NotNull Set<K> a, @NotNull Set<K> b) {
        Set<K> result = new HashSet<>(a);
        result.addAll(b);
        return result;
    }

    @NotNull
    private static <K> Set<K> update(@NotNull Set<K> set, @NotNull Set<K> insert, @NotNull Set<K> remove) {
        /* first removes, then insert, to support expressions like:
         * t1 = t1 + 3
         * insert: t1
         * remove: t1
         */
        Set<K> result = new HashSet<>(set);
        result.removeAll(remove);
        result.addAll(insert);
        return result;
    }
}

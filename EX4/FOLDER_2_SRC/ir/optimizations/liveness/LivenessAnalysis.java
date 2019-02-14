package ir.optimizations.liveness;

import ir.optimizations.IRBlock;
import ir.optimizations.Optimization;
import ir.registers.Register;
import ir.registers.ReturnRegister;

import java.util.*;

/**
 * Liveness analysis for set of blocks. Assumes all the blocks are for the same procedure
 * It will return allocation graph depends on all of them)
 */
public class LivenessAnalysis extends Optimization<Set<Register>> {
    private static final Set<Register> DEFAULT_VALUE = Collections.singleton(ReturnRegister.instance);
    private OptimizationRun run;

    public LivenessAnalysis() {
        super(false,
                LivenessAnalysis::union,
                Collections.singletonList(((command, old) -> update(old, command.getDependencies(), command.getInvalidates()))),
                DEFAULT_VALUE,
                Collections.emptySet()
        );
    }

    public void run(List<IRBlock> blocks) {
        run = new OptimizationRun(blocks);
        run.run();
    }

    public List<Node<Register>> generateGraph() {
        assert run != null;

        Map<Register, Node<Register>> nodes = new HashMap<>();
        Map<IRBlock, List<Set<Register>>> outs = run.out();
        for (List<Set<Register>> livenessInfoList : outs.values()) {
            for (Set<Register> registers : livenessInfoList) {

                List<Register> list = new ArrayList<>(registers);
                for (int i = 0; i < list.size(); i++) {
                    for (int j = i + 1; j < list.size(); j++) {
                        Node<Register> first = nodes.computeIfAbsent(list.get(i), Node::new);
                        Node<Register> second = nodes.computeIfAbsent(list.get(j), Node::new);
                        first.edge(second);
                    }
                }
            }
        }
        return new ArrayList<>(nodes.values());
    }


    private static <K> Set<K> union(Set<K> a, Set<K> b) {
        Set<K> result = new HashSet<>(a);
        result.addAll(b);
        return result;
    }

    private static <K> Set<K> update(Set<K> set, Set<K> insert, Set<K> remove) {
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

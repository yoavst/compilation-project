package ir.analysis.liveness;

import ir.analysis.Analysis;
import ir.analysis.IRBlock;
import ir.commands.IRCommand;
import ir.commands.arithmetic.IRSetValueCommand;
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
        super(false, DEFAULT_VALUE, Collections.emptySet());
    }

    @NotNull
    @Override
    protected Set<Register> transfer(@NotNull IRCommand command, @NotNull Set<Register> old) {
        if (old.isEmpty())
            return command.getDependencies();

        /* first removes, then insert, to support expressions like:
         * t1 = t1 + 3
         * insert: t1
         * remove: t1
         */
        Set<Register> result = new HashSet<>(old);
        result.removeAll(command.getInvalidates());
        result.addAll(command.getDependencies());
        return result;
    }

    @NotNull
    @Override
    protected Set<Register> join(@NotNull Set<Register> v1, @NotNull Set<Register> v2) {
        if (v2.isEmpty())
            return v1;
        else if (v1.isEmpty())
            return v2;

        Set<Register> result = new HashSet<>(v1);
        result.addAll(v2);
        return result;
    }

    /**
     * Generate inference graph for the given analysis.
     */
    @NotNull List<Node<Register>> inferenceGraph() {
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

    public boolean deadCodeElimination() {
        final boolean[] hasEliminated = {false};
        runner.out().forEach((block, info) -> {
            ListIterator<IRCommand> iterator = block.commands.listIterator();
            for (int i = 0; iterator.hasNext(); i++) {
                IRCommand command = iterator.next();
                if (command.canBeOptimized()) {
                    // Check if one of the invalidated registers is a life after the command
                    Set<Register> shared = new HashSet<>(command.getInvalidates());
                    shared.retainAll(iterator.hasNext() ? info.get(i + 1) : runner.in().get(block).get(i));
                    if (shared.isEmpty()) {
                        // remove command
                        hasEliminated[0] = true;
                        iterator.remove();
                        continue;
                    }
                }

                if (command instanceof IRSetValueCommand && ((IRSetValueCommand) command).dest.equals(((IRSetValueCommand) command).source)) {
                    hasEliminated[0] = true;
                    iterator.remove();
                }
            }
        });
        return hasEliminated[0];
    }
}

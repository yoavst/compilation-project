package ir.optimizations;

import ir.IRCommand;
import ir.registers.Register;
import ir.registers.ReturnRegister;

import java.util.*;

public class LivenessAnalysis extends Optimization<Set<Register>> {
    private static final Set<Register> DEFAULT_VALUE = Collections.singleton(ReturnRegister.instance);
    private static final List<Transfer<Set<Register>>> TRANSFERS = Collections.singletonList(
            ((command, old) -> update(old, command.getDependencies(), command.getInvalidates()))
    );

    public LivenessAnalysis() {
        super(false, LivenessAnalysis::union, TRANSFERS , DEFAULT_VALUE, Collections.emptySet());
    }

    public void run(List<IRBlock> blocks) {
        OptimizationRun run = new OptimizationRun(blocks);
        run.run();
        System.out.println();
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

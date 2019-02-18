package ir.analysis;

import ir.commands.IRCommand;
import utils.NotNull;

import java.util.*;

/**
 * Represents an analysis using the analysis framework from class.
 */
public abstract class Analysis<@NotNull K> {
    private boolean isForward;
    @NotNull
    private final K initialValue;
    @NotNull
    private final K zero;

    @NotNull
    protected abstract K transfer(@NotNull IRCommand command, @NotNull K old);

    @NotNull
    protected abstract K join(@NotNull K v1, @NotNull K v2);

    protected AnalysisRunner runner;

    public Analysis(boolean isForward, @NotNull K defaultValue, @NotNull K zero) {
        this.isForward = isForward;
        this.initialValue = defaultValue;
        this.zero = zero;
    }

    /**
     * Will run the analysis on the given blocks.
     */
    public void run(Collection<IRBlock> blocks) {
        runner = new AnalysisRunner(blocks);
        runner.run();
    }

    /**
     * Runner for the fixed point computation.
     */
    protected class AnalysisRunner {
        Map<IRBlock, List<K>> in = new HashMap<>();
        Map<IRBlock, List<K>> out = new HashMap<>();
        Collection<IRBlock> blocks;
        boolean isFixed = false;

        private AnalysisRunner(Collection<IRBlock> blocks) {
            this.blocks = blocks;
            /* initiate the analysis.
             *  for every statement:
             *   out[s] = zero
             *
             *  out[entry] = initialValue
             */
            for (IRBlock block : blocks) {
                List<K> inList = new ArrayList<>(block.commands.size());
                List<K> outList = new ArrayList<>(block.commands.size());

                if (!block.isEmpty()) {
                    // set zero
                    int size = block.commands.size();
                    for (int i = 0; i < size; i++) {
                        inList.add(zero);
                        outList.add(zero);
                    }
                    // set initial value
                    outList.set(isForward ? size - 1 : 0, initialValue);
                } else {
                    outList.add(initialValue);
                    inList.add(zero);
                }

                // save lists
                in.put(block, inList);
                out.put(block, outList);
            }
        }

        private void run() {
            boolean isFirstRound = true;
            while (!isFixed) {
                isFixed = true;
                for (IRBlock block : blocks) {
                    List<K> blockIn = in.get(block);
                    List<K> blockOut = out.get(block);

                    // handle join
                    Set<IRBlock> prev = isForward ? block.prev : block.next;
                    K newIn;
                    if (prev.isEmpty()) {
                        newIn = initialValue;
                    } else {
                        K joinedValue = zero;
                        for (IRBlock irBlock : prev) {
                                K k = blockOut(irBlock);
                                joinedValue = join(joinedValue, k);
                        }
                        newIn = joinedValue;
                    }
                    if (set(blockIn, 0, newIn) || isFirstRound) {
                        if (block.isEmpty()) {
                            set(blockOut, 0, blockIn.get(0));
                        } else {
                            // run transfer
                            for (int i = 0; i < blockIn.size() - 1; i++) {
                                IRCommand command = get(block.commands, i);
                                K transferResult = transfer(command, get(blockIn, i));
                                set(blockOut, i, transferResult);
                                set(blockIn, i + 1, transferResult);
                            }
                            // handle last - need only out.
                            int i = blockIn.size() - 1;
                            K transferResult = transfer(get(block.commands, i), get(blockIn, i));
                            set(blockOut, i, transferResult);
                        }
                    }
                }
                isFirstRound = false;
            }
        }

        private boolean set(@NotNull List<K> l, int i, K value) {
            int index = index(i, l);
            K old = l.get(index);

            boolean hasChanged = !old.equals(value);
            if (hasChanged)
                isFixed = false;

            l.set(index, value);
            return hasChanged;
        }

        @NotNull
        private <Value> Value get(@NotNull List<@NotNull Value> l, int i) {
            return l.get(index(i, l));
        }

        private <Value> int index(int i, List<@NotNull Value> l) {
            return isForward ? i : l.size() - i - 1;
        }

        /**
         * returns the out value of the whole block
         */
        private K blockOut(IRBlock block) {
            List<K> values = out.get(block);
            return isForward ? values.get(values.size() - 1) : values.get(0);
        }

        public Map<IRBlock, List<K>> in() {
            return in;
        }

        public Map<IRBlock, List<K>> out() {
            return out;
        }
    }
}

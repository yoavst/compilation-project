package ir.optimizations;

import ir.IRCommand;
import utils.NotNull;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;

public class Optimization<@NotNull K> {
    private boolean isForward;
    @NotNull
    private final BinaryOperator<K> joiner;
    @NotNull
    private final BiFunction<IRCommand, K, K> transfer;
    @NotNull
    private final K initialValue;
    @NotNull
    private final K zero;

    public Optimization(boolean isForward, @NotNull BinaryOperator<K> joiner, @NotNull List<BiFunction<IRCommand, K, K>> transfers, @NotNull K defaultValue, @NotNull K zero) {
        this.isForward = isForward;
        this.joiner = joiner;
        this.transfer = transfers.stream().reduce((command, value) -> value, (f1, f2) -> (command, value) -> f1.apply(command, f2.apply(command, value)));
        this.initialValue = defaultValue;
        this.zero = zero;
    }


    protected class OptimizationRun {
        Map<IRBlock, List<K>> in = new HashMap<>();
        Map<IRBlock, List<K>> out = new HashMap<>();
        List<IRBlock> blocks;
        boolean isFixed = false;

        public OptimizationRun(List<IRBlock> blocks) {
            this.blocks = blocks;
            for (IRBlock block : blocks) {
                List<K> inList = new ArrayList<>(block.commands.size());
                List<K> outList = new ArrayList<>(block.commands.size());

                if (!block.isEmpty()) {
                    int size = block.commands.size();
                    for (int i = 0; i < size; i++) {
                        inList.add(zero);
                        outList.add(zero);
                    }
                        outList.set(isForward ? size - 1 : 0, initialValue);
                } else {
                    outList.add(initialValue);
                    inList.add(zero);
                }

                in.put(block, inList);
                out.put(block, outList);
            }
        }

        public void run() {
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
                       newIn = prev.stream().map(this::blockOut).reduce(zero, joiner);
                    }
                    set(blockIn, 0, newIn);

                    if (block.isEmpty()) {
                        set(blockOut, 0, blockIn.get(0));
                    } else {
                        // run transfer
                        for (int i = 0; i < blockIn.size() - 1; i++) {
                            K transferResult = transfer.apply(get(block.commands, i), get(blockIn, i));
                            set(blockOut, i, transferResult);
                            set(blockIn, i + 1, transferResult);
                        }
                        // handle last - need only out.
                        int i = blockIn.size() - 1;
                        K transferResult = transfer.apply(get(block.commands, i), get(blockIn, i));
                        set(blockOut, i, transferResult);
                    }
                }
            }
        }

        public Map<IRBlock, List<K>> in() {
            return in;
        }

        public Map<IRBlock, List<K>> out() {
            return out;
        }

        private void set(List<K> l, int i, K value) {
            int index = index(i, l);
            K old = l.get(index);

            if (!old.equals(value))
                isFixed = false;

            l.set(index, value);
        }

        private <Value> Value get(List<Value> l, int i) {
            return l.get(index(i, l));
        }

        private <Value> int index(int i, List<Value> l) {
            return isForward ? i : l.size() - i - 1;
        }

        private K blockOut(IRBlock block) {
            List<K> values = out.get(block);
            return isForward ? values.get(values.size() - 1) : values.get(0);
        }
    }
}

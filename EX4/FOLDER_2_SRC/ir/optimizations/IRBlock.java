package ir.optimizations;

import ir.IRCommand;
import ir.flow.IRLabel;
import utils.NotNull;
import utils.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class IRBlock {
    private static int counter = 0;

    @NotNull
    List<@NotNull IRCommand> commands;
    @NotNull
    Set<@NotNull IRBlock> next;
    @NotNull
    Set<@NotNull IRBlock> prev;
    /**
     * The block that comes after this block, even if there is no way to go on to it.
     */
    @Nullable
    IRBlock realNextBlock;
    @Nullable
    IRLabel label;
    private final int id;

    private IRBlock(@NotNull List<@NotNull IRCommand> commands, @NotNull Set<@NotNull IRBlock> next, @NotNull Set<@NotNull IRBlock> prev, @Nullable IRLabel label) {
        this.commands = commands;
        this.next = next;
        this.prev = prev;
        this.label = label;
        id = counter++;
    }

    IRBlock(IRLabel label) {
        this(new ArrayList<>(), new HashSet<>(), new HashSet<>(), label);
    }

    IRBlock() {
        this(null);
    }

    @Override
    public int hashCode() {
        return id;
    }

    public boolean isOrphanBlock() {
        return prev.isEmpty();
    }

    @Override
    public String toString() {
        return commands.stream().map(IRCommand::toString).collect(Collectors.joining(" :: "));
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof IRBlock && ((IRBlock) obj).id == id;
    }

    public boolean isEmpty() {
        return commands.isEmpty();
    }
}

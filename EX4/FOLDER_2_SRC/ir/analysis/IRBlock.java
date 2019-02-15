package ir.analysis;

import ir.commands.IRCommand;
import ir.commands.flow.IRLabel;
import utils.NotNull;
import utils.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Represent a set of linear IR commands: if the first one is executed, then all of them will.
 */
public class IRBlock {
    private static int counter = 0;

    @NotNull
    public final List<@NotNull IRCommand> commands;
    @NotNull
    final Set<@NotNull IRBlock> next;
    @NotNull
    final Set<@NotNull IRBlock> prev;
    /**
     * The block that comes after this block, even if there is no way to go on to it.
     */
    @Nullable
    public IRBlock realNextBlock;
    @Nullable
    public IRLabel label;

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

    public boolean isStartingBlock() {
        return label != null && label.isStartingLabel();
    }

    public boolean isEmpty() {
        return commands.isEmpty();
    }

    @Override
    public String toString() {
        return commands.stream().map(IRCommand::toString).collect(Collectors.joining(" :: "));
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof IRBlock && ((IRBlock) obj).id == id;
    }

    /**
     * Run DFS from this node, and return all encountered nodes
     */
    public Set<IRBlock> scanGraph() {
        Set<IRBlock> encountered = new HashSet<>();
        scanGraph(encountered);
        return encountered;
    }

    /**
     * Run graph scan from this node, and return all encountered nodes
     */
    private void scanGraph(Set<IRBlock> encountered) {
        if (encountered.contains(this))
            return;
        encountered.add(this);

        prev.forEach(block -> block.scanGraph(encountered));
        next.forEach(block -> block.scanGraph(encountered));
    }
}

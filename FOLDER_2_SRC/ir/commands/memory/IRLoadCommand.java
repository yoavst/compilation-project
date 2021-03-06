package ir.commands.memory;

import ir.commands.IRAssignmentCommand;
import ir.registers.Register;
import utils.NotNull;

import java.util.Set;

import static utils.Utils.setOf;

public class IRLoadCommand extends IRAssignmentCommand {
    @NotNull
    public final Register source;

    public IRLoadCommand(@NotNull Register dest, @NotNull Register source) {
        super("var1 := *var2", dest);
        this.source = source;
        this.dependencies = setOf(source);
        this.invalidates = setOf(dest);
    }

    @Override
    public String toString() {
        return String.format("%s := *%s", dest, source);
    }

    @Override
    public boolean canBeOptimized() {
        return true;
    }
}

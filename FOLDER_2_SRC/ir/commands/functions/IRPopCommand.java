package ir.commands.functions;

import ir.commands.IRAssignmentCommand;
import ir.registers.Register;
import utils.NotNull;

import java.util.Set;

import static utils.Utils.setOf;

public class IRPopCommand extends IRAssignmentCommand {
    public IRPopCommand(@NotNull Register dest) {
        super("pop var", dest);
        this.invalidates = setOf(dest);
    }

    @Override
    public String toString() {
        return String.format("pop %s", dest);
    }

    @Override
    public boolean canBeOptimized() {
        return true;
    }
}

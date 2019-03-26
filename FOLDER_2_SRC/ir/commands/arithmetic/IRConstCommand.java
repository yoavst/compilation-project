package ir.commands.arithmetic;

import ir.commands.IRAssignmentCommand;
import ir.registers.Register;
import utils.NotNull;

import java.util.Set;

import static utils.Utils.setOf;

public class IRConstCommand extends IRAssignmentCommand {
    public final int value;

    public IRConstCommand(@NotNull Register dest, int value) {
        super("var := const", dest);
        this.value = value;
        this.invalidates = setOf(dest);
    }

    @Override
    public String toString() {
        return String.format("%s := %d", dest, value);
    }

    @Override
    public boolean canBeOptimized() {
        return !dest.isGlobal();
    }
}

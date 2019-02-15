package ir.commands.memory;

import ir.commands.IRAssignmentCommand;
import ir.registers.Register;
import utils.NotNull;

import java.util.Set;

import static utils.Utils.setOf;

public class IRStoreCommand extends IRAssignmentCommand {
    @NotNull
    public final Register source;
    public IRStoreCommand(@NotNull Register dest, @NotNull Register source) {
        super("*var1 := var2", dest);
        this.source = source;
    }

    @Override
    public Set<Register> getDependencies() {
        return setOf(source, dest);
    }

    @Override
    public String toString() {
        return String.format("*%s := %s", dest, source);
    }
}

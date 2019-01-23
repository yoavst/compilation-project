package ir.memory;

import ir.IRAssignmentCommand;
import ir.Register;
import utils.NotNull;

public class IRStoreCommand extends IRAssignmentCommand {
    @NotNull
    private final Register source;
    public IRStoreCommand(@NotNull Register dest, @NotNull Register source) {
        super("*var1 := var2", dest);
        this.source = source;
    }

    @Override
    public String toString() {
        return String.format("*%s := %s", dest, source);
    }
}

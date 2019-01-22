package ir.memory;

import ir.IRAssignmentCommand;
import utils.NotNull;

public class IRLoadCommand extends IRAssignmentCommand {
    @NotNull
    private final String source;
    public IRLoadCommand(@NotNull String dest, @NotNull String source) {
        super("var1 := *var2", dest);
        this.source = source;
    }

    @Override
    public String toString() {
        return String.format("%s := *%s", dest, source);
    }
}

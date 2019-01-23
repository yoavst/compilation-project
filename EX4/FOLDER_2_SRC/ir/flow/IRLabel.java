package ir.flow;

import ir.IRCommand;
import utils.NotNull;

public class IRLabel extends IRCommand {
    @NotNull
    private final String name;

    public IRLabel(@NotNull String name) {
        super("label:");
        this.name = name;
    }

    @Override
    public String toString() {
        return name + ":";
    }
}

package ir.functions;

import ir.IRCommand;
import utils.NotNull;

public class IRFunction extends IRCommand {
    @NotNull
    private final String name;

    public IRFunction(@NotNull String name) {
        super("function label:");
        this.name = name;
    }

    @Override
    public String toString() {
        return "function " + name + ":";
    }
}

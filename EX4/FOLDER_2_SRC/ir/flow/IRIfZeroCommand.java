package ir.flow;

import ir.IRCommand;
import utils.NotNull;

public class IRIfZeroCommand extends IRCommand {
    @NotNull
    private final String condition;
    @NotNull
    private final String label;
    public IRIfZeroCommand(@NotNull String condition, @NotNull String label) {
        super("ifz var1 goto label");
        this.condition = condition;
        this.label = label;
    }

    @Override
    public String toString() {
        return String.format("ifz %s goto %s", condition, label);
    }
}

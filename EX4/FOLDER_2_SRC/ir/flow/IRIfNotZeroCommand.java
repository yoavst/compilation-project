package ir.flow;

import ir.IRCommand;
import utils.NotNull;

public class IRIfNotZeroCommand extends IRCommand {
    @NotNull
    private final String condition;
    @NotNull
    private final String label;
    public IRIfNotZeroCommand(@NotNull String condition, @NotNull String label) {
        super("ifnz var1 goto label");
        this.condition = condition;
        this.label = label;
    }

    @Override
    public String toString() {
        return String.format("ifnz %s goto %s", condition, label);
    }
}

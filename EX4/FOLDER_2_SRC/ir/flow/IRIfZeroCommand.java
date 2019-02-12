package ir.flow;

import ir.IRCommand;
import ir.registers.Register;
import utils.NotNull;

public class IRIfZeroCommand extends IRCommand {
    @NotNull
    private final Register condition;
    @NotNull
    private final IRLabel label;
    public IRIfZeroCommand(@NotNull Register condition, @NotNull IRLabel label) {
        super("ifz var1 goto label");
        this.condition = condition;
        this.label = label;
    }

    @Override
    public String toString() {
        return String.format("ifz %s goto %s", condition, label);
    }
}

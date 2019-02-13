package ir.flow;

import ir.registers.Register;
import utils.NotNull;

public class IRIfNotZeroCommand extends IRFlowCommand {
    @NotNull
    private final Register condition;

    public IRIfNotZeroCommand(@NotNull Register condition, @NotNull IRLabel label) {
        super("ifnz var1 goto label", label);
        this.condition = condition;
    }

    @Override
    public String toString() {
        return String.format("ifnz %s goto %s", condition, label);
    }
}

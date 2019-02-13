package ir.flow;

import ir.IRCommand;
import ir.registers.Register;
import utils.NotNull;

public class IRIfZeroCommand extends IRFlowCommand{
    @NotNull
    private final Register condition;

    public IRIfZeroCommand(@NotNull Register condition, @NotNull IRLabel label) {
        super("ifz var1 goto label", label);
        this.condition = condition;
    }

    @Override
    public String toString() {
        return String.format("ifz %s goto %s", condition, label);
    }
}

package ir.commands.flow;

import ir.registers.Register;
import utils.NotNull;

import java.util.Set;

import static utils.Utils.setOf;

public class IRIfZeroCommand extends IRFlowCommand {
    @NotNull
    public final Register condition;

    public IRIfZeroCommand(@NotNull Register condition, @NotNull IRLabel label) {
        super("ifz var1 goto label", label);
        this.condition = condition;
    }

    @Override
    public Set<Register> getDependencies() {
        return setOf(condition);
    }

    @Override
    public String toString() {
        return String.format("ifz %s goto %s", condition, getLabel());
    }
}

package ir.commands.flow;

import ir.registers.Register;
import utils.NotNull;

import java.util.Set;

import static utils.Utils.setOf;

public class IRIfNotZeroCommand extends IRFlowCommand {
    @NotNull
    public final Register condition;

    public IRIfNotZeroCommand(@NotNull Register condition, @NotNull IRLabel label) {
        super("ifnz var1 goto label", label);
        this.condition = condition;
        this.dependencies = setOf(condition);
    }

    @Override
    public String toString() {
        return String.format("ifnz %s goto %s", condition, getLabel());
    }
}

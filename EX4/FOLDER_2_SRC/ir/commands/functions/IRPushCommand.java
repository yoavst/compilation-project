package ir.commands.functions;

import ir.commands.IRCommand;
import ir.registers.Register;
import utils.NotNull;

import java.util.Set;

import static utils.Utils.setOf;

public class IRPushCommand extends IRCommand {
    @NotNull
    public final Register source;

    public IRPushCommand(@NotNull Register source) {
        super("push var");
        this.source = source;
        this.dependencies = setOf(source);
    }

    @Override
    public String toString() {
        return String.format("push %s", source);
    }
}

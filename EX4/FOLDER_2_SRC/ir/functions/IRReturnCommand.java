package ir.functions;

import ir.IRCommand;
import ir.registers.Register;

import java.util.Set;

import static utils.Utils.setOf;

public class IRReturnCommand extends IRCommand {
    public IRReturnCommand() {
        super("return");
    }

    @Override
    public Set<Register> getDependencies() {
        return setOf();
    }

    @Override
    public Set<Register> getInvalidates() {
        return setOf();
    }

    @Override
    public String toString() {
        return "return";
    }
}

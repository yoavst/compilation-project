package ir.flow;

import ir.IRCommand;
import ir.registers.Register;
import utils.NotNull;

import java.util.Set;

import static utils.Utils.setOf;

public final class IRLabel extends IRCommand {
    @NotNull
    private final String name;

    public IRLabel(@NotNull String name) {
        super("label:");
        this.name = name;
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
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IRLabel label = (IRLabel) o;

        return name.equals(label.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}

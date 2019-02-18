package ir.registers;

/**
 * Represents a register in the IR level. There are many types of IR registers, and each may have different real representation.
 */
public abstract class Register {
    private final int id;

    Register(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public boolean isTemporary() {
        return false;
    }
    public boolean isGlobal() {
        return false;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return toString().equals(o.toString());
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }
}

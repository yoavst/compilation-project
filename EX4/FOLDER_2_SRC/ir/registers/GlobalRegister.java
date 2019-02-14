package ir.registers;

/**
 * Represents a global variable.
 * In assembly translation calls to this register will be translated to memory calls.
 */
public class GlobalRegister extends Register {
    public GlobalRegister(int id) {
        super(id);
    }
    @Override
    public String toString() {
        return "g" + getId();
    }
}

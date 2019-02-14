package ir.registers;

/**
 * Represents a local function variable.
 * In assembly translation calls to this register will be translated to memory stack calls.
 */
public class LocalRegister extends Register {
    public LocalRegister(int id) {
        super(id);
    }
    @Override
    public String toString() {
        return "l" + getId();
    }

}

package ir.registers;

/**
 * Represents a parameter to a function: p1 is the first parameter (or this if instance call), and so on...
 * In assembly translation calls to this register will be translated to memory stack calls.
 */
public class ParameterRegister extends Register {
    public ParameterRegister(int id) {
        super(id);
    }
    @Override
    public String toString() {
        return "p" + getId();
    }

}

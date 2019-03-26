package ir.registers;

/**
 * Represents a temporary register allocated during the runtime of the program
 * In assembly translation the register will be mapped to a real register.
 */
public final class TempRegister extends Register {
    public TempRegister(int id) {
        super(id);
    }
    @Override
    public String toString() {
        return "t" + getId();
    }

    public boolean isTemporary() {
        return true;
    }

}

package ir.registers;

/**
 * In order to return a value from function, once has to save the value to the return register.
 * In assembly translation calls to this register will be translated to memory stack calls.
 */
public class ReturnRegister extends Register {
    public static final ReturnRegister instance = new ReturnRegister();
    private ReturnRegister() {
        super(-2);
    }

    @Override
    public String toString() {
        return "r0";
    }
}

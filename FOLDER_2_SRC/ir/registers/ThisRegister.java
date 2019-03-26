package ir.registers;

/**
 * Represents the [this] parameter to an instance function.
 * In assembly translation calls to this register will be translated to memory stack calls.
 */
public class ThisRegister extends Register {
    public static final ThisRegister instance = new ThisRegister();
    private ThisRegister() {
        super(-2);
    }

    @Override
    public String toString() {
        return "this";
    }
}

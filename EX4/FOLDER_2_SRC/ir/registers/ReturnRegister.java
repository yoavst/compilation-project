package ir.registers;

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

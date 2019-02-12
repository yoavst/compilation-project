package ir.registers;

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

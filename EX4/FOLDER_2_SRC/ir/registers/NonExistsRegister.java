package ir.registers;

public class NonExistsRegister extends Register {
    public static final NonExistsRegister instance = new NonExistsRegister();
    private NonExistsRegister() {
        super(-1);
    }

    @Override
    public int getId() {
        throw new IllegalArgumentException("Should not use this register");
    }
}

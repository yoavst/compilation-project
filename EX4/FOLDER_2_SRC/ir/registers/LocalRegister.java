package ir.registers;

public class LocalRegister extends Register {
    public LocalRegister(int id) {
        super(id);
    }
    @Override
    public String toString() {
        return "l" + getId();
    }

}

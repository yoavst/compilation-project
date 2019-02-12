package ir.registers;

public class ConstantResourceRegister extends Register {
    public ConstantResourceRegister(int id) {
        super(id);
    }
    @Override
    public String toString() {
        return "c" + getId();
    }

}

package ir.registers;

public final class TempRegister extends Register {
    public TempRegister(int id) {
        super(id);
    }
    @Override
    public String toString() {
        return "t" + getId();
    }

}

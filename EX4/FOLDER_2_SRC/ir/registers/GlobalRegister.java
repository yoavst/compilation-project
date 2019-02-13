package ir.registers;

public class GlobalRegister extends Register {
    public GlobalRegister(int id) {
        super(id);
    }
    @Override
    public String toString() {
        return "g" + getId();
    }

}

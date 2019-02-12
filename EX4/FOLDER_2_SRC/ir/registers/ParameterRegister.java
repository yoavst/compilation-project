package ir.registers;

public class ParameterRegister extends Register {
    public ParameterRegister(int id) {
        super(id);
    }
    @Override
    public String toString() {
        return "p" + getId();
    }

}

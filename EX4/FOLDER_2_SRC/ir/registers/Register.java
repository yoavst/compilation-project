package ir.registers;

public class Register {
    private final int id;

    public Register(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Register register = (Register) o;
        return id == register.id;
    }

    @Override
    public int hashCode() {
        return id;
    }
}

package ast.expressions;

public class AST_EXP_INT extends AST_EXP {
    public int value;

    public AST_EXP_INT(int value, boolean sign) {
        if (value == 0 && !sign) {
            //throw new IllegalArgumentException("Only 16 bit signed number are supported");
        }
        value = value * (sign ? 1 : -1);
        if (value >= 32768 || value < -32768) {
            throw new IllegalArgumentException("Only 16 bit signed number are supported");
        }
        this.value = value;
    }

    public AST_EXP_INT(int value) {
        this(value, true);
    }

    @Override
    protected String name() {
        return "INT(" + value + ")";
    }
}

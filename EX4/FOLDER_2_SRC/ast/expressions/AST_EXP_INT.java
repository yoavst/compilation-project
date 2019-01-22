package ast.expressions;

import types.builtins.TypeInt;
import utils.NotNull;

public class AST_EXP_INT extends AST_EXP_CONSTANT {
    public int value;

    public AST_EXP_INT(int value, boolean sign) {
        super(TypeInt.instance);
        value = value * (sign ? 1 : -1);
        if (value >= 32768 || value < -32768) {
            throw new IllegalArgumentException("Only 16 bit signed number are supported");
        }
        this.value = value;
    }

    public AST_EXP_INT(int value) {
        this(value, true);
    }

    @NotNull
    @Override
    protected String name() {
        return "INT(" + value + ")";
    }
}

package ast.expressions;

import ir.utils.IRContext;
import ir.registers.Register;
import ir.commands.arithmetic.IRConstCommand;
import types.builtins.TypeInt;
import utils.NotNull;

public class AST_EXP_INT extends AST_EXP_CONSTANT {
    public final int value;

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

    @NotNull
    @Override
    public Register irMe(IRContext context) {
        Register temp = context.newRegister();
        context.command(new IRConstCommand(temp, value));
        return temp;
    }

    Object getConstValue() {
        return value;
    }
}

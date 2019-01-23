package ast.expressions;

import ir.IRContext;
import ir.Register;
import ir.arithmetic.IRConstCommand;
import ir.memory.IRLoadPreallocatedCommand;
import types.builtins.TypeString;
import utils.NotNull;

public class AST_EXP_STRING extends AST_EXP_CONSTANT {
    @NotNull
    public String value;

    public AST_EXP_STRING(@NotNull String value) {
        super(TypeString.instance);
        this.value = value;
    }

    @NotNull
    @Override
    protected String name() {
        if (value.length() > 5)
            return "STR(" + value.substring(0, 5) + "...)";
        return "STR(" + value + ")";
    }

    @NotNull
    @Override
    public Register irMe(IRContext context) {
        int stringResource = context.preallocateString(value);
        Register temp = context.getNewRegister();
        context.addCommand(new IRLoadPreallocatedCommand(temp, stringResource));
        return temp;
    }
}

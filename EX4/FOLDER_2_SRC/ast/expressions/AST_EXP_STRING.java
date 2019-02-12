package ast.expressions;

import ir.IRContext;
import ir.arithmetic.IRSetValueCommand;
import ir.registers.Register;
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

    @Override
    public @NotNull Register irMe(IRContext context) {
        Register temp = context.getNewRegister();
        context.addCommand(new IRSetValueCommand(temp, context.preallocateString(value)));
        return temp;
    }
}

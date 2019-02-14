package ast.expressions;

import ir.utils.IRContext;
import ir.registers.Register;
import ir.commands.arithmetic.IRConstCommand;
import types.builtins.TypeNil;
import utils.NotNull;

public class AST_EXP_NIL extends AST_EXP_CONSTANT {
    public AST_EXP_NIL() {
        super(TypeNil.instance);
    }

    @NotNull
    @Override
    protected String name() {
        return "NIL";
    }

    @NotNull
    @Override
    public Register irMe(IRContext context) {
        Register temp = context.newRegister();
        context.command(new IRConstCommand(temp, IRContext.NIL_VALUE));
        return temp;
    }
}

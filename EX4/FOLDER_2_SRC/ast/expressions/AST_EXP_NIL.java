package ast.expressions;

import ir.IRContext;
import ir.Register;
import ir.arithmetic.IRConstCommand;
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
        Register temp = context.getNewRegister();
        context.addCommand(new IRConstCommand(temp, IRContext.NilValue));
        return temp;
    }
}

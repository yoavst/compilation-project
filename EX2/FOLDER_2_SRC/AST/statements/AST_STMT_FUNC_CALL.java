package ast.statements;

import ast.expressions.AST_EXP_FUNC_CALL;
import utils.NotNull;

public class AST_STMT_FUNC_CALL extends AST_STMT {
    @NotNull
    public AST_EXP_FUNC_CALL funcCall;

    public AST_STMT_FUNC_CALL(@NotNull AST_EXP_FUNC_CALL funcCall) {
        this.funcCall = funcCall;
    }

    @NotNull
    @Override
    protected String name() {
        return "Func call";

    }

    @Override
    public void printMe() {
        super.printMe();
        printAndEdge(funcCall);
    }
}


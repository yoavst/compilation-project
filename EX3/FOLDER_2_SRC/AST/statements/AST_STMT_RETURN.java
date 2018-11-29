package ast.statements;

import ast.expressions.AST_EXP;
import utils.NotNull;
import utils.Nullable;

public class AST_STMT_RETURN extends AST_STMT {
    @Nullable
    public AST_EXP exp;

    public AST_STMT_RETURN(@Nullable AST_EXP exp) {
        this.exp = exp;
    }

    public AST_STMT_RETURN() {
        this(null);
    }

    @NotNull
    @Override
    protected String name() {
        return "RETURN";
    }

    @Override
    public void printMe() {
        super.printMe();
        printAndEdge(exp);
    }
}
package ast.statements;

import ast.variables.AST_VAR;
import ast.expressions.AST_NEW_EXP;
import utils.NotNull;

public class AST_STMT_ASSIGN_NEW extends AST_STMT {
    @NotNull
    public AST_VAR var;
    @NotNull
    public AST_NEW_EXP exp;

    public AST_STMT_ASSIGN_NEW(@NotNull AST_VAR var, @NotNull AST_NEW_EXP exp) {
        this.var = var;
        this.exp = exp;
    }

    @NotNull
    @Override
    protected String name() {
        return ":= new";
    }

    @Override
    public void printMe() {
        super.printMe();
        printAndEdge(var);
        printAndEdge(exp);
    }


}

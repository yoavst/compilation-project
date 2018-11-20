package ast.expressions;

import ast.variables.AST_VAR;
import utils.NotNull;

public class AST_EXP_VAR extends AST_EXP {
    @NotNull
    public AST_VAR var;


    public AST_EXP_VAR(@NotNull AST_VAR var) {
        this.var = var;
    }

    @NotNull
    @Override
    protected String name() {
        return "Var";
    }

    @Override
    public void printMe() {
        super.printMe();
        printAndEdge(var);
    }
}

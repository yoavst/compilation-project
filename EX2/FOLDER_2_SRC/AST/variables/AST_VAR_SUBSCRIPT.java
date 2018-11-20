package ast.variables;

import ast.expressions.AST_EXP;
import utils.NotNull;

public class AST_VAR_SUBSCRIPT extends AST_VAR {
    @NotNull
    public AST_VAR var;
    @NotNull
    public AST_EXP subscript;

    public AST_VAR_SUBSCRIPT(@NotNull AST_VAR var, @NotNull AST_EXP subscript) {
        this.var = var;
        this.subscript = subscript;
    }

    @NotNull
    @Override
    protected String name() {
        return "VAR(_.[...])";
    }

    @Override
    public void printMe() {
        super.printMe();
        printAndEdge(var);
        printAndEdge(subscript);
    }
}

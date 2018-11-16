package ast.variables;

import ast.expressions.AST_EXP;

public class AST_VAR_SUBSCRIPT extends AST_VAR {
    public AST_VAR var;
    public AST_EXP subscript;

    public AST_VAR_SUBSCRIPT(AST_VAR var, AST_EXP subscript) {
        this.var = var;
        this.subscript = subscript;
    }

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

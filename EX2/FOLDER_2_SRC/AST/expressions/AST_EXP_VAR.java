package ast.expressions;

import ast.variables.AST_VAR;

public class AST_EXP_VAR extends AST_EXP {
    public AST_VAR var;


    public AST_EXP_VAR(AST_VAR var) {
        this.var = var;
    }

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

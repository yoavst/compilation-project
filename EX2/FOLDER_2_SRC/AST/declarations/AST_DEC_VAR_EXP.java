package ast.declarations;

import ast.expressions.AST_EXP;

public class AST_DEC_VAR_EXP extends AST_DEC_VAR {
    public AST_EXP exp;

    public AST_DEC_VAR_EXP(String type, String name) {
        super(type, name);
    }

    public AST_DEC_VAR_EXP(String type, String name, AST_EXP exp) {
        super(type, name);
        this.exp = exp;
    }

    @Override
    protected String name() {
        return "var " + type + " " + name;
    }

    @Override
    public void printMe() {
        super.printMe();
        printAndEdge(exp);
    }
}
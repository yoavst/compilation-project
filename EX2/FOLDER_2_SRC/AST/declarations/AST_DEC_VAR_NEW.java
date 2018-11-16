package ast.declarations;

import ast.expressions.AST_NEW_EXP;

public class AST_DEC_VAR_NEW extends AST_DEC_VAR {
    public AST_NEW_EXP newExp;

    public AST_DEC_VAR_NEW(String type, String name, AST_NEW_EXP newExp) {
        super(type, name);
        this.newExp = newExp;
    }

    @Override
    protected String name() {
        return "new var " + type + " " + name;
    }

    @Override
    public void printMe() {
        super.printMe();
        printAndEdge(newExp);
    }
}
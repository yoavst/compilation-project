package ast.statements;

import ast.expressions.AST_EXP;

public class AST_STMT_RETURN extends AST_STMT {
    public AST_EXP exp;

    public AST_STMT_RETURN(AST_EXP exp) {
        this.exp = exp;
    }

    public AST_STMT_RETURN() {
        this(null);
    }

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
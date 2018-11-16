package ast.statements;

import ast.expressions.AST_EXP;

public class AST_STMT_WHILE extends AST_STMT {
    public AST_EXP cond;
    public AST_STMT_LIST body;


    public AST_STMT_WHILE(AST_EXP cond, AST_STMT_LIST body) {
        this.cond = cond;
        this.body = body;
    }

    @Override
    protected String name() {
        return "WHILE(...)";
    }

    @Override
    public void printMe() {
        super.printMe();
        printAndEdge(cond);
        printAndEdge(body);
    }

}
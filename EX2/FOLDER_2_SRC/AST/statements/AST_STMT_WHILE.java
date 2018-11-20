package ast.statements;

import ast.expressions.AST_EXP;

import java.util.List;

public class AST_STMT_WHILE extends AST_STMT {
    public AST_EXP cond;
    public List<AST_STMT> body;


    public AST_STMT_WHILE(AST_EXP cond, List<AST_STMT> body) {
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
        addListUnderWrapper("body", body);
    }

}
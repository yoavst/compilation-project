package ast.statements;

import ast.expressions.AST_EXP;
import utils.NotNull;

import java.util.List;

public class AST_STMT_WHILE extends AST_STMT {
    @NotNull
    public AST_EXP cond;
    @NotNull
    public AST_STMT[] body;


    public AST_STMT_WHILE(@NotNull AST_EXP cond, @NotNull AST_STMT[] body) {
        this.cond = cond;
        this.body = body;
    }

    @NotNull
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
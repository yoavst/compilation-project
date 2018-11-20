package ast.statements;

import ast.expressions.AST_EXP;
import utils.NotNull;

import java.util.List;

public class AST_STMT_IF extends AST_STMT {
    @NotNull
    public AST_EXP cond;
    @NotNull
    public List<AST_STMT> body;

    public AST_STMT_IF(@NotNull AST_EXP cond, @NotNull List<AST_STMT> body) {
        this.cond = cond;
        this.body = body;
    }

    @NotNull
    @Override
    protected String name() {
        return "IF";
    }

    @Override
    public void printMe() {
        super.printMe();
        printAndEdge(cond);
        addListUnderWrapper("body", body);
    }

}
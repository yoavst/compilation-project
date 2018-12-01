package ast.statements;

import ast.expressions.AST_EXP;
import symbols.SymbolTable;
import types.builtins.TypeInt;
import utils.NotNull;
import utils.SemanticException;

import java.util.List;

public class AST_STMT_IF extends AST_STMT {
    @NotNull
    public AST_EXP cond;
    @NotNull
    public AST_STMT[] body;

    public AST_STMT_IF(@NotNull AST_EXP cond, @NotNull AST_STMT[] body) {
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

    @Override
    protected void semantMe(SymbolTable symbolTable) throws SemanticException {
        cond.semant(symbolTable);
        if (cond.getType() != TypeInt.instance) {
            throwSemantic("if condition can only be int, received: " + cond.getType());
        }

        symbolTable.beginScope();
        for (AST_STMT statement : body) {
            statement.semant(symbolTable);
        }
        symbolTable.endScope();
    }

}
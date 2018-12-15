package ast.statements;

import ast.expressions.AST_EXP;
import symbols.SymbolTable;
import types.TypeError;
import types.builtins.TypeInt;
import utils.NotNull;
import utils.errors.SemanticException;

import static types.TYPE_FOR_SCOPE_BOUNDARIES.Scope.Block;

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

    @Override
    protected void semantMe(SymbolTable symbolTable) throws SemanticException {
        cond.semant(symbolTable);
        if (cond.getType() != TypeInt.instance && cond.getType() != TypeError.instance) {
            throwSemantic("while condition can only be int, received: " + cond.getType());
        }

        symbolTable.beginScope(Block, null, "while(...)");
        for (AST_STMT statement : body) {
            statement.semant(symbolTable);
        }
        symbolTable.endScope();
    }
}
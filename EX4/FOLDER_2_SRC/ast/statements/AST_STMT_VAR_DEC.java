package ast.statements;

import ast.declarations.AST_DEC_VAR;
import symbols.SymbolTable;
import utils.NotNull;
import utils.errors.SemanticException;

public class AST_STMT_VAR_DEC extends AST_STMT {
    @NotNull
    public AST_DEC_VAR var;

    public AST_STMT_VAR_DEC(@NotNull AST_DEC_VAR var) {
        this.var = var;
    }

    @NotNull
    @Override
    protected String name() {
        return "Stmt var dec";
    }

    @Override
    public void printMe() {
        super.printMe();
        printAndEdge(var);
    }

    @Override
    protected void semantMe(SymbolTable symbolTable) throws SemanticException {
        var.semantHeader(symbolTable);
        var.semant(symbolTable);
    }
}

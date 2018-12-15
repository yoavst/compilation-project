package ast.expressions;

import ast.variables.AST_VAR;
import symbols.SymbolTable;
import utils.NotNull;
import utils.errors.SemanticException;

public class AST_EXP_VAR extends AST_EXP {
    @NotNull
    public AST_VAR var;


    public AST_EXP_VAR(@NotNull AST_VAR var) {
        this.var = var;
    }

    @NotNull
    @Override
    protected String name() {
        return "Var";
    }

    @Override
    public void printMe() {
        super.printMe();
        printAndEdge(var);
    }

    @Override
    protected void semantMe(SymbolTable symbolTable) throws SemanticException {
        var.semant(symbolTable);
        type = var.getType();
    }
}

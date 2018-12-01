package ast.statements;

import ast.variables.AST_VAR;
import ast.expressions.AST_EXP;
import symbols.SymbolTable;
import utils.NotNull;
import utils.SemanticException;

/**
 * var := exp
 */
public class AST_STMT_ASSIGN extends AST_STMT {
    @NotNull
    public AST_VAR var;
    @NotNull
    public AST_EXP exp;

    public AST_STMT_ASSIGN(@NotNull AST_VAR var, @NotNull AST_EXP exp) {
        this.var = var;
        this.exp = exp;
    }

    @NotNull
    @Override
    protected String name() {
        return ":=";
    }

    @Override
    public void printMe() {
        super.printMe();
        printAndEdge(var);
        printAndEdge(exp);
    }

    @Override
    protected void semantMe(SymbolTable symbolTable) throws SemanticException {
        var.semant(symbolTable);
        exp.semant(symbolTable);

        if (var.getType() != exp.getType()) {
            throwSemantic("Trying to assign exp of type " + exp.getType() + " to a variable of type " + var.getType());
        }
    }
}

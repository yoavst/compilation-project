package ast.statements;

import ast.expressions.AST_EXP;
import ast.variables.AST_VAR;
import ir.registers.NonExistsRegister;
import ir.registers.Register;
import ir.utils.IRContext;
import symbols.SymbolTable;
import utils.NotNull;
import utils.errors.SemanticException;

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

        /* Section 3.2 in manual
         * arrayType := same arrayType | nil
         * classType := assignable classType | nil
         * int       := int
         * string    := string
         */
        if (!var.getType().isAssignableFrom(exp.getType())) {
            throwSemantic("Trying to assign exp of type " + exp.getType() + " to a variable of type " + var.getType());
        }
    }

    @Override
    public @NotNull Register irMe(IRContext context) {
        var.irAssignTo(context, () -> exp.irMe(context));
        return NonExistsRegister.instance;
    }
}

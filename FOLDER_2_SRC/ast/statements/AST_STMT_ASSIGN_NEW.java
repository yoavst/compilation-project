package ast.statements;

import ast.variables.AST_VAR;
import ast.expressions.AST_NEW_EXP;
import ir.registers.NonExistsRegister;
import ir.registers.Register;
import ir.utils.IRContext;
import symbols.SymbolTable;
import utils.NotNull;
import utils.errors.SemanticException;

public class AST_STMT_ASSIGN_NEW extends AST_STMT {
    @NotNull
    public AST_VAR var;
    @NotNull
    public AST_NEW_EXP exp;

    public AST_STMT_ASSIGN_NEW(@NotNull AST_VAR var, @NotNull AST_NEW_EXP exp) {
        this.var = var;
        this.exp = exp;
    }

    @NotNull
    @Override
    protected String name() {
        return ":= new";
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
         * arrayType := new same arrayType[]
         * classType := new assignable classType
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

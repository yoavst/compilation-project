package ast.statements;

import ast.expressions.AST_EXP;
import ir.utils.IRContext;
import ir.arithmetic.IRSetValueCommand;
import ir.flow.IRGotoCommand;
import ir.registers.NonExistsRegister;
import ir.registers.Register;
import ir.registers.ReturnRegister;
import symbols.Symbol;
import symbols.SymbolTable;
import types.TypeFunction;
import types.builtins.TypeVoid;
import utils.NotNull;
import utils.Nullable;
import utils.errors.SemanticException;

public class AST_STMT_RETURN extends AST_STMT {
    @Nullable
    private AST_EXP exp;
    private Symbol enclosingFunction;

    public AST_STMT_RETURN(@Nullable AST_EXP exp) {
        this.exp = exp;
    }

    public AST_STMT_RETURN() {
        this(null);
    }

    @NotNull
    @Override
    protected String name() {
        return "RETURN";
    }

    @Override
    public void printMe() {
        super.printMe();
        printAndEdge(exp);
    }

    @Override
    protected void semantMe(SymbolTable symbolTable) throws SemanticException {
        if (exp != null)
            exp.semant(symbolTable);

        enclosingFunction = symbolTable.getEnclosingFunction();
        if (enclosingFunction == null) {
            throwSemantic("Trying to return not from function context");
        }
        TypeFunction function = enclosingFunction.getFunction();
        if (function.returnType == TypeVoid.instance) {
            if (exp != null) {
                throwSemantic("Trying to return " + exp.getType() + " from void function");
            }
        } else if (exp == null) {
            throwSemantic("Trying to return void from non-void function. Expected: " + function.returnType);
        } else if (!function.returnType.isAssignableFrom(exp.getType())) {
            throwSemantic("Trying to return invalid type from function. Expected: " + function.returnType + ". Received: " + exp.getType() + ".");
        }
    }

    @Override
    public @NotNull Register irMe(IRContext context) {
        if (exp != null) {
            Register temp = exp.irMe(context);
            context.command(new IRSetValueCommand(ReturnRegister.instance, temp));
        }
        context.command(new IRGotoCommand(context.returnLabelFor(enclosingFunction)));

        return NonExistsRegister.instance;
    }
}
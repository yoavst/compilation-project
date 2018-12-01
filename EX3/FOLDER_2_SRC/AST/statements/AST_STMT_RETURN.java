package ast.statements;

import ast.expressions.AST_EXP;
import symbols.SymbolTable;
import types.TypeFunction;
import types.builtins.TypeVoid;
import utils.NotNull;
import utils.Nullable;
import utils.SemanticException;

public class AST_STMT_RETURN extends AST_STMT {
    @Nullable
    public AST_EXP exp;

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

        TypeFunction function = symbolTable.getEnclosingFunction();
        if (function == null) {
            throwSemantic("Trying to return not from function context");
        } else if (function.returnType == TypeVoid.instance) {
            if (exp != null) {
                throwSemantic("Trying to return " + exp.getType() + "from void function");
            }
        } else if (exp == null) {
            throwSemantic("Trying to return void from non-void function. Expected: " + function.returnType);
        } else if (!function.returnType.isAssignableFrom(exp.getType())) {
            throwSemantic("Trying to return invalid type from function. Expected: " + function.returnType + ". Received: " + exp.getType() + ".");
        }
    }
}
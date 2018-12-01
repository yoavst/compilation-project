package ast.declarations;

import ast.expressions.AST_EXP;
import ast.expressions.AST_EXP_CONSTANT;
import symbols.SymbolTable;
import types.Type;
import types.builtins.TypeVoid;
import utils.NotNull;
import utils.Nullable;
import utils.SemanticException;

public class AST_DEC_VAR_EXP extends AST_DEC_VAR {
    @Nullable
    public AST_EXP exp;

    private Type representingType;

    public AST_DEC_VAR_EXP(@NotNull String type,@NotNull String name) {
        super(type, name);
    }

    public AST_DEC_VAR_EXP(@NotNull String type, @NotNull String name, @Nullable AST_EXP exp) {
        super(type, name);
        this.exp = exp;
    }

    @NotNull
    @Override
    protected String name() {
        return "var " + type + " " + name;
    }

    @Override
    public void printMe() {
        super.printMe();
        printAndEdge(exp);
    }

    @Override
    protected void semantMe(SymbolTable symbolTable) {
        // no-op since just an header:
        // `semantHeader` takes care of  semanting `exp` and checking if it is a valid assignment
    }

    @Override
    public void semantHeader(SymbolTable symbolTable) throws SemanticException {
        //noinspection ConstantConditions
        representingType = symbolTable.findGeneralizedType(type);
        if (representingType == null || representingType == TypeVoid.instance) {
            throwSemantic("Trying to declare a variable of unknown type: " + type);
        }

        if (exp != null) {
            exp.semant(symbolTable);
            if (!representingType.isAssignableFrom(exp.getType())) {
                throwSemantic("Trying to declare a variable of type " + type + " but received " + exp.getType());
            } else if (symbolTable.getEnclosingFunction() == null && symbolTable.getEnclosingClass() != null) {
                /* Section 3.2 in manual
                 * class member can only be initialized by a simple constant expression: int | nil | string
                 */
                if (!(exp instanceof AST_EXP_CONSTANT)) {
                    throwSemantic("Trying to declare a class member \"" + name + "\" but using non-constant initializer");
                }
            }
        }

        // check scoping rules
        if (!canBeDefined(symbolTable)) {
            throwSemantic("Trying to define the variable \"" + name +"\", but it violates the scoping rules");
        }

        symbolTable.enter(name, representingType, true);
    }

    @Override
    public Type getType() {
        return representingType;
    }
}
package ast.declarations;

import ast.expressions.AST_NEW_EXP;
import symbols.SymbolTable;
import types.Type;
import types.builtins.TypeVoid;
import utils.NotNull;
import utils.SemanticException;

public class AST_DEC_VAR_NEW extends AST_DEC_VAR {
    @NotNull
    public AST_NEW_EXP newExp;

    public Type representingType;

    public AST_DEC_VAR_NEW(@NotNull String type, @NotNull String name, @NotNull AST_NEW_EXP newExp) {
        super(type, name);
        this.newExp = newExp;
    }

    @NotNull
    @Override
    protected String name() {
        return "new var " + type + " " + name;
    }

    @Override
    public void printMe() {
        super.printMe();
        printAndEdge(newExp);
    }

    @Override
    protected void semantMe(SymbolTable symbolTable) {
        // no-op since just an header:
        // `semantHeader` takes care of  semanting`newExp` and checking if it is a valid assignment
    }

    @Override
    public void semantHeader(SymbolTable symbolTable) throws SemanticException {
        //noinspection ConstantConditions
        representingType = symbolTable.findGeneralizedType(type);
        if (representingType == null || representingType == TypeVoid.instance) {
            throwSemantic("Trying to declare a variable of unknown type: " + type);
        }

        newExp.semant(symbolTable);
        if (!representingType.isAssignableFrom(newExp.getType())) {
            throwSemantic("Trying to declare a variable of type " + type + " but received new " + newExp.getType());
        } else if (symbolTable.getEnclosingFunction() == null && symbolTable.getEnclosingClass() != null) {
            /* Section 3.2 in manual
             * class member can only be initialized by a simple constant expression: int | nil | string
             * Specifically, new initializer is not a constant.
             */
            throwSemantic("Trying to declare a class member \"" + name + "\" but using non-constant new initializer");
        }

        // check scoping rules
        if (!canBeDefined(symbolTable)) {
            throwSemantic("Trying to define the variable \"" + name +"\", but it violates the scoping rules");
        }

        symbolTable.enter(name, representingType, true);
    }
}
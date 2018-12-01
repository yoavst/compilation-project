package ast.expressions;

import symbols.SymbolTable;
import types.Type;
import utils.NotNull;
import utils.SemanticException;

public class AST_NEW_EXP_SUBSCRIPT extends AST_NEW_EXP {
    @NotNull
    public AST_EXP subscript;

    public AST_NEW_EXP_SUBSCRIPT(String className, @NotNull AST_EXP subscript) {
        super(className);
        this.subscript = subscript;
    }

    @NotNull
    @Override
    protected String name() {
        return "new " + className + "[...]";
    }

    @Override
    public void printMe() {
        super.printMe();
        printAndEdge(subscript);
    }

    @Override
    protected void semantMe(SymbolTable symbolTable) throws SemanticException {
        subscript.semant(symbolTable);

        Type classType = symbolTable.findArrayType(className);
        if (classType != null) {
            // valid expression: new TypeArray[const_int]
            if (subscript instanceof AST_EXP_INT) {
                type = classType;
            } else {
                throwSemantic("Trying to create an array of \"" + className + "\" not of fixed int size: " + subscript);
            }
        } else {
            // Since poseidon classes do not have a constructor, it's an error.
            throwSemantic("Trying to create a new[] expression of invalid type: \"" + className + "\".");
        }
    }
}

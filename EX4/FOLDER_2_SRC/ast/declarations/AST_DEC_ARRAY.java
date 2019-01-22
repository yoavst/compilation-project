package ast.declarations;

import symbols.SymbolTable;
import types.Type;
import types.builtins.TypeArray;
import types.builtins.TypeVoid;
import utils.NotNull;
import utils.errors.SemanticException;

public class AST_DEC_ARRAY extends AST_DEC {
    public AST_DEC_ARRAY(@NotNull String type, @NotNull String name) {
        super(type, name);
    }

    @NotNull
    @Override
    protected String name() {
        return "Arr: " + type + "[] " + name;
    }

    @Override
    protected void semantMe(SymbolTable symbolTable) {
       // no-op since just an header
    }

    @Override
    public void semantMeHeader(SymbolTable symbolTable) throws SemanticException {
        @SuppressWarnings("ConstantConditions")
        Type arrayType = symbolTable.findGeneralizedType(type);
        if (arrayType == null) {
            throwSemantic("Trying to declare an array type of an unknown type: \"" + type + "\"");
        } else if (arrayType == TypeVoid.instance) {
            throwSemantic("Trying to declare an array type of type void.");
        }

        // check scoping rules
        if (symbolTable.find(name) != null) {
            throwSemantic("Trying to declare an array typedef but the name \"" + name + "\" is already in use");
        }
        symbolTable.enter(name, new TypeArray(name, arrayType));
    }
}